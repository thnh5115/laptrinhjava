package ccm.owner.journey.controller;

import ccm.owner.journey.dto.request.JourneySubmissionRequest;
import ccm.owner.journey.dto.response.JourneyResponse;
import ccm.owner.journey.service.OwnerJourneyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/owner/journeys")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('EV_OWNER')")
@Tag(name = "EV Owner - Journeys", description = "Journey submission and management for EV Owners")
/**
 * Controller for EV Owner journey operations
 */
public class OwnerJourneyController {

    private final OwnerJourneyService journeyService;
    private final ObjectMapper objectMapper;

    @Operation(
            summary = "Submit Journey (JSON)",
            description = "Submit a new EV journey for carbon credit generation using JSON payload"
    )
    @PostMapping
    public ResponseEntity<JourneyResponse> submitJourney(
            @Valid @RequestBody JourneySubmissionRequest request) {

        log.info("Received journey submission: distance={} km", request.getDistanceKm());

        JourneyResponse response = journeyService.submitJourney(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Submit Journey (JSON File)",
            description = "Submit a new EV journey by uploading a JSON file containing journey data"
    )
    @PostMapping("/upload")
    public ResponseEntity<JourneyResponse> submitJourneyFromFile(
            @RequestParam("file") MultipartFile file) {

        log.info("Received journey submission file: {}", file.getOriginalFilename());

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!"application/json".equals(file.getContentType()) &&
                !file.getOriginalFilename().endsWith(".json")) {
            throw new IllegalArgumentException("File must be a JSON file");
        }

        try {
            // Parse JSON file
            JourneySubmissionRequest request = objectMapper.readValue(
                    file.getInputStream(),
                    JourneySubmissionRequest.class
            );

            // Validate the parsed request
            // (Spring validation won't automatically apply to parsed objects)
            if (request.getJourneyDate() == null ||
                    request.getStartLocation() == null ||
                    request.getEndLocation() == null ||
                    request.getDistanceKm() == null ||
                    request.getEnergyUsedKwh() == null) {
                throw new IllegalArgumentException("Missing required fields in JSON file");
            }

            log.info("Parsed journey from file: distance={} km", request.getDistanceKm());

            // Submit journey
            JourneyResponse response = journeyService.submitJourney(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IOException e) {
            log.error("Failed to parse JSON file: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JSON file format: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Get My Journeys",
            description = "Retrieve all journeys submitted by the authenticated EV Owner"
    )
    @GetMapping
    public ResponseEntity<List<JourneyResponse>> getMyJourneys() {
        List<JourneyResponse> journeys = journeyService.getMyJourneys();
        return ResponseEntity.ok(journeys);
    }

    @Operation(
            summary = "Get Journey by ID",
            description = "Retrieve details of a specific journey"
    )
    @GetMapping("/{id}")
    public ResponseEntity<JourneyResponse> getJourneyById(@PathVariable Long id) {
        JourneyResponse journey = journeyService.getJourneyById(id);
        return ResponseEntity.ok(journey);
    }
}