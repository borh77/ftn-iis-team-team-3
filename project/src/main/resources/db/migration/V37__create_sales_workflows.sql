CREATE TABLE sales_workflows (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    region VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sales_stage_definitions (
    id BIGSERIAL PRIMARY KEY,
    workflow_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    stage_order INTEGER NOT NULL,
    start_stage BOOLEAN NOT NULL DEFAULT FALSE,
    end_stage BOOLEAN NOT NULL DEFAULT FALSE,
    successful_end BOOLEAN NOT NULL DEFAULT FALSE,
    required_inputs TEXT,
    expected_outputs TEXT,

    CONSTRAINT fk_sales_stage_workflow
        FOREIGN KEY (workflow_id)
        REFERENCES sales_workflows(id)
        ON DELETE CASCADE
);

CREATE TABLE sales_stage_transitions (
    id BIGSERIAL PRIMARY KEY,
    workflow_id BIGINT NOT NULL,
    from_stage_id BIGINT NOT NULL,
    to_stage_id BIGINT NOT NULL,
    condition_type VARCHAR(100),
    condition_description VARCHAR(500),

    CONSTRAINT fk_sales_transition_workflow
        FOREIGN KEY (workflow_id)
        REFERENCES sales_workflows(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_sales_transition_from_stage
        FOREIGN KEY (from_stage_id)
        REFERENCES sales_stage_definitions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_sales_transition_to_stage
        FOREIGN KEY (to_stage_id)
        REFERENCES sales_stage_definitions(id)
        ON DELETE CASCADE
);