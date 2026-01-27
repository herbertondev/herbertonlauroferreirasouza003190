package org.projetoseletivo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma regional sincronizada do endpoint externo.
 * 
 * Estrutura:
 * - id: PK interna auto-gerada (permite histórico)
 * - idExterno: referência da API externa
 * - nome: nome da regional
 * - ativo: flag de ativação (soft delete)
 */
@Entity
@Table(name = "regional", indexes = {
        @Index(name = "idx_regional_id_externo", columnList = "id_externo"),
        @Index(name = "idx_regional_id_externo_ativo", columnList = "id_externo, ativo")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Regional {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "regional_seq")
    @SequenceGenerator(name = "regional_seq", sequenceName = "regional_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "id_externo", nullable = false)
    private Long idExterno;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "ativo", nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now();
    }
}
