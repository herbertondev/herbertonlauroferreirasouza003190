package org.projetoseletivo.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.projetoseletivo.domain.entity.Album;
import org.projetoseletivo.domain.entity.Artista;
import org.projetoseletivo.domain.enums.TipoArtista;
import org.projetoseletivo.dto.request.AlbumRequest;
import org.projetoseletivo.dto.response.AlbumResponse;
import org.projetoseletivo.dto.response.PaginacaoResponse;
import org.projetoseletivo.repository.AlbumRepository;
import org.projetoseletivo.repository.ArtistaRepository;
import org.projetoseletivo.websocket.AlbumNotificacaoSocket;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para AlbumService.
 */
@QuarkusTest
class AlbumServiceTest {

    @InjectMock
    AlbumRepository albumRepository;

    @InjectMock
    ArtistaRepository artistaRepository;

    @InjectMock
    MinioService minioService;

    @InjectMock
    AlbumNotificacaoSocket notificacaoSocket;

    @Inject
    AlbumService albumService;

    private Album albumExemplo;
    private Artista artistaExemplo;
    private AlbumRequest requestExemplo;

    @BeforeEach
    void setup() {
        artistaExemplo = Artista.builder()
                .id(1L)
                .nome("Linkin Park")
                .tipo(TipoArtista.BANDA)
                .criadoEm(LocalDateTime.now())
                .albuns(new HashSet<>())
                .build();

        albumExemplo = Album.builder()
                .id(1L)
                .titulo("Hybrid Theory")
                .anoLancamento(2000)
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .artistas(new HashSet<>(Arrays.asList(artistaExemplo)))
                .imagens(new java.util.ArrayList<>())
                .build();

        requestExemplo = AlbumRequest.builder()
                .titulo("Hybrid Theory")
                .anoLancamento(2000)
                .artistaIds(Arrays.asList(1L))
                .build();
    }

    // =================================================================
    // TESTES - LISTAR
    // =================================================================

    @Test
    void deveListarAlbunsComPaginacao() {
        // Arrange
        List<Album> albuns = Arrays.asList(albumExemplo);
        when(albumRepository.listarTodos(eq(0), eq(10))).thenReturn(albuns);
        when(albumRepository.contarTodos()).thenReturn(1L);

        // Act
        PaginacaoResponse<AlbumResponse> resultado = albumService.listar(null, null, null, 0, 10);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getConteudo().size());
        assertEquals(1, resultado.getTotalElementos());
        assertEquals("Hybrid Theory", resultado.getConteudo().get(0).getTitulo());
        verify(albumRepository).listarTodos(0, 10);
    }

    @Test
    void deveListarAlbunsFiltrandoPorTitulo() {
        // Arrange
        List<Album> albuns = Arrays.asList(albumExemplo);
        when(albumRepository.buscarPorTitulo(eq("Hybrid"), eq(0), eq(10))).thenReturn(albuns);
        when(albumRepository.contarPorTitulo("Hybrid")).thenReturn(1L);

        // Act
        PaginacaoResponse<AlbumResponse> resultado = albumService.listar("Hybrid", null, null, 0, 10);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getConteudo().size());
        verify(albumRepository).buscarPorTitulo("Hybrid", 0, 10);
    }

    @Test
    void deveListarAlbunsFiltrandoPorNomeArtista() {
        // Arrange
        List<Album> albuns = Arrays.asList(albumExemplo);
        when(albumRepository.buscarPorNomeArtista(eq("Linkin"), eq(0), eq(10))).thenReturn(albuns);
        when(albumRepository.contarPorNomeArtista("Linkin")).thenReturn(1L);

        // Act
        PaginacaoResponse<AlbumResponse> resultado = albumService.listar(null, "Linkin", null, 0, 10);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getConteudo().size());
        verify(albumRepository).buscarPorNomeArtista("Linkin", 0, 10);
    }

    @Test
    void deveListarAlbunsFiltrandoPorTipoArtista() {
        // Arrange
        List<Album> albuns = Arrays.asList(albumExemplo);
        when(albumRepository.buscarPorTipoArtista(eq(TipoArtista.BANDA), eq(0), eq(10))).thenReturn(albuns);
        when(albumRepository.contarPorTipoArtista(TipoArtista.BANDA)).thenReturn(1L);

        // Act
        PaginacaoResponse<AlbumResponse> resultado = albumService.listar(null, null, TipoArtista.BANDA, 0, 10);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getConteudo().size());
        verify(albumRepository).buscarPorTipoArtista(TipoArtista.BANDA, 0, 10);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverAlbuns() {
        // Arrange
        when(albumRepository.listarTodos(eq(0), eq(10))).thenReturn(List.of());
        when(albumRepository.contarTodos()).thenReturn(0L);

        // Act
        PaginacaoResponse<AlbumResponse> resultado = albumService.listar(null, null, null, 0, 10);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.getConteudo().isEmpty());
        assertEquals(0, resultado.getTotalElementos());
    }

    // =================================================================
    // TESTES - BUSCAR POR ID
    // =================================================================

    @Test
    void deveBuscarAlbumPorId() {
        // Arrange
        when(albumRepository.buscarPorIdCompleto(1L)).thenReturn(Optional.of(albumExemplo));

        // Act
        Optional<AlbumResponse> resultado = albumService.buscarPorId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Hybrid Theory", resultado.get().getTitulo());
        assertEquals(2000, resultado.get().getAnoLancamento());
    }

    @Test
    void deveRetornarVazioQuandoAlbumNaoEncontrado() {
        // Arrange
        when(albumRepository.buscarPorIdCompleto(999L)).thenReturn(Optional.empty());

        // Act
        Optional<AlbumResponse> resultado = albumService.buscarPorId(999L);

        // Assert
        assertTrue(resultado.isEmpty());
    }

    // =================================================================
    // TESTES - CRIAR
    // =================================================================

    @Test
    void deveCriarNovoAlbumSemArtistas() {
        // Arrange
        AlbumRequest requestSemArtista = AlbumRequest.builder()
                .titulo("Novo Album")
                .anoLancamento(2024)
                .build();

        doNothing().when(albumRepository).persist(any(Album.class));
        doNothing().when(notificacaoSocket).notificarNovoAlbum(any(AlbumResponse.class));

        // Act
        AlbumResponse resultado = albumService.criar(requestSemArtista);

        // Assert
        assertNotNull(resultado);
        verify(albumRepository).persist(any(Album.class));
        verify(notificacaoSocket).notificarNovoAlbum(any(AlbumResponse.class));
    }

    @Test
    void deveCriarNovoAlbumComArtistas() {
        // Arrange
        when(artistaRepository.findByIdOptional(1L)).thenReturn(Optional.of(artistaExemplo));
        doNothing().when(albumRepository).persist(any(Album.class));
        doNothing().when(notificacaoSocket).notificarNovoAlbum(any(AlbumResponse.class));

        // Act
        AlbumResponse resultado = albumService.criar(requestExemplo);

        // Assert
        assertNotNull(resultado);
        verify(artistaRepository).findByIdOptional(1L);
        verify(albumRepository).persist(any(Album.class));
    }

    @Test
    void deveLancarExcecaoQuandoArtistaNaoEncontrado() {
        // Arrange
        AlbumRequest requestComArtistaInvalido = AlbumRequest.builder()
                .titulo("Album Teste")
                .artistaIds(Arrays.asList(999L))
                .build();

        when(artistaRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> albumService.criar(requestComArtistaInvalido));

        assertTrue(exception.getMessage().contains("999"));
    }

    // =================================================================
    // TESTES - ATUALIZAR
    // =================================================================

    @Test
    void deveAtualizarAlbumExistente() {
        // Arrange
        AlbumRequest requestAtualizado = AlbumRequest.builder()
                .titulo("Hybrid Theory (Remastered)")
                .anoLancamento(2020)
                .build();

        when(albumRepository.findByIdOptional(1L)).thenReturn(Optional.of(albumExemplo));

        // Act
        Optional<AlbumResponse> resultado = albumService.atualizar(1L, requestAtualizado);

        // Assert
        assertTrue(resultado.isPresent());
    }

    @Test
    void deveRetornarVazioAoAtualizarAlbumInexistente() {
        // Arrange
        when(albumRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

        // Act
        Optional<AlbumResponse> resultado = albumService.atualizar(999L, requestExemplo);

        // Assert
        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveAtualizarArtistasDoAlbum() {
        // Arrange
        Artista novoArtista = Artista.builder()
                .id(2L)
                .nome("Mike Shinoda")
                .tipo(TipoArtista.SOLO)
                .albuns(new HashSet<>())
                .build();

        AlbumRequest requestComNovoArtista = AlbumRequest.builder()
                .titulo("Hybrid Theory")
                .artistaIds(Arrays.asList(2L))
                .build();

        when(albumRepository.findByIdOptional(1L)).thenReturn(Optional.of(albumExemplo));
        when(artistaRepository.findByIdOptional(2L)).thenReturn(Optional.of(novoArtista));

        // Act
        Optional<AlbumResponse> resultado = albumService.atualizar(1L, requestComNovoArtista);

        // Assert
        assertTrue(resultado.isPresent());
        verify(artistaRepository).findByIdOptional(2L);
    }

    @Test
    void deveRemoverTodosArtistasDoAlbum() {
        // Arrange
        AlbumRequest requestSemArtistas = AlbumRequest.builder()
                .titulo("Hybrid Theory")
                .artistaIds(List.of()) // Lista vazia remove todos
                .build();

        when(albumRepository.findByIdOptional(1L)).thenReturn(Optional.of(albumExemplo));

        // Act
        Optional<AlbumResponse> resultado = albumService.atualizar(1L, requestSemArtistas);

        // Assert
        assertTrue(resultado.isPresent());
    }

    // =================================================================
    // TESTES - REMOVER
    // =================================================================

    @Test
    void deveRemoverAlbumExistente() {
        // Arrange
        when(albumRepository.deleteById(1L)).thenReturn(true);

        // Act
        boolean resultado = albumService.remover(1L);

        // Assert
        assertTrue(resultado);
        verify(albumRepository).deleteById(1L);
    }

    @Test
    void deveRetornarFalsoAoRemoverAlbumInexistente() {
        // Arrange
        when(albumRepository.deleteById(999L)).thenReturn(false);

        // Act
        boolean resultado = albumService.remover(999L);

        // Assert
        assertFalse(resultado);
    }
}
