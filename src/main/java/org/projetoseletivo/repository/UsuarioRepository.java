package org.projetoseletivo.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.projetoseletivo.domain.entity.Usuario;

import java.util.Optional;


@ApplicationScoped
public class UsuarioRepository implements PanacheRepository<Usuario> {


    public Optional<Usuario> buscarPorUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public boolean existePorUsername(String username) {
        return count("username", username) > 0;
    }
}
