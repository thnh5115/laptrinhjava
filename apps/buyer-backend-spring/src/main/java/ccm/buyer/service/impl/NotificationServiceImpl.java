package ccm.buyer.service.impl;

import ccm.buyer.entity.Notification;
import ccm.buyer.repository.NotificationRepository;
import ccm.buyer.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repo;

    @Override
    public void notifyBuyer(Long buyerId, String message) {
        Notification n = Notification.builder()
                .buyerId(buyerId)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        repo.save(n);
    }

    @Override
    public List<Notification> listNotifications(Long buyerId) {
        return repo.findAll().stream()
                .filter(n -> n.getBuyerId().equals(buyerId))
                .toList();
    }
}
