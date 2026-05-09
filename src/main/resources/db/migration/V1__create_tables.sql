CREATE TABLE pedidos (
                         id                   VARCHAR(36)  NOT NULL,
                         cliente_id           VARCHAR(36)  NOT NULL,
                         status               VARCHAR(20)  NOT NULL,
                         criado_em            TIMESTAMP    NOT NULL,
                         responsavel          VARCHAR(100),
                         concluido_em         TIMESTAMP,
                         motivo_cancelamento  VARCHAR(500),

                         CONSTRAINT pk_pedidos PRIMARY KEY (id),
                         CONSTRAINT chk_pedidos_status
                             CHECK (status IN ('AGUARDANDO','PROCESSANDO','CONCLUIDO','CANCELADO'))
);

CREATE TABLE itens (
                       id               VARCHAR(36)    NOT NULL,
                       pedido_id        VARCHAR(36)    NOT NULL,
                       produto_id       VARCHAR(36)    NOT NULL,
                       nome             VARCHAR(255)   NOT NULL,
                       quantidade       INTEGER        NOT NULL,
                       preco_unitario   NUMERIC(10,2)  NOT NULL,

                       CONSTRAINT pk_itens          PRIMARY KEY (id),
                       CONSTRAINT fk_itens_pedido   FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
                       CONSTRAINT chk_quantidade    CHECK (quantidade > 0),
                       CONSTRAINT chk_preco         CHECK (preco_unitario > 0)
);