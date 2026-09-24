ALTER TABLE agendamento_servicos
    ADD COLUMN IF NOT EXISTS preco_cobrado DECIMAL(10, 2);

UPDATE agendamento_servicos
SET preco_cobrado = 0.00
WHERE preco_cobrado IS NULL;

ALTER TABLE agendamento_servicos
    ALTER COLUMN preco_cobrado SET NOT NULL;
