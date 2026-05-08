package com.flaviorosa.pedidos_api.application.service;

import com.flaviorosa.pedidos_api.domain.exception.PedidoNaoEncontradoException;
import com.flaviorosa.pedidos_api.domain.exception.TransicaoEstadoInvalidaException;
import com.flaviorosa.pedidos_api.infrastructure.persistence.entity.PedidoEntity;
import com.flaviorosa.pedidos_api.infrastructure.persistence.entity.StatusPedidoEnum;
import com.flaviorosa.pedidos_api.infrastructure.persistence.mapper.PedidoMapper;
import com.flaviorosa.pedidos_api.infrastructure.persistence.repository.PedidoRepository;
import com.flaviorosa.pedidos_api.infrastructure.web.dto.CriarPedidoRequest;
import com.flaviorosa.pedidos_api.infrastructure.web.dto.ItemRequest;
import com.flaviorosa.pedidos_api.infrastructure.web.dto.PedidoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// inicializa os mocks sem subir o Spring
@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock
    private PedidoRepository repository;

    @Mock
    private PedidoMapper mapper;

    // injeta os mocks acima no service
    @InjectMocks
    private PedidoService service;

    private CriarPedidoRequest requestValido;
    private PedidoEntity entityMock;

    @BeforeEach
    void setUp() {
        requestValido = new CriarPedidoRequest(
                "CLI001",
                List.of(new ItemRequest("P001", "Notebook", 1, new BigDecimal("3500.00")))
        );

        entityMock = new PedidoEntity();
        entityMock.setId("id-teste");
        entityMock.setClienteId("CLI001");
        entityMock.setStatus(StatusPedidoEnum.AGUARDANDO);
        entityMock.setCriadoEm(LocalDateTime.now());
    }

    @Test
    void deveCriarPedidoComSucesso() {
        when(mapper.toEntity(any())).thenReturn(entityMock);
        when(repository.save(any())).thenReturn(entityMock);

        PedidoResponse response = service.criar(requestValido);

        assertThat(response).isNotNull();
        assertThat(response.clienteId()).isEqualTo("CLI001");
        assertThat(response.total()).isEqualByComparingTo("3500.00");

        verify(repository, times(1)).save(any());

    }

    @Test
    void deveLancarExcecaoQuandoPedidoNaoEncontrado() {
        when(repository.findWithItensById("id-inexistente"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId("id-inexistente"))
                .isInstanceOf(PedidoNaoEncontradoException.class)
                .hasMessageContaining("id-inexistente");
    }

    @Test
    void deveLancarExcecaoAoCancelarPedidoConcluido() {
        // monta um entity com status CONCLUIDO
        entityMock.setStatus(StatusPedidoEnum.CONCLUIDO);
        entityMock.setItens(new ArrayList<>());

        // configura o mapper para retornar um domínio com status Concluido
        when(repository.findWithItensById("id-teste"))
                .thenReturn(Optional.of(entityMock));
        when(mapper.toDomain(any())).thenCallRealMethod();

        assertThatThrownBy(() -> service.cancelar("id-teste", "desistência"))
                .isInstanceOf(TransicaoEstadoInvalidaException.class);
    }

}
