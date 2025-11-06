package ccm.buyer.service;

import ccm.buyer.entity.Notification;
import java.util.List;

public interface NotificationService {
    void notifyBuyer(Long buyerId, String message);
    List<Notification> listNotifications(Long buyerId);
}
