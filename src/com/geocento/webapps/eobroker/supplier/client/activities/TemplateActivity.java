package com.geocento.webapps.eobroker.supplier.client.activities;

import com.geocento.webapps.eobroker.common.shared.entities.orders.RequestDTO;
import com.geocento.webapps.eobroker.customer.shared.NotificationDTO;
import com.geocento.webapps.eobroker.supplier.client.ClientFactory;
import com.geocento.webapps.eobroker.supplier.client.events.LogOut;
import com.geocento.webapps.eobroker.supplier.client.events.LogOutHandler;
import com.geocento.webapps.eobroker.supplier.client.events.SupplierNotifications;
import com.geocento.webapps.eobroker.supplier.client.events.SupplierNotificationsHandler;
import com.geocento.webapps.eobroker.supplier.client.services.ServicesUtil;
import com.geocento.webapps.eobroker.supplier.client.utils.NotificationMonitoring;
import com.geocento.webapps.eobroker.supplier.client.views.TemplateView;
import com.geocento.webapps.eobroker.supplier.shared.dtos.SupplierNotificationDTO;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import java.util.List;

/**
 * Created by thomas on 29/06/2016.
 */
public abstract class TemplateActivity extends AbstractApplicationActivity {

    private TemplateView templateView;

    private List<SupplierNotificationDTO> notifications = null;

    public TemplateActivity(ClientFactory clientFactory) {
        super(clientFactory);
    }

    public void setTemplateView(TemplateView templateView) {
        this.templateView = templateView;
        // load notifications
        if(notifications == null) {
            loadUserNotifications();
        } else {
            templateView.setNotifications(notifications);
        }
    }

    @Override
    protected void bind() {
        activityEventBus.addHandler(LogOut.TYPE, new LogOutHandler() {
            @Override
            public void onLogOut(LogOut event) {
                REST.withCallback(new MethodCallback<Void>() {
                    @Override
                    public void onFailure(Method method, Throwable exception) {

                    }

                    @Override
                    public void onSuccess(Method method, Void response) {
                        History.newItem("", false);
                        Window.Location.reload();
                    }
                }).call(ServicesUtil.loginService).signout();
            }
        });
    }

    private void loadUserNotifications() {
        try {
            REST.withCallback(new MethodCallback<List<SupplierNotificationDTO>>() {
                @Override
                public void onFailure(Method method, Throwable exception) {

                }

                @Override
                public void onSuccess(Method method, List<SupplierNotificationDTO> notificationDTOs) {
                    notifications = notificationDTOs;
                    templateView.setNotifications(notificationDTOs);
                }
            }).call(ServicesUtil.assetsService).getNotifications();
        } catch (RequestException e) {
        }
    }

}
