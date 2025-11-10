package ccm.owner.controller;

import ccm.owner.entitys.EvOwner;
import ccm.owner.service.JourneyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/journeys")
@RequiredArgsConstructor
public class JourneyController {

    private final JourneyService journeySyncService;

    // You will need to get the authenticated EvOwner
    // This is a placeholder for Spring Security's @AuthenticationPrincipal
    // You MUST adapt this to your auth system.
    private EvOwner getAuthenticatedEvOwner() {
        // FAKE EvOwner FOR TESTING:
        // In a real app, you'd get this from the security context
        // EvOwner Owner = (EvOwner) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        EvOwner Owner = new EvOwner();
        Owner.setId(1L); // <--- THIS IS A PLACEHOLDER
        return Owner;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadJourneyFile(@RequestParam("file") MultipartFile file) {

        // TODO: Replace with your actual authentication logic
        EvOwner Owner = getAuthenticatedEvOwner();

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try {
            journeySyncService.processJourneyFile(file.getInputStream(), Owner);
            return ResponseEntity.ok("File processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process file: " + e.getMessage());
        }
    }
}