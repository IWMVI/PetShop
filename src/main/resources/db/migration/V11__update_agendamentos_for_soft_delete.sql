ALTER TABLE agendamentos
    ADD COLUMN IF NOT EXISTS observacoes TEXT;

ALTER TABLE agendamentos
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

UPDATE agendamentos
SET status = 'AGENDADO'
WHERE status IS NULL;

ALTER TABLE agendamentos
    ALTER COLUMN status SET NOT NULL;
