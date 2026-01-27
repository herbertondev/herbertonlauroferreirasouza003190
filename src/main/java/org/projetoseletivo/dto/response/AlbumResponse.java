package org.projetoseletivo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.projetoseletivo.domain.enums.TipoArtista;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de resposta do álbum.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResponse {

    private Long id;
    private String titulo;
    private Integer anoLancamento;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private List<ArtistaResumoResponse> artistas;
    private List<ImagemResponse> imagens;

    /**
     * Resumo do artista para listagem.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArtistaResumoResponse {
        private Long id;
        private String nome;
        private TipoArtista tipo;
    }
}
