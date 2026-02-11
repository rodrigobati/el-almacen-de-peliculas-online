UPDATE pelicula
SET version = 0
WHERE version IS NULL OR version < 0;

ALTER TABLE pelicula
MODIFY COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE pelicula
ADD CONSTRAINT chk_pelicula_version_non_negative CHECK (version >= 0);
