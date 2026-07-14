-- ============================================================
-- Sales process stagnation monitoring
-- PL/pgSQL integration for the Sales Process module
-- ============================================================

-- Pragovi definišu koliko dana prodajni proces može da ostane
-- u određenoj fazi pre nego što se kreira upozorenje.
CREATE TABLE sales_stage_stagnation_thresholds (
    id BIGSERIAL PRIMARY KEY,
    stage_name VARCHAR(100) NOT NULL UNIQUE,
    warning_days INTEGER NOT NULL,
    critical_days INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_stagnation_warning_days
        CHECK (warning_days > 0),

    CONSTRAINT chk_stagnation_critical_days
        CHECK (critical_days > warning_days)
);

-- Upozorenja koja generiše PL/pgSQL procedura.
CREATE TABLE sales_stagnation_alerts (
    id BIGSERIAL PRIMARY KEY,
    sales_process_id BIGINT NOT NULL,
    stage_name VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    days_in_stage INTEGER NOT NULL,
    message VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,

    CONSTRAINT fk_stagnation_alert_sales_process
        FOREIGN KEY (sales_process_id)
        REFERENCES sales_processes(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_stagnation_alert_severity
        CHECK (severity IN ('WARNING', 'CRITICAL')),

    CONSTRAINT chk_stagnation_alert_status
        CHECK (status IN ('OPEN', 'RESOLVED'))
);

-- Za isti proces, fazu i nivo upozorenja može postojati samo
-- jedno otvoreno upozorenje.
CREATE UNIQUE INDEX uq_open_sales_stagnation_alert
    ON sales_stagnation_alerts (
        sales_process_id,
        stage_name,
        severity
    )
    WHERE status = 'OPEN';

CREATE INDEX idx_sales_stagnation_alert_process
    ON sales_stagnation_alerts (sales_process_id);

CREATE INDEX idx_sales_stagnation_alert_status
    ON sales_stagnation_alerts (status);

CREATE INDEX idx_sales_process_history_process_changed_at
    ON sales_process_history (sales_process_id, changed_at DESC);

CREATE INDEX idx_sales_processes_status
    ON sales_processes (status);


-- ============================================================
-- Početni pragovi za faze iz podrazumevanog Sales Workflow-a
-- ============================================================

INSERT INTO sales_stage_stagnation_thresholds (
    stage_name,
    warning_days,
    critical_days
)
VALUES
    ('New', 3, 7),
    ('Contacted', 5, 10),
    ('Qualified', 7, 14),
    ('Proposal Sent', 7, 14),
    ('Negotiation', 10, 21)
ON CONFLICT (stage_name) DO NOTHING;


-- ============================================================
-- Funkcija vraća datum poslednjeg ulaska procesa u trenutnu fazu.
--
-- Uzima poslednju promenu iz sales_process_history za koju je
-- new_stage jednaka trenutnoj fazi procesa.
--
-- Ako istorija ne postoji, koristi created_at procesa.
-- ============================================================

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
      AND sph.new_stage = p_current_stage
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


-- ============================================================
-- Funkcija određuje nivo upozorenja na osnovu faze i broja dana.
-- ============================================================

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
    WHERE threshold.stage_name = p_stage_name
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


-- ============================================================
-- Glavna procedura:
-- 1. prolazi kroz aktivne prodajne procese;
-- 2. određuje koliko dugo je proces u trenutnoj fazi;
-- 3. određuje severity;
-- 4. kreira upozorenje;
-- 5. kreira FOLLOW_UP aktivnost ako već ne postoji.
-- ============================================================

CREATE OR REPLACE PROCEDURE pr_run_sales_stagnation_check()
LANGUAGE plpgsql
AS $$
DECLARE
    v_process RECORD;
    v_stage_entered_at TIMESTAMP;
    v_days_in_stage INTEGER;
    v_severity VARCHAR(20);
    v_message VARCHAR(1000);
BEGIN
    FOR v_process IN
        SELECT
            sp.id,
            sp.title,
            sp.stage
        FROM sales_processes sp
        WHERE sp.status = 'ACTIVE'
    LOOP
        BEGIN
            v_stage_entered_at :=
                fn_sales_stage_entered_at(
                    v_process.id,
                    v_process.stage
                );

            IF v_stage_entered_at IS NULL THEN
                CONTINUE;
            END IF;

            v_days_in_stage :=
                GREATEST(
                    0,
                    CURRENT_DATE - v_stage_entered_at::DATE
                );

            v_severity :=
                fn_sales_stagnation_severity(
                    v_process.stage,
                    v_days_in_stage
                );

            IF v_severity IS NULL THEN
                CONTINUE;
            END IF;

            v_message :=
                'Sales process "' || v_process.title ||
                '" has remained in stage "' || v_process.stage ||
                '" for ' || v_days_in_stage ||
                ' days. Severity: ' || v_severity || '.';

            INSERT INTO sales_stagnation_alerts (
                sales_process_id,
                stage_name,
                severity,
                days_in_stage,
                message
            )
            VALUES (
                v_process.id,
                v_process.stage,
                v_severity,
                v_days_in_stage,
                v_message
            )
            ON CONFLICT DO NOTHING;

            IF NOT EXISTS (
                SELECT 1
                FROM sales_activities activity
                WHERE activity.sales_process_id = v_process.id
                  AND activity.status = 'PLANNED'
                  AND activity.type = 'FOLLOW_UP'
                  AND activity.title =
                      'Follow up stagnant sales process'
            ) THEN
                INSERT INTO sales_activities (
                    sales_process_id,
                    type,
                    status,
                    title,
                    description,
                    scheduled_at
                )
                VALUES (
                    v_process.id,
                    'FOLLOW_UP',
                    'PLANNED',
                    'Follow up stagnant sales process',
                    v_message,
                    CURRENT_TIMESTAMP
                );
            END IF;

        EXCEPTION
            WHEN OTHERS THEN
                RAISE WARNING
                    'Stagnation check failed for sales process %: %',
                    v_process.id,
                    SQLERRM;
        END;
    END LOOP;
END;
$$;