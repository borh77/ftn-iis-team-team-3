CREATE OR REPLACE FUNCTION avg_pricelist_time_per_status(
    start_time TIMESTAMPTZ DEFAULT NULL,
    end_time TIMESTAMPTZ DEFAULT NULL
)
RETURNS TABLE (
    status VARCHAR(32),
    average_duration_seconds NUMERIC,
    transition_count BIGINT
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        h.old_status AS status,
        AVG(h.duration_in_previous_status_seconds)::NUMERIC AS average_duration_seconds,
        COUNT(*)::BIGINT AS transition_count
    FROM pricelist_status_history h
    WHERE (start_time IS NULL OR h.changed_at >= start_time)
      AND (end_time IS NULL OR h.changed_at <= end_time)
    GROUP BY h.old_status
    ORDER BY h.old_status;
END;
$$;

CREATE OR REPLACE FUNCTION pricelist_status_change_counts(
    start_time TIMESTAMPTZ DEFAULT NULL,
    end_time TIMESTAMPTZ DEFAULT NULL
)
RETURNS TABLE (
    old_status VARCHAR(32),
    new_status VARCHAR(32),
    transition_count BIGINT
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        h.old_status,
        h.new_status,
        COUNT(*)::BIGINT AS transition_count
    FROM pricelist_status_history h
    WHERE (start_time IS NULL OR h.changed_at >= start_time)
      AND (end_time IS NULL OR h.changed_at <= end_time)
    GROUP BY h.old_status, h.new_status
    ORDER BY COUNT(*) DESC, h.old_status, h.new_status;
END;
$$;

CREATE OR REPLACE FUNCTION slowest_pricelist_status_processes(
    result_limit INTEGER DEFAULT 10,
    start_time TIMESTAMPTZ DEFAULT NULL,
    end_time TIMESTAMPTZ DEFAULT NULL
)
RETURNS TABLE (
    pricelist_id BIGINT,
    old_status VARCHAR(32),
    new_status VARCHAR(32),
    changed_at TIMESTAMPTZ,
    duration_in_previous_status_seconds BIGINT
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        h.pricelist_id,
        h.old_status,
        h.new_status,
        h.changed_at,
        h.duration_in_previous_status_seconds
    FROM pricelist_status_history h
    WHERE (start_time IS NULL OR h.changed_at >= start_time)
      AND (end_time IS NULL OR h.changed_at <= end_time)
    ORDER BY h.duration_in_previous_status_seconds DESC, h.changed_at DESC
    LIMIT GREATEST(COALESCE(result_limit, 10), 0);
END;
$$;
