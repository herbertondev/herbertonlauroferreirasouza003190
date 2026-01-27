package org.projetoseletivo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.projetoseletivo.domain.enums.TipoArtista;

import java.util.List;

/**
 * DTO de requisição para criar/atualizar artista.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistaRequest {

    @NotBlank(message = "Nome do artista é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    private String nome;

    @NotNull(message = "Tipo do artista é obrigatório (SOLO ou BANDA)")
    private TipoArtista tipo;

    /**
     * IDs dos álbuns a serem associados ao artista.
     */
    private List<Long> albumIds;
}
