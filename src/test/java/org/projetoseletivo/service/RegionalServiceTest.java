package org.projetoseletivo.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.projetoseletivo.client.RegionalClient;
import org.projetoseletivo.domain.entity.Regional;
import org.projetoseletivo.repository.RegionalRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RegionalService.
 * 
 * Testa a lógica de sincronização:
 * 1) Novo no endpoint → inserir
 * 2) Ausente no endpoint → inativar
 * 3) Atributo alterado → inativar antigo e criar novo registro
 */
@QuarkusTest
class RegionalServiceTest {

    @InjectMock
    @RestClient
    RegionalClient regionalClient;

    @InjectMock
    RegionalRepository regionalRepository;

    @Inject
    RegionalService regionalService;

    @Test
    void deveInserirNovaRegional() {
        // Arrange - API retorna regional nova
        List<RegionalClient.RegionalExterna> externas = Arrays.asList(
                new RegionalClient.RegionalExterna(1L, "Regional Nova"));
        when(regionalClient.listar()).thenReturn(externas);
        when(regionalRepository.buscarAtivas()).thenReturn(Collections.emptyList());

        // Act
        RegionalService.SincronizacaoResultado resultado = regionalService.sincronizar();

        // Assert - deve inserir
        assertEquals(1, resultado.inseridos());
        assertEquals(0, resultado.inativados());
        assertEquals(0, resultado.atualizados());
        verify(regionalRepository).persist(any(Regional.class));
    }

    @Test
    void deveInativarRegionalAusente() {
        // Arrange - Regional existe no banco mas não na API
        Regional regionalExistente = Regional.builder()
                .id(100L)
                .idExterno(1L)
                .nome("Regional Antiga")
                .ativo(true)
                .build();

        when(regionalClient.listar()).thenReturn(Collections.emptyList());
        when(regionalRepository.buscarAtivas()).thenReturn(Arrays.asList(regionalExistente));

        // Act
        RegionalService.SincronizacaoResultado resultado = regionalService.sincronizar();

        // Assert - deve inativar
        assertEquals(0, resultado.inseridos());
        assertEquals(1, resultado.inativados());
        assertEquals(0, resultado.atualizados());
        verify(regionalRepository).inativarPorIdExterno(1L);
    }

    @Test
    void deveCriarNovoRegistroQuandoNomeAlterado() {
        // Arrange - API retorna nome diferente
        Regional regionalExistente = Regional.builder()
                .id(100L)
                .idExterno(1L)
                .nome("Nome Antigo")
                .ativo(true)
                .build();

        List<RegionalClient.RegionalExterna> externas = Arrays.asList(
                new RegionalClient.RegionalExterna(1L, "Nome Novo"));

        when(regionalClient.listar()).thenReturn(externas);
        when(regionalRepository.buscarAtivas()).thenReturn(Arrays.asList(regionalExistente));

        // Act
        RegionalService.SincronizacaoResultado resultado = regionalService.sincronizar();

        // Assert - deve inativar antigo e criar novo
        assertEquals(0, resultado.inseridos());
        assertEquals(0, resultado.inativados());
        assertEquals(1, resultado.atualizados());
        verify(regionalRepository).inativarPorIdExterno(1L);
        verify(regionalRepository).persist(any(Regional.class));
    }

    @Test
    void deveListarRegionaisAtivas() {
        // Arrange
        List<Regional> ativas = Arrays.asList(
                Regional.builder().id(100L).idExterno(1L).nome("Regional 1").ativo(true).build(),
                Regional.builder().id(101L).idExterno(2L).nome("Regional 2").ativo(true).build());
        when(regionalRepository.buscarAtivas()).thenReturn(ativas);

        // Act
        List<Regional> resultado = regionalService.listarAtivas();

        // Assert
        assertEquals(2, resultado.size());
    }
}
