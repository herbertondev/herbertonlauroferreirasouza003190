package org.projetoseletivo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.projetoseletivo.domain.enums.TipoArtista;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de resposta do artista.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistaResponse {

    private Long id;
    private String nome;
    private TipoArtista tipo;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private List<AlbumResumoResponse> albuns;

    /**
     * Resumo do álbum para listagem.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlbumResumoResponse {
        private Long id;
        private String titulo;
        private Integer anoLancamento;
    }
}
