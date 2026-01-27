package org.projetoseletivo.dto.response;

import lombok.*;

import java.util.List;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginacaoResponse<T> {

    private List<T> conteudo;
    private int pagina;
    private int tamanhoPagina;
    private long totalElementos;
    private int totalPaginas;
    private boolean primeira;
    private boolean ultima;

    public static <T> PaginacaoResponse<T> of(List<T> conteudo, int pagina, int tamanhoPagina, long totalElementos) {
        int totalPaginas = (int) Math.ceil((double) totalElementos / tamanhoPagina);
        return PaginacaoResponse.<T>builder()
                .conteudo(conteudo)
                .pagina(pagina)
                .tamanhoPagina(tamanhoPagina)
                .totalElementos(totalElementos)
                .totalPaginas(totalPaginas)
                .primeira(pagina == 0)
                .ultima(pagina >= totalPaginas - 1)
                .build();
    }
}
