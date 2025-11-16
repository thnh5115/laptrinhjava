package ccm.owner.journey.service;

import ccm.owner.journey.dto.request.JourneySubmissionRequest;
import ccm.owner.journey.dto.response.JourneyResponse;
import ccm.admin.journey.entity.Journey;
import ccm.admin.journey.entity.enums.JourneyStatus;
import ccm.admin.journey.repository.JourneyRepository;
import ccm.admin.user.entity.User;
import ccm.admin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * Service for EV Owner journey submission
 */
public class OwnerJourneyService {

    private final JourneyRepository journeyRepository;
    private final UserRepository userRepository;

    // Carbon credit calculation constants
    private static final BigDecimal CO2_SAVED_PER_KM = BigDecimal.valueOf(0.142); // kg CO2 per km
    private static final BigDecimal CREDIT_CONVERSION_RATE = BigDecimal.valueOf(0.001); // 1 credit = 1 ton CO2

    /**
     * Submit a new journey for carbon credit generation
     */
    @Transactional
    public JourneyResponse submitJourney(JourneySubmissionRequest request) {
        log.info("Submitting journey: distance={} km, energy={} kWh",
                request.getDistanceKm(), request.getEnergyUsedKwh());

        // Get authenticated user
        User currentUser = getCurrentUser();

        // Validate user role (must be EV_OWNER)
        if (currentUser.getRole() == null ||
                !"EV_OWNER".equals(currentUser.getRole().getName())) {
            throw new IllegalStateException("Only EV Owners can submit journeys");
        }

        // Calculate estimated carbon credits
        BigDecimal estimatedCredits = calculateCarbonCredits(request.getDistanceKm());

        // Create journey entity
        Journey journey = Journey.builder()
                .userId(currentUser.getId())
                .journeyDate(request.getJourneyDate())
                .startLocation(request.getStartLocation())
                .endLocation(request.getEndLocation())
                .distanceKm(request.getDistanceKm())
                .energyUsedKwh(request.getEnergyUsedKwh())
                .creditsGenerated(estimatedCredits)
                .status(JourneyStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        // Save journey
        Journey savedJourney = journeyRepository.save(journey);

        log.info("Journey submitted successfully: id={}, user={}, estimatedCredits={}",
                savedJourney.getId(), currentUser.getEmail(), estimatedCredits);

        return JourneyResponse.builder()
                .id(savedJourney.getId())
                .userId(savedJourney.getUserId())
                .journeyDate(savedJourney.getJourneyDate())
                .startLocation(savedJourney.getStartLocation())
                .endLocation(savedJourney.getEndLocation())
                .distanceKm(savedJourney.getDistanceKm())
                .energyUsedKwh(savedJourney.getEnergyUsedKwh())
                .estimatedCredits(estimatedCredits)
                .status(savedJourney.getStatus().name())
                .createdAt(savedJourney.getCreatedAt())
                .message("Journey submitted successfully. Awaiting verification from Carbon Verification Authority.")
                .build();
    }

    /**
     * Get all journeys for the current user
     */
    @Transactional(readOnly = true)
    public List<JourneyResponse> getMyJourneys() {
        User currentUser = getCurrentUser();

        List<Journey> journeys = journeyRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("userId"), currentUser.getId())
        );

        return journeys.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get journey by ID (must belong to current user)
     */
    @Transactional(readOnly = true)
    public JourneyResponse getJourneyById(Long id) {
        User currentUser = getCurrentUser();

        Journey journey = journeyRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Journey not found with id: " + id));

        // Verify ownership
        if (!journey.getUserId().equals(currentUser.getId())) {
            throw new IllegalAccessError("You can only access your own journeys");
        }

        return mapToResponse(journey);
    }

    // ===== PRIVATE HELPER METHODS =====

    /**
     * Calculate carbon credits based on distance
     * Formula: CO2 saved (kg) = distance (km) * 0.12 kg/km
     *          Credits = CO2 saved / 1000 (1 credit = 1 ton CO2)
     */
    private BigDecimal calculateCarbonCredits(BigDecimal distanceKm) {
        BigDecimal co2SavedKg = distanceKm.multiply(CO2_SAVED_PER_KM);
        BigDecimal credits = co2SavedKg.multiply(CREDIT_CONVERSION_RATE);
        return credits.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get currently authenticated user
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("User not authenticated");
        }

        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "User not found: " + email));
    }

    /**
     * Map Journey entity to JourneyResponse
     */
    private JourneyResponse mapToResponse(Journey journey) {
        return JourneyResponse.builder()
                .id(journey.getId())
                .userId(journey.getUserId())
                .journeyDate(journey.getJourneyDate())
                .startLocation(journey.getStartLocation())
                .endLocation(journey.getEndLocation())
                .distanceKm(journey.getDistanceKm())
                .energyUsedKwh(journey.getEnergyUsedKwh())
                .estimatedCredits(journey.getCreditsGenerated())
                .status(journey.getStatus().name())
                .createdAt(journey.getCreatedAt())
                .build();
    }
}