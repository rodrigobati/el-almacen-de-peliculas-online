CREATE TABLE IF NOT EXISTS eventos_procesados (
    event_id VARCHAR(64) PRIMARY KEY,
    processed_at TIMESTAMP NOT NULL,
    source VARCHAR(32) NOT NULL,
    compra_id BIGINT
);

CREATE INDEX IF NOT EXISTS idx_eventos_procesados_compra ON eventos_procesados (compra_id);

CREATE TABLE IF NOT EXISTS catalogo_outbox_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    event_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    payload_json VARCHAR(8000) NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    published_at TIMESTAMP,
    attempts INT NOT NULL,
    last_error VARCHAR(1000)
);

CREATE INDEX IF NOT EXISTS idx_catalogo_outbox_status_created ON catalogo_outbox_event (status, created_at);
