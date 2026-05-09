-- índice por cliente — busca de pedidos por cliente é a query mais comum
CREATE INDEX idx_pedidos_cliente_id ON pedidos(cliente_id);

-- índice por status — filtragem por status é muito frequente
CREATE INDEX idx_pedidos_status ON pedidos(status);

-- índice por data — listagens ordenadas por data usam muito
CREATE INDEX idx_pedidos_criado_em ON pedidos(criado_em DESC);

-- índice composto — busca por cliente + status numa query só
-- mais eficiente que dois índices separados para esse padrão de busca
CREATE INDEX idx_pedidos_cliente_status ON pedidos(cliente_id, status);

-- índice no FK de itens — JOINs entre pedidos e itens ficam muito mais rápidos
CREATE INDEX idx_itens_pedido_id ON itens(pedido_id);