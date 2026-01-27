package org.projetoseletivo.dto.response;

import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImagemResponse {

    private Long id;
    private String bucket;
    private String objectKey;
    private String contentType;
    private String url;
    private LocalDateTime criadoEm;
}
