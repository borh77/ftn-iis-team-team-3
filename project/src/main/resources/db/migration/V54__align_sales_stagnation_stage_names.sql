-- ============================================================
-- Usklađivanje naziva faza za stagnation monitoring
-- ============================================================

-- U aplikaciji se faze čuvaju velikim slovima, npr. NEW,
-- QUALIFIED i NEGOTIATION. Pragovi moraju koristiti isti format.

UPDATE sales_stage_stagnation_thresholds
SET stage_name = UPPER(stage_name);

-- Funkcija sada poredi faze nezavisno od velikih i malih slova.
CREATE OR REPLACE FUNCTION fn_sales_stagnation_severity(
    p_stage_name VARCHAR,
    p_days_in_stage INTEGER
)
RETURNS VARCHAR
LANGUAGE plpgsql
AS $$
DECLARE
    v_warning_days INTEGER;
    v_critical_days INTEGER;
BEGIN
    SELECT
        threshold.warning_days,
        threshold.critical_days
    INTO
        v_warning_days,
        v_critical_days
    FROM sales_stage_stagnation_thresholds threshold
    WHERE UPPER(threshold.stage_name) = UPPER(p_stage_name)
      AND threshold.active = TRUE;

    IF p_days_in_stage >= v_critical_days THEN
        RETURN 'CRITICAL';
    ELSIF p_days_in_stage >= v_warning_days THEN
        RETURN 'WARNING';
    ELSE
        RETURN NULL;
    END IF;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN NULL;
END;
$$;


-- I datum ulaska u fazu tražimo nezavisno od veličine slova.
CREATE OR REPLACE FUNCTION fn_sales_stage_entered_at(
    p_sales_process_id BIGINT,
    p_current_stage VARCHAR
)
RETURNS TIMESTAMP
LANGUAGE plpgsql
AS $$
DECLARE
    v_stage_entered_at TIMESTAMP;
BEGIN
    SELECT sph.changed_at
    INTO v_stage_entered_at
    FROM sales_process_history sph
    WHERE sph.sales_process_id = p_sales_process_id
      AND UPPER(sph.new_stage) = UPPER(p_current_stage)
    ORDER BY sph.changed_at DESC
    LIMIT 1;

    IF v_stage_entered_at IS NULL THEN
        SELECT sp.created_at
        INTO v_stage_entered_at
        FROM sales_processes sp
        WHERE sp.id = p_sales_process_id;
    END IF;

    RETURN v_stage_entered_at;
END;
$$;