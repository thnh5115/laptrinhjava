package ccm.buyer.controller;

import ccm.buyer.entity.Notification;
import ccm.buyer.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/buyer/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping
    public ResponseEntity<List<Notification>> list(@RequestParam Long buyerId) {
        return ResponseEntity.ok(service.listNotifications(buyerId));
    }
}
