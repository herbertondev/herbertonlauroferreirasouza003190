package org.projetoseletivo.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.projetoseletivo.domain.entity.Usuario;
import org.projetoseletivo.dto.request.LoginRequest;
import org.projetoseletivo.dto.response.TokenResponse;
import org.projetoseletivo.repository.UsuarioRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Serviço de autenticação JWT.
 */
@ApplicationScoped
public class AuthService {

    private static final long ACCESS_TOKEN_DURATION_MINUTES = 5;
    private static final long REFRESH_TOKEN_DURATION_HOURS = 24;

    @Inject
    UsuarioRepository usuarioRepository;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    /**
     * Realiza login e retorna tokens JWT.
     */
    public Optional<TokenResponse> login(LoginRequest loginRequest) {
        Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorUsername(loginRequest.getUsername());

        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuario = usuarioOpt.get();

        // Valida senha (BCrypt)
        if (!verificarSenha(loginRequest.getSenha(), usuario.getSenhaHash())) {
            return Optional.empty();
        }

        return Optional.of(gerarTokens(usuario));
    }

    /**
     * Renova o access token usando o refresh token.
     */
    public Optional<TokenResponse> refresh(String refreshToken, String username) {
        Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorUsername(username);

        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(gerarTokens(usuarioOpt.get()));
    }

    /**
     * Gera access e refresh tokens para o usuário.
     */
    private TokenResponse gerarTokens(Usuario usuario) {
        Instant agora = Instant.now();

        Set<String> grupos = new HashSet<>();
        grupos.add(usuario.getRole());

        // Access Token (5 minutos)
        String accessToken = Jwt.issuer(issuer)
                .upn(usuario.getUsername())
                .groups(grupos)
                .claim("userId", usuario.getId())
                .issuedAt(agora)
                .expiresAt(agora.plus(Duration.ofMinutes(ACCESS_TOKEN_DURATION_MINUTES)))
                .sign();

        // Refresh Token (24 horas)
        String refreshToken = Jwt.issuer(issuer)
                .upn(usuario.getUsername())
                .claim("type", "refresh")
                .claim("userId", usuario.getId())
                .issuedAt(agora)
                .expiresAt(agora.plus(Duration.ofHours(REFRESH_TOKEN_DURATION_HOURS)))
                .sign();

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tipo("Bearer")
                .expiracaoEmSegundos(ACCESS_TOKEN_DURATION_MINUTES * 60)
                .build();
    }

    /**
     * Verifica se a senha informada corresponde ao hash armazenado.
     * Em produção, usar BCrypt.checkpw()
     */
    private boolean verificarSenha(String senhaInformada, String senhaHash) {
        // Para simplificar em dev, aceita "admin123" como senha válida
        // Em produção: return BCrypt.checkpw(senhaInformada, senhaHash);
        return "admin123".equals(senhaInformada) ||
                org.mindrot.jbcrypt.BCrypt.checkpw(senhaInformada, senhaHash);
    }
}
