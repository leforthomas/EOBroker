package com.geocento.webapps.eobroker.customer.server.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geocento.webapps.eobroker.common.server.EMF;
import com.geocento.webapps.eobroker.common.server.UserSession;
import com.geocento.webapps.eobroker.common.server.websockets.BaseCustomConfigurator;
import com.geocento.webapps.eobroker.common.server.websockets.BaseNotificationSocket;
import com.geocento.webapps.eobroker.common.shared.entities.Conversation;
import com.geocento.webapps.eobroker.common.shared.utils.ListUtil;
import com.geocento.webapps.eobroker.customer.shared.WebSocketMessage;
import com.geocento.webapps.eobroker.supplier.server.websockets.SupplierNotificationSocket;
import com.google.gwt.http.client.RequestException;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/notifications", configurator = BaseCustomConfigurator.class)
public class NotificationSocket extends BaseNotificationSocket {

    static Logger logger = Logger.getLogger(NotificationSocket.class);

    static private ConcurrentHashMap<String, List<Session>> userSessions = new ConcurrentHashMap<String, List<Session>>();

    public NotificationSocket() {
        logger.info("Starting websocket handler");
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        // set maximum time out to 30 minutes
        session.setMaxIdleTimeout(30 * 60 * 1000L);
        UserSession userSession = getUserSession();
        if(userSession == null) {
            sendLoggedOut(session);
            throw new IOException("Not signed in");
        }
        addUserSession(userSession.getUserName(), session);
        logger.info("Open session for user " + userSession.getUserName());
    }

    private void addUserSession(String userName, Session session) {
        super.addHttpSession(session);
        List<Session> sessions = userSessions.get(userName);
        if(sessions == null) {
            sessions = new ArrayList<Session>();
            userSessions.put(userName, sessions);
        }
        sessions.add(session);
    }

    private void removeUserSession(String userName, Session session) {
        super.removeHttpSession(session);
        List<Session> sessions = userSessions.get(userName);
        if(sessions == null) {
            return;
        }
        sessions.remove(session);
    }

    @OnMessage
    public String echo(String message, Session session) {
        if(message != null && message.startsWith("follow:")) {
            String conversationId = message.replace("follow:", "");
            boolean online = subscribeConversation(conversationId, session);
            WebSocketMessage webSocketMessage = new WebSocketMessage();
            webSocketMessage.setType(WebSocketMessage.TYPE.conversationOnline);
            webSocketMessage.setDestination(conversationId);
            webSocketMessage.setConversationStatus(online);
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.writeValueAsString(webSocketMessage);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private boolean subscribeConversation(String conversationId, Session session) {
        EntityManager em = EMF.get().createEntityManager();
        try {
            Conversation conversation = em.find(Conversation.class, conversationId);
            if(conversation == null) {
                throw new RequestException("Could not find conversation with id " + conversationId);
            }
            return SupplierNotificationSocket.subscribeConversation(conversation, session);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            em.close();
        }
        return false;
    }

    @OnError
    public void onError(Throwable t) {
        handleError(t);
    }

    @OnClose
    public void onClose(Session session) {
        UserSession userSession = getUserSession();
        if(userSession == null) {
            // TODO - find a strategy
            // we need a parallel process to remove sessions when the http session has expired
            return;
        }
        removeUserSession(userSession.getUserName(), session);
        logger.info("Close session for user " + userSession.getUserName());
    }

    static public void sendMessage(String userName, WebSocketMessage webSocketMessage) throws JsonProcessingException {
        List<Session> sessions = userSessions.get(userName);
        if (sessions == null) {
            return;
        }
        sendMessageSessions(sessions, webSocketMessage);
    }

    static public void sendMessageSession(Session session, WebSocketMessage webSocketMessage) throws JsonProcessingException {
        sendMessageSessions(ListUtil.toList(session), webSocketMessage);
    }

    static public void sendMessageSessions(List<Session> sessions, WebSocketMessage webSocketMessage) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(webSocketMessage);
        for(Session session : sessions) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                // TODO - remove session?

            }
        }
    }

    static public void sendLogout(String sessionID) throws JsonProcessingException {
        WebSocketMessage webSocketMessage = new WebSocketMessage();
        webSocketMessage.setType(WebSocketMessage.TYPE.logout);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(webSocketMessage);
        sendHttpSessionMessage(sessionID, message);
    }

    private void sendLoggedOut(Session session) throws IOException {
        WebSocketMessage webSocketMessage = new WebSocketMessage();
        webSocketMessage.setType(WebSocketMessage.TYPE.logout);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(webSocketMessage);
        session.getBasicRemote().sendText(message);
    }

    public static void broadcastTyping(List<Session> sessions, String conversationId) throws JsonProcessingException {
        WebSocketMessage webSocketMessage = new WebSocketMessage();
        webSocketMessage.setType(WebSocketMessage.TYPE.conversationTyping);
        webSocketMessage.setDestination(conversationId);
        sendMessageSessions(sessions, webSocketMessage);
    }
}
