ALTER TABLE lancamentos_financeiros ALTER COLUMN data DROP NOT NULL;
ALTER TABLE lancamentos_financeiros ADD COLUMN data_vencimento TIMESTAMP;
ALTER TABLE lancamentos_financeiros ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PAGO';
ALTER TABLE lancamentos_financeiros ALTER COLUMN status DROP DEFAULT;
CREATE INDEX idx_lancamento_tipo_status ON lancamentos_financeiros (tipo, status);
