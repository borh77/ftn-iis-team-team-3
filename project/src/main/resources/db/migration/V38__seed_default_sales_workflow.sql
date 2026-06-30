INSERT INTO sales_workflows (name, region, active)
VALUES ('Default Sales Workflow', 'GLOBAL', TRUE);

WITH workflow AS (
    SELECT id FROM sales_workflows
    WHERE name = 'Default Sales Workflow' AND region = 'GLOBAL'
    LIMIT 1
),
stages AS (
    INSERT INTO sales_stage_definitions
        (workflow_id, name, description, stage_order, start_stage, end_stage, successful_end, required_inputs, expected_outputs)
    VALUES
        ((SELECT id FROM workflow), 'New', 'Initial sales process stage', 1, TRUE, FALSE, FALSE, 'Lead exists', 'Lead contacted'),
        ((SELECT id FROM workflow), 'Contacted', 'Customer has been contacted', 2, FALSE, FALSE, FALSE, 'Contact information exists', 'Customer interest confirmed'),
        ((SELECT id FROM workflow), 'Qualified', 'Lead is qualified', 3, FALSE, FALSE, FALSE, 'Customer need identified', 'Qualified opportunity'),
        ((SELECT id FROM workflow), 'Proposal Sent', 'Offer has been created and sent', 4, FALSE, FALSE, FALSE, 'Offer exists', 'Offer sent to customer'),
        ((SELECT id FROM workflow), 'Negotiation', 'Negotiation with customer is active', 5, FALSE, FALSE, FALSE, 'Offer sent', 'Offer accepted or rejected'),
        ((SELECT id FROM workflow), 'Closed Won', 'Sales process successfully completed', 6, FALSE, TRUE, TRUE, 'Accepted offer and contract', 'Deal won'),
        ((SELECT id FROM workflow), 'Closed Lost', 'Sales process unsuccessfully completed', 7, FALSE, TRUE, FALSE, 'Customer rejection or failed negotiation', 'Deal lost')
    RETURNING id, name
)
INSERT INTO sales_stage_transitions
    (workflow_id, from_stage_id, to_stage_id, condition_type, condition_description)
SELECT
    (SELECT id FROM workflow),
    from_stage.id,
    to_stage.id,
    transition.condition_type,
    transition.condition_description
FROM (
    VALUES
        ('New', 'Contacted', 'CONTACT_RECORDED', 'Customer contact must be recorded'),
        ('Contacted', 'Qualified', 'LEAD_QUALIFIED', 'Lead must be qualified'),
        ('Qualified', 'Proposal Sent', 'OFFER_CREATED', 'Offer must be created'),
        ('Proposal Sent', 'Negotiation', 'COMMUNICATION_RECORDED', 'Customer communication must be recorded'),
        ('Negotiation', 'Closed Won', 'OFFER_ACCEPTED', 'Offer must be accepted'),
        ('Negotiation', 'Closed Lost', 'DEAL_LOST', 'Deal is marked as lost')
) AS transition(from_name, to_name, condition_type, condition_description)
JOIN stages from_stage ON from_stage.name = transition.from_name
JOIN stages to_stage ON to_stage.name = transition.to_name;