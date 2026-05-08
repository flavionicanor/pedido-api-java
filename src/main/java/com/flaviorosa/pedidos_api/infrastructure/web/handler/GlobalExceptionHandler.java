package com.flaviorosa.pedidos_api.infrastructure.web.handler;

import com.flaviorosa.pedidos_api.domain.exception.PedidoNaoEncontradoException;
import com.flaviorosa.pedidos_api.domain.exception.TransicaoEstadoInvalidaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice // intercepta exceções de todos os controllers
public class GlobalExceptionHandler {

    // Estrutura padrão de resposta de erro
    public record ErroResponse(
            int Status,
            String erro,
            String mensagem,
            LocalDateTime timestamp,
            List<String> detalhes // para erros de validação
    ){
        // Construtor sem detalhes — para erros simples
        public ErroResponse(int status, String erro, String mensagem) {
            this(status, erro, mensagem, LocalDateTime.now(), List.of());
        }

    }

    // 404 — Pedido não encontrado
    @ExceptionHandler(PedidoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handlePedidoNaoEncontrado(PedidoNaoEncontradoException ex){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErroResponse(404, "Não encontrado", ex.getMessage()));
    }

    // 422 — Transição de estado inválida
    @ExceptionHandler(TransicaoEstadoInvalidaException.class)
    public ResponseEntity<ErroResponse> handleTransicaoInvalida(TransicaoEstadoInvalidaException ex){
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErroResponse(422,"Estado inválido", ex.getMessage()));
    }

    // 400 — Erros de validação do Bean Validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidacao(
            MethodArgumentNotValidException ex){

        // Coleta todos os erros de validação
        List<String> detalhes = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField()+" : "+e.getDefaultMessage())
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErroResponse(
                        400,
                        "Dados inválidos",
                        "Verifique os campos",
                        LocalDateTime.now(),
                        detalhes));

    }

    // 500 — Qualquer erro não tratado (safety net)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleGenerico(Exception ex) {
        log.error("Erro não tratado: {}", ex.getMessage(), ex); // ← adiciona ex.getMessage()

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErroResponse(500, "Erro interno",
                        ex.getMessage())); // ← temporário: mostra o erro real
    }
}
