CREATE TABLE IF NOT EXISTS sales_workflows (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    region VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sales_stage_definitions (
    id BIGSERIAL PRIMARY KEY,
    workflow_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    stage_order INTEGER NOT NULL,
    start_stage BOOLEAN NOT NULL DEFAULT FALSE,
    end_stage BOOLEAN NOT NULL DEFAULT FALSE,
    successful_end BOOLEAN NOT NULL DEFAULT FALSE,
    required_inputs TEXT,
    expected_outputs TEXT
);

CREATE TABLE IF NOT EXISTS sales_stage_transitions (
    id BIGSERIAL PRIMARY KEY,
    workflow_id BIGINT NOT NULL,
    from_stage_id BIGINT NOT NULL,
    to_stage_id BIGINT NOT NULL,
    condition_type VARCHAR(100),
    condition_description VARCHAR(500)
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_sales_stage_workflow'
    ) THEN
        ALTER TABLE sales_stage_definitions
        ADD CONSTRAINT fk_sales_stage_workflow
        FOREIGN KEY (workflow_id)
        REFERENCES sales_workflows(id)
        ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_sales_transition_workflow'
    ) THEN
        ALTER TABLE sales_stage_transitions
        ADD CONSTRAINT fk_sales_transition_workflow
        FOREIGN KEY (workflow_id)
        REFERENCES sales_workflows(id)
        ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_sales_transition_from_stage'
    ) THEN
        ALTER TABLE sales_stage_transitions
        ADD CONSTRAINT fk_sales_transition_from_stage
        FOREIGN KEY (from_stage_id)
        REFERENCES sales_stage_definitions(id)
        ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_sales_transition_to_stage'
    ) THEN
        ALTER TABLE sales_stage_transitions
        ADD CONSTRAINT fk_sales_transition_to_stage
        FOREIGN KEY (to_stage_id)
        REFERENCES sales_stage_definitions(id)
        ON DELETE CASCADE;
    END IF;
END $$;

INSERT INTO sales_workflows (name, region, active)
SELECT 'Default Sales Workflow', 'GLOBAL', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM sales_workflows
    WHERE name = 'Default Sales Workflow' AND COALESCE(region, 'GLOBAL') = 'GLOBAL'
);

WITH workflow AS (
    SELECT id FROM sales_workflows
    WHERE name = 'Default Sales Workflow' AND COALESCE(region, 'GLOBAL') = 'GLOBAL'
    LIMIT 1
),
missing_stages AS (
    SELECT *
    FROM (
        VALUES
            ('New', 'Initial sales process stage', 1, TRUE, FALSE, FALSE, 'Lead exists', 'Lead contacted'),
            ('Contacted', 'Customer has been contacted', 2, FALSE, FALSE, FALSE, 'Contact information exists', 'Customer interest confirmed'),
            ('Qualified', 'Lead is qualified', 3, FALSE, FALSE, FALSE, 'Customer need identified', 'Qualified opportunity'),
            ('Proposal Sent', 'Offer has been created and sent', 4, FALSE, FALSE, FALSE, 'Offer exists', 'Offer sent to customer'),
            ('Negotiation', 'Negotiation with customer is active', 5, FALSE, FALSE, FALSE, 'Offer sent', 'Offer accepted or rejected'),
            ('Closed Won', 'Sales process successfully completed', 6, FALSE, TRUE, TRUE, 'Accepted offer and contract', 'Deal won'),
            ('Closed Lost', 'Sales process unsuccessfully completed', 7, FALSE, TRUE, FALSE, 'Customer rejection or failed negotiation', 'Deal lost')
    ) AS stage(name, description, stage_order, start_stage, end_stage, successful_end, required_inputs, expected_outputs)
    WHERE NOT EXISTS (
        SELECT 1 FROM sales_stage_definitions existing
        WHERE existing.workflow_id = (SELECT id FROM workflow)
          AND existing.name = stage.name
    )
)
INSERT INTO sales_stage_definitions
    (workflow_id, name, description, stage_order, start_stage, end_stage, successful_end, required_inputs, expected_outputs)
SELECT
    (SELECT id FROM workflow),
    name,
    description,
    stage_order,
    start_stage,
    end_stage,
    successful_end,
    required_inputs,
    expected_outputs
FROM missing_stages;

WITH workflow AS (
    SELECT id FROM sales_workflows
    WHERE name = 'Default Sales Workflow' AND COALESCE(region, 'GLOBAL') = 'GLOBAL'
    LIMIT 1
),
transition_seed AS (
    SELECT *
    FROM (
        VALUES
            ('New', 'Contacted', 'CONTACT_RECORDED', 'Customer contact must be recorded'),
            ('Contacted', 'Qualified', 'LEAD_QUALIFIED', 'Lead must be qualified'),
            ('Qualified', 'Proposal Sent', 'OFFER_CREATED', 'Offer must be created'),
            ('Proposal Sent', 'Negotiation', 'COMMUNICATION_RECORDED', 'Customer communication must be recorded'),
            ('Negotiation', 'Closed Won', 'OFFER_ACCEPTED', 'Offer must be accepted'),
            ('Negotiation', 'Closed Lost', 'DEAL_LOST', 'Deal is marked as lost')
    ) AS transition(from_name, to_name, condition_type, condition_description)
)
INSERT INTO sales_stage_transitions
    (workflow_id, from_stage_id, to_stage_id, condition_type, condition_description)
SELECT
    (SELECT id FROM workflow),
    from_stage.id,
    to_stage.id,
    transition.condition_type,
    transition.condition_description
FROM transition_seed transition
JOIN sales_stage_definitions from_stage
  ON from_stage.workflow_id = (SELECT id FROM workflow)
 AND from_stage.name = transition.from_name
JOIN sales_stage_definitions to_stage
  ON to_stage.workflow_id = (SELECT id FROM workflow)
 AND to_stage.name = transition.to_name
WHERE NOT EXISTS (
    SELECT 1 FROM sales_stage_transitions existing
    WHERE existing.workflow_id = (SELECT id FROM workflow)
      AND existing.from_stage_id = from_stage.id
      AND existing.to_stage_id = to_stage.id
);

ALTER TABLE sales_processes
ADD COLUMN IF NOT EXISTS workflow_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_sales_processes_workflow'
    ) THEN
        ALTER TABLE sales_processes
        ADD CONSTRAINT fk_sales_processes_workflow
        FOREIGN KEY (workflow_id)
        REFERENCES sales_workflows(id);
    END IF;
END $$;

ALTER TABLE sales_workflows
ADD COLUMN IF NOT EXISTS region_id BIGINT;

ALTER TABLE sales_workflows
ALTER COLUMN region DROP NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_sales_workflows_region'
    ) THEN
        ALTER TABLE sales_workflows
        ADD CONSTRAINT fk_sales_workflows_region
        FOREIGN KEY (region_id)
        REFERENCES regions(id);
    END IF;
END $$;
