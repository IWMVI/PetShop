ALTER TABLE pagamentos RENAME COLUMN metodo TO metodo_pagamento;
ALTER TABLE pagamentos ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
