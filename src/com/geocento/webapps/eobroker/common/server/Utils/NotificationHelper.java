package com.geocento.webapps.eobroker.common.server.Utils;

import com.geocento.webapps.eobroker.customer.server.websockets.NotificationSocket;
import com.geocento.webapps.eobroker.supplier.server.websockets.SupplierNotificationSocket;
import com.geocento.webapps.eobroker.common.shared.entities.Company;
import com.geocento.webapps.eobroker.common.shared.entities.User;
import com.geocento.webapps.eobroker.common.shared.entities.notifications.AdminNotification;
import com.geocento.webapps.eobroker.common.shared.entities.notifications.Notification;
import com.geocento.webapps.eobroker.common.shared.entities.notifications.SupplierNotification;
import com.geocento.webapps.eobroker.customer.shared.NotificationDTO;
import com.geocento.webapps.eobroker.customer.shared.WebSocketMessage;
import com.geocento.webapps.eobroker.supplier.shared.dtos.SupplierNotificationDTO;
import com.geocento.webapps.eobroker.supplier.shared.dtos.SupplierWebSocketMessage;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import java.util.Date;

/**
 * Created by thomas on 06/07/2016.
 */
public class NotificationHelper {

    static public Logger logger = Logger.getLogger(NotificationHelper.class);

    public static void notifyCustomer(EntityManager em, User user, Notification.TYPE type, String message, String linkId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);
        notification.setLinkId(linkId);
        notification.setCreationDate(new Date());
        notification.setSent(false);
        notification.setViewed(false);
        em.persist(notification);
        try {
            WebSocketMessage webSocketMessage = new WebSocketMessage();
            webSocketMessage.setType(WebSocketMessage.TYPE.notification);
            webSocketMessage.setNotificationDTO(NotificationHelper.createNotificationDTO(notification));
            NotificationSocket.sendMessage(user.getUsername(), webSocketMessage);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void notifySupplier(EntityManager em, Company company, SupplierNotification.TYPE type, String message, String linkId) {
        SupplierNotification supplierNotification = new SupplierNotification();
        supplierNotification.setCompany(company);
        supplierNotification.setType(type);
        supplierNotification.setMessage(message);
        supplierNotification.setLinkId(linkId);
        supplierNotification.setCreationDate(new Date());
        supplierNotification.setSent(false);
        supplierNotification.setViewed(false);
        em.persist(supplierNotification);
        try {
            SupplierWebSocketMessage webSocketMessage = new SupplierWebSocketMessage();
            webSocketMessage.setType(SupplierWebSocketMessage.TYPE.notification);
            webSocketMessage.setNotificationDTO(NotificationHelper.createSupplierNotificationDTO(supplierNotification));
            SupplierNotificationSocket.sendCompanyMessage(supplierNotification.getCompany().getId(), webSocketMessage);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void notifyAdmin(EntityManager em, AdminNotification.TYPE type, String message, String linkId) {
        AdminNotification adminNotification = new AdminNotification();
        adminNotification.setType(type);
        adminNotification.setMessage(message);
        adminNotification.setLinkId(linkId);
        adminNotification.setCreationDate(new Date());
        adminNotification.setSent(false);
        adminNotification.setViewed(false);
        em.persist(adminNotification);
    }

    public static NotificationDTO createNotificationDTO(Notification notification) {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setType(notification.getType());
        notificationDTO.setMessage(notification.getMessage());
        notificationDTO.setLinkId(notification.getLinkId());
        notificationDTO.setCreationDate(notification.getCreationDate());
        return notificationDTO;
    }

    public static SupplierNotificationDTO createSupplierNotificationDTO(SupplierNotification notification) {
        SupplierNotificationDTO supplierNotificationDTO = new SupplierNotificationDTO();
        supplierNotificationDTO.setType(notification.getType());
        supplierNotificationDTO.setMessage(notification.getMessage());
        supplierNotificationDTO.setLinkId(notification.getLinkId());
        supplierNotificationDTO.setCreationDate(notification.getCreationDate());
        return supplierNotificationDTO;
    }
}
