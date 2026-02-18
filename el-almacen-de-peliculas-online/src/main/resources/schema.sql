CREATE TABLE IF NOT EXISTS eventos_procesados (
    event_id VARCHAR(64) PRIMARY KEY,
    processed_at TIMESTAMP NOT NULL,
    source VARCHAR(32) NOT NULL,
    compra_id BIGINT
);

CREATE INDEX IF NOT EXISTS idx_eventos_procesados_compra
    ON eventos_procesados (compra_id);
