CREATE TABLE IF NOT EXISTS agendamento_servicos
(
    agendamento_id BIGINT NOT NULL,
    servico_id     BIGINT NOT NULL,

    PRIMARY KEY (agendamento_id, servico_id),

    CONSTRAINT fk_agend_serv_agendamento FOREIGN KEY (agendamento_id) REFERENCES agendamentos (id),
    CONSTRAINT fk_agend_serv_servicos FOREIGN KEY (servico_id) REFERENCES servicos (id)
);

