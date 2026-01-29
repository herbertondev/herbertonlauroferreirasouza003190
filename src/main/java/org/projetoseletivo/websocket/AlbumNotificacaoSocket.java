package org.projetoseletivo.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.jboss.logging.Logger;
import org.projetoseletivo.dto.response.AlbumResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket para notificar clientes sobre novos álbuns cadastrados.
 */
@ServerEndpoint("/ws/albuns")
@ApplicationScoped
public class AlbumNotificacaoSocket {

    private static final Logger LOG = Logger.getLogger(AlbumNotificacaoSocket.class);

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    @Inject
    ObjectMapper objectMapper;

    @OnOpen
    public void onOpen(Session session) {
        sessions.put(session.getId(), session);
        LOG.infov("WebSocket conectado: {0}", session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session.getId());
        LOG.infov("WebSocket desconectado: {0}", session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        sessions.remove(session.getId());
        LOG.errorv(throwable, "Erro no WebSocket: {0}", session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        LOG.debugv("Mensagem recebida de {0}: {1}", session.getId(), message);
        // Mensagens do cliente podem ser usadas para ping/pong ou comandos futuros
    }

    /**
     * Notifica todos os clientes conectados sobre um novo álbum.
     */
    public void notificarNovoAlbum(AlbumResponse album) {
        try {
            String mensagem = objectMapper.writeValueAsString(new NotificacaoAlbum("NOVO_ALBUM", album));

            sessions.values().forEach(session -> {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(mensagem, result -> {
                        if (result.getException() != null) {
                            LOG.warnv("Erro ao enviar notificação para {0}: {1}",
                                    session.getId(), result.getException().getMessage());
                        }
                    });
                }
            });

            LOG.infov("Notificação de novo álbum enviada para {0} clientes", sessions.size());
        } catch (Exception e) {
            LOG.errorv(e, "Erro ao serializar notificação de álbum");
        }
    }

    /**
     * DTO interno para notificações WebSocket.
     */
    record NotificacaoAlbum(String tipo, AlbumResponse album) {
    }
}
