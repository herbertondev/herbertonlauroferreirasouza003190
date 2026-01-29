package org.projetoseletivo.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.projetoseletivo.domain.entity.Album;
import org.projetoseletivo.domain.entity.Artista;
import org.projetoseletivo.domain.enums.Ordem;
import org.projetoseletivo.domain.enums.TipoArtista;
import org.projetoseletivo.dto.request.ArtistaRequest;
import org.projetoseletivo.dto.response.ArtistaResponse;
import org.projetoseletivo.dto.response.PaginacaoResponse;
import org.projetoseletivo.repository.AlbumRepository;
import org.projetoseletivo.repository.ArtistaRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ArtistaService.
 */
@QuarkusTest
class ArtistaServiceTest {

    @InjectMock
    ArtistaRepository artistaRepository;

    @InjectMock
    AlbumRepository albumRepository;

    @Inject
    ArtistaService artistaService;

    private Artista artistaExemplo;
    private Album albumExemplo;
    private ArtistaRequest requestExemplo;

    @BeforeEach
    void setup() {
        albumExemplo = Album.builder()
                .id(1L)
                .titulo("Hybrid Theory")
                .anoLancamento(2000)
                .criadoEm(LocalDateTime.now())
                .artistas(new HashSet<>())
                .imagens(new java.util.ArrayList<>())
                .build();

        artistaExemplo = Artista.builder()
                .id(1L)
                .nome("Serj Tankian")
                .tipo(TipoArtista.SOLO)
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .albuns(new HashSet<>())
                .build();

        requestExemplo = ArtistaRequest.builder()
                .nome("Serj Tankian")
                .tipo(TipoArtista.SOLO)
                .build();
    }

    // =================================================================
    // TESTES - LISTAR
    // =================================================================

    @Test
    void deveListarArtistasComPaginacao() {
        // Arrange
        List<Artista> artistas = Arrays.asList(artistaExemplo);
        when(artistaRepository.listarTodos(eq(Ordem.ASC), eq(0), eq(10))).thenReturn(artistas);
        when(artistaRepository.contarTodos()).thenReturn(1L);

        // Act
        PaginacaoResponse<ArtistaResponse> resultado = artistaService.listar(null, null, Ordem.ASC, 0, 10);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getConteudo().size());
        assertEquals(1, resultado.getTotalElementos());
        assertEquals("Serj Tankian", resultado.getConteudo().get(0).getNome());
        verify(artistaRepository).listarTodos(Ordem.ASC, 0, 10);
    }

    @Test
    void deveListarArtistasFiltrandoPorNome() {
        // Arrange
        List<Artista> artistas = Arrays.asList(artistaExemplo);
        when(artistaRepository.buscarPorNome(eq("Serj"), eq(Ordem.ASC), eq(0), eq(10))).thenReturn(artistas);
        when(artistaRepository.contarPorNome("Serj")).thenReturn(1L);

        // Act
        PaginacaoResponse<ArtistaResponse> resultado = artistaService.listar("Serj", null, Ordem.ASC, 0, 10);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getConteudo().size());
        verify(artistaRepository).buscarPorNome("Serj", Ordem.ASC, 0, 10);
    }

    @Test
    void deveListarArtistasFiltrandoPorTipo() {
        // Arrange
        List<Artista> artistas = Arrays.asList(artistaExemplo);
        when(artistaRepository.buscarPorTipo(eq(TipoArtista.SOLO), eq(Ordem.ASC), eq(0), eq(10))).thenReturn(artistas);
        when(artistaRepository.contarPorTipo(TipoArtista.SOLO)).thenReturn(1L);

        // Act
        PaginacaoResponse<ArtistaResponse> resultado = artistaService.listar(null, TipoArtista.SOLO, Ordem.ASC, 0, 10);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getConteudo().size());
        verify(artistaRepository).buscarPorTipo(TipoArtista.SOLO, Ordem.ASC, 0, 10);
    }

    @Test
    void deveListarArtistasComOrdenacaoDescendente() {
        // Arrange
        List<Artista> artistas = Arrays.asList(artistaExemplo);
        when(artistaRepository.listarTodos(eq(Ordem.DESC), eq(0), eq(10))).thenReturn(artistas);
        when(artistaRepository.contarTodos()).thenReturn(1L);

        // Act
        PaginacaoResponse<ArtistaResponse> resultado = artistaService.listar(null, null, Ordem.DESC, 0, 10);

        // Assert
        assertNotNull(resultado);
        verify(artistaRepository).listarTodos(Ordem.DESC, 0, 10);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverArtistas() {
        // Arrange
        when(artistaRepository.listarTodos(eq(Ordem.ASC), eq(0), eq(10))).thenReturn(List.of());
        when(artistaRepository.contarTodos()).thenReturn(0L);

        // Act
        PaginacaoResponse<ArtistaResponse> resultado = artistaService.listar(null, null, Ordem.ASC, 0, 10);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.getConteudo().isEmpty());
        assertEquals(0, resultado.getTotalElementos());
    }

    // =================================================================
    // TESTES - BUSCAR POR ID
    // =================================================================

    @Test
    void deveBuscarArtistaPorId() {
        // Arrange
        when(artistaRepository.buscarPorIdComAlbuns(1L)).thenReturn(Optional.of(artistaExemplo));

        // Act
        Optional<ArtistaResponse> resultado = artistaService.buscarPorId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Serj Tankian", resultado.get().getNome());
        assertEquals(TipoArtista.SOLO, resultado.get().getTipo());
    }

    @Test
    void deveRetornarVazioQuandoArtistaNaoEncontrado() {
        // Arrange
        when(artistaRepository.buscarPorIdComAlbuns(999L)).thenReturn(Optional.empty());

        // Act
        Optional<ArtistaResponse> resultado = artistaService.buscarPorId(999L);

        // Assert
        assertTrue(resultado.isEmpty());
    }

    // =================================================================
    // TESTES - CRIAR
    // =================================================================

    @Test
    void deveCriarNovoArtistaSemAlbuns() {
        // Arrange
        doNothing().when(artistaRepository).persist(any(Artista.class));

        // Act
        ArtistaResponse resultado = artistaService.criar(requestExemplo);

        // Assert
        assertNotNull(resultado);
        verify(artistaRepository).persist(any(Artista.class));
    }

    @Test
    void deveCriarNovoArtistaComAlbuns() {
        // Arrange
        ArtistaRequest requestComAlbuns = ArtistaRequest.builder()
                .nome("Linkin Park")
                .tipo(TipoArtista.BANDA)
                .albumIds(Arrays.asList(1L))
                .build();

        when(albumRepository.findByIdOptional(1L)).thenReturn(Optional.of(albumExemplo));
        doNothing().when(artistaRepository).persist(any(Artista.class));

        // Act
        ArtistaResponse resultado = artistaService.criar(requestComAlbuns);

        // Assert
        assertNotNull(resultado);
        verify(albumRepository).findByIdOptional(1L);
        verify(artistaRepository).persist(any(Artista.class));
    }

    @Test
    void deveIgnorarAlbunsInexistentesAoCriar() {
        // Arrange - diferente do AlbumService que lança exceção, ArtistaService ignora
        // álbuns não encontrados
        ArtistaRequest requestComAlbumInvalido = ArtistaRequest.builder()
                .nome("Artista Teste")
                .tipo(TipoArtista.SOLO)
                .albumIds(Arrays.asList(999L))
                .build();

        when(albumRepository.findByIdOptional(999L)).thenReturn(Optional.empty());
        doNothing().when(artistaRepository).persist(any(Artista.class));

        // Act - não lança exceção, apenas ignora
        ArtistaResponse resultado = artistaService.criar(requestComAlbumInvalido);

        // Assert
        assertNotNull(resultado);
        verify(artistaRepository).persist(any(Artista.class));
    }

    // =================================================================
    // TESTES - ATUALIZAR
    // =================================================================

    @Test
    void deveAtualizarArtistaExistente() {
        // Arrange
        ArtistaRequest requestAtualizado = ArtistaRequest.builder()
                .nome("Serj Tankian Updated")
                .tipo(TipoArtista.SOLO)
                .build();

        when(artistaRepository.findByIdOptional(1L)).thenReturn(Optional.of(artistaExemplo));

        // Act
        Optional<ArtistaResponse> resultado = artistaService.atualizar(1L, requestAtualizado);

        // Assert
        assertTrue(resultado.isPresent());
    }

    @Test
    void deveRetornarVazioAoAtualizarArtistaInexistente() {
        // Arrange
        when(artistaRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

        // Act
        Optional<ArtistaResponse> resultado = artistaService.atualizar(999L, requestExemplo);

        // Assert
        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveAtualizarAlbunsDoArtista() {
        // Arrange
        Album novoAlbum = Album.builder()
                .id(2L)
                .titulo("Meteora")
                .artistas(new HashSet<>())
                .imagens(new java.util.ArrayList<>())
                .build();

        ArtistaRequest requestComNovoAlbum = ArtistaRequest.builder()
                .nome("Linkin Park")
                .tipo(TipoArtista.BANDA)
                .albumIds(Arrays.asList(2L))
                .build();

        when(artistaRepository.findByIdOptional(1L)).thenReturn(Optional.of(artistaExemplo));
        when(albumRepository.findByIdOptional(2L)).thenReturn(Optional.of(novoAlbum));

        // Act
        Optional<ArtistaResponse> resultado = artistaService.atualizar(1L, requestComNovoAlbum);

        // Assert
        assertTrue(resultado.isPresent());
        verify(albumRepository).findByIdOptional(2L);
    }

    @Test
    void deveRemoverTodosAlbunsDoArtista() {
        // Arrange
        ArtistaRequest requestSemAlbuns = ArtistaRequest.builder()
                .nome("Serj Tankian")
                .tipo(TipoArtista.SOLO)
                .albumIds(List.of()) // Lista vazia remove todos
                .build();

        when(artistaRepository.findByIdOptional(1L)).thenReturn(Optional.of(artistaExemplo));

        // Act
        Optional<ArtistaResponse> resultado = artistaService.atualizar(1L, requestSemAlbuns);

        // Assert
        assertTrue(resultado.isPresent());
    }

    @Test
    void deveAlterarTipoDoArtista() {
        // Arrange
        ArtistaRequest requestMudarTipo = ArtistaRequest.builder()
                .nome("Serj Tankian")
                .tipo(TipoArtista.BANDA) // Mudou de SOLO para BANDA
                .build();

        when(artistaRepository.findByIdOptional(1L)).thenReturn(Optional.of(artistaExemplo));

        // Act
        Optional<ArtistaResponse> resultado = artistaService.atualizar(1L, requestMudarTipo);

        // Assert
        assertTrue(resultado.isPresent());
    }

    // =================================================================
    // TESTES - REMOVER
    // =================================================================

    @Test
    void deveRemoverArtistaExistente() {
        // Arrange
        when(artistaRepository.deleteById(1L)).thenReturn(true);

        // Act
        boolean resultado = artistaService.remover(1L);

        // Assert
        assertTrue(resultado);
        verify(artistaRepository).deleteById(1L);
    }

    @Test
    void deveRetornarFalsoAoRemoverArtistaInexistente() {
        // Arrange
        when(artistaRepository.deleteById(999L)).thenReturn(false);

        // Act
        boolean resultado = artistaService.remover(999L);

        // Assert
        assertFalse(resultado);
    }
}
