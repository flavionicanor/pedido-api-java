# pedidos-api

API REST de gestão de pedidos desenvolvida com Java 21 e Spring Boot 4.0.6

## Tecnologias
- Java 21 (Records, Sealed Classes, Pattern Matching, Virtual Threads)
- Spring Boot 4.0.6
- Spring Data JPA + H2
- Spring Security + JWT
- Lombok
- JUnit 5 + Mockito

## Como rodar
```bash
./mvnw spring-boot:run
```

## Endpoints
| Método | Endpoint | Descrição        |
|--------|----------|------------------|
| GET | /api/v1/auth/token/{clienteId} | Obter o Token    |
| POST | /api/v1/pedidos | Criar pedido     |
| GET | /api/v1/pedidos | Listar pedidos   |
| GET | /api/v1/pedidos/{id} | Buscar por ID    |
| PATCH | /api/v1/pedidos/{id}/processar | Processar pedido |
| PATCH | /api/v1/pedidos/{id}/concluir | Concluir pedido  |
| DELETE | /api/v1/pedidos/{id} | Cancelar pedido  |

## H2 Console (desenvolvimento)
http://localhost:8081/h2-console  
JDBC URL: `jdbc:h2:mem:pedidosdb`

## Obter o Token
http://localhost:8081/api/v1/auth/token/CLI001


## Criar um pedido
POST http://localhost:8081/api/v1/pedidos
Authorization: Bearer {cole_o_token_aqui}
Content-Type: application/json

{
"clienteId": "CLI001",
"itens": [
{
"produtoId": "P001",
"nome": "Notebook Dell",
"quantidade": 1,
"precoUnitario": 3500.00
},
{
"produtoId": "P002",
"nome": "Mouse Logitech",
"quantidade": 2,
"precoUnitario": 150.00
}
]
}

## Buscar o pedido criado
GET http://localhost:8081/api/v1/pedidos/{id_do_pedido}
Authorization: Bearer {token}

## Processar o pedido
PATCH http://localhost:8081/api/v1/pedidos/{id_do_pedido}/processar?responsavel=João
Authorization: Bearer {token}

## Concluir o pedido
PATCH http://localhost:8081/api/v1/pedidos/{id_do_pedido}/concluir
Authorization: Bearer {token}

## Listar com paginação
GET http://localhost:8081/api/v1/pedidos?page=0&size=5
Authorization: Bearer {token}
