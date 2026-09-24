-- CPF do tutor, armazenado apenas com dígitos.
-- Anulável para não quebrar tutores já cadastrados; a API passa a exigi-lo em cadastros e atualizações.
ALTER TABLE tutores ADD COLUMN cpf VARCHAR(11);

CREATE UNIQUE INDEX uk_tutores_cpf ON tutores (cpf);
