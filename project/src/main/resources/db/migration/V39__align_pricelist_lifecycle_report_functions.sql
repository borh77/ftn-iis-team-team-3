DROP FUNCTION IF EXISTS avg_pricelist_time_per_status(TIMESTAMPTZ, TIMESTAMPTZ);
DROP FUNCTION IF EXISTS pricelist_status_change_counts(TIMESTAMPTZ, TIMESTAMPTZ);
DROP FUNCTION IF EXISTS slowest_pricelist_status_processes(INTEGER, TIMESTAMPTZ, TIMESTAMPTZ);
DROP FUNCTION IF EXISTS current_pricelist_status_counts();

CREATE OR REPLACE FUNCTION avg_pricelist_time_per_status(
    p_start TIMESTAMPTZ DEFAULT NULL,
    p_end TIMESTAMPTZ DEFAULT NULL
)
RETURNS TABLE (
    status VARCHAR,
    average_seconds NUMERIC,
    average_hours NUMERIC,
    transition_count BIGINT
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        h.old_status::VARCHAR AS status,
        AVG(h.duration_in_previous_status_seconds)::NUMERIC AS average_seconds,
        (AVG(h.duration_in_previous_status_seconds) / 3600.0)::NUMERIC AS average_hours,
        COUNT(*)::BIGINT AS transition_count
    FROM pricelist_status_history h
    WHERE (p_start IS NULL OR h.changed_at >= p_start)
      AND (p_end IS NULL OR h.changed_at <= p_end)
    GROUP BY h.old_status
    ORDER BY AVG(h.duration_in_previous_status_seconds) DESC;
END;
$$;

CREATE OR REPLACE FUNCTION pricelist_status_change_counts(
    p_start TIMESTAMPTZ DEFAULT NULL,
    p_end TIMESTAMPTZ DEFAULT NULL
)
RETURNS TABLE (
    transition_name TEXT,
    old_status VARCHAR,
    new_status VARCHAR,
    transition_count BIGINT
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        CASE
            WHEN h.old_status = 'DRAFT' AND h.new_status = 'IN_REVIEW' THEN 'DRAFT_TO_IN_REVIEW'
            WHEN h.old_status = 'IN_REVIEW' AND h.new_status = 'ACTIVE' THEN 'IN_REVIEW_TO_ACTIVE'
            WHEN h.old_status = 'IN_REVIEW' AND h.new_status = 'DRAFT' THEN 'IN_REVIEW_TO_DRAFT'
            WHEN h.old_status = 'ACTIVE' AND h.new_status = 'ARCHIVED' THEN 'ACTIVE_TO_ARCHIVED'
            ELSE h.old_status || '_TO_' || h.new_status
        END AS transition_name,
        h.old_status::VARCHAR AS old_status,
        h.new_status::VARCHAR AS new_status,
        COUNT(*)::BIGINT AS transition_count
    FROM pricelist_status_history h
    WHERE (p_start IS NULL OR h.changed_at >= p_start)
      AND (p_end IS NULL OR h.changed_at <= p_end)
    GROUP BY h.old_status, h.new_status
    ORDER BY COUNT(*) DESC, 1;
END;
$$;

CREATE OR REPLACE FUNCTION slowest_pricelist_status_processes(
    p_limit INT DEFAULT 10,
    p_start TIMESTAMPTZ DEFAULT NULL,
    p_end TIMESTAMPTZ DEFAULT NULL
)
RETURNS TABLE (
    pricelist_id BIGINT,
    old_status VARCHAR,
    new_status VARCHAR,
    changed_at TIMESTAMPTZ,
    duration_seconds BIGINT,
    duration_hours NUMERIC
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        h.pricelist_id,
        h.old_status::VARCHAR AS old_status,
        h.new_status::VARCHAR AS new_status,
        h.changed_at,
        h.duration_in_previous_status_seconds AS duration_seconds,
        (h.duration_in_previous_status_seconds / 3600.0)::NUMERIC AS duration_hours
    FROM pricelist_status_history h
    WHERE (p_start IS NULL OR h.changed_at >= p_start)
      AND (p_end IS NULL OR h.changed_at <= p_end)
    ORDER BY h.duration_in_previous_status_seconds DESC, h.changed_at DESC
    LIMIT GREATEST(COALESCE(p_limit, 10), 0);
END;
$$;

CREATE OR REPLACE FUNCTION current_pricelist_status_counts()
RETURNS TABLE (
    status VARCHAR,
    count BIGINT
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        p.status::VARCHAR AS status,
        COUNT(*)::BIGINT AS count
    FROM pricelists p
    GROUP BY p.status
    ORDER BY COUNT(*) DESC, p.status;
END;
$$;

-- Manual report examples:
-- SELECT * FROM avg_pricelist_time_per_status('2026-06-01T00:00:00Z', '2026-06-30T23:59:59Z');
-- SELECT * FROM pricelist_status_change_counts('2026-06-01T00:00:00Z', '2026-06-30T23:59:59Z');
-- SELECT * FROM slowest_pricelist_status_processes(10, '2026-06-01T00:00:00Z', '2026-06-30T23:59:59Z');
-- SELECT * FROM current_pricelist_status_counts();
