package ccm.owner.service;

import ccm.owner.DTO.JourneyDTO;
import ccm.owner.entitys.EvOwner;
import ccm.owner.entitys.Journey;
import ccm.owner.repo.JourneyRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JourneyService {

    private final JourneyRepository journeyRepository;
    private final CarbonCalculationService carbonService;
    private final WalletService walletService;
    private final ObjectMapper objectMapper; // Spring Boot provides this

    @Transactional
    public void processJourneyFile(InputStream inputStream, EvOwner owner) {
        try {
            // 1. Read JSON file into a List of DTOs
            List<JourneyDTO> dtos = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<JourneyDTO>>() {}
            );

            // 2. Loop through each DTO
            for (JourneyDTO dto : dtos) {
                // 3. Map DTO to the Journey Entity
                Journey journey = new Journey();
                journey.setOwner(owner);
                journey.setDistance(dto.distanceInKm());

                Journey savedJourney = journeyRepository.save(journey);

                // 4. Calculate carbon & update wallet
                BigDecimal kgCo2Saved = carbonService.calculateCarbonSaved(savedJourney);
                BigDecimal creditsEarned = carbonService.convertCo2ToCredits(kgCo2Saved);

                if (creditsEarned.compareTo(BigDecimal.ZERO) > 0) {
                    walletService.createNewCreditToken(
                            user.getWallet(),
                            creditsEarned,
                            savedJourney
                    );
                }
            }
        } catch (Exception e) {
            // In a real app, use a more specific exception
            throw new RuntimeException("Failed to parse and process journey file", e);
        }
    }
}
