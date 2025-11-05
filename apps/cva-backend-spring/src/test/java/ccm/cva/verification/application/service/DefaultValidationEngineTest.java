package ccm.cva.verification.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import ccm.cva.verification.application.command.CreateVerificationRequestCommand;
import ccm.cva.verification.infrastructure.repository.VerificationRequestRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultValidationEngineTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    @Mock
    private VerificationRequestRepository repository;

    private DefaultValidationEngine validationEngine;

    @BeforeEach
    void setUp() {
        validationEngine = new DefaultValidationEngine(repository);
    }

    @Test
    void returnsErrorsWhenMandatoryFieldsMissing() {
        CreateVerificationRequestCommand command = new CreateVerificationRequestCommand(
            null,
            "",
            null,
            null,
            "",
            null
        );

        ValidationResult result = validationEngine.validate(command);

        assertThat(result.valid()).isFalse();
        assertThat(result.messages())
            .contains("ownerId is required", "tripId is required", "distanceKm is required",
                "energyKwh is required", "checksum is required");
    }

    @Test
    void flagsDuplicateChecksum() {
        CreateVerificationRequestCommand command = new CreateVerificationRequestCommand(
            OWNER_ID,
            "TRIP-001",
            new BigDecimal("120.0"),
            new BigDecimal("24.0"),
            "checksum-001",
            null
        );
        when(repository.existsByChecksum("checksum-001")).thenReturn(true);

        ValidationResult result = validationEngine.validate(command);

        assertThat(result.valid()).isFalse();
        assertThat(result.messages()).contains("A verification request with the same checksum already exists");
    }

    @Test
    void flagsDuplicateOwnerTripCombination() {
        CreateVerificationRequestCommand command = new CreateVerificationRequestCommand(
            OWNER_ID,
            "TRIP-001",
            new BigDecimal("150.0"),
            new BigDecimal("30.0"),
            "checksum-unique",
            null
        );
        when(repository.existsByChecksum("checksum-unique")).thenReturn(false);
        when(repository.existsByOwnerIdAndTripId(OWNER_ID, "TRIP-001")).thenReturn(true);

        ValidationResult result = validationEngine.validate(command);

        assertThat(result.valid()).isFalse();
        assertThat(result.messages()).contains("This owner has already submitted verification for the provided trip");
    }

    @Test
    void flagsEfficiencyOutsideAllowedRange() {
        CreateVerificationRequestCommand command = new CreateVerificationRequestCommand(
            OWNER_ID,
            "TRIP-002",
            new BigDecimal("10.0"),
            new BigDecimal("8.0"),
            "checksum-unique",
            null
        );
        when(repository.existsByChecksum("checksum-unique")).thenReturn(false);
        when(repository.existsByOwnerIdAndTripId(OWNER_ID, "TRIP-002")).thenReturn(false);

        ValidationResult result = validationEngine.validate(command);

        assertThat(result.valid()).isFalse();
        assertThat(result.messages()).anyMatch(msg -> msg.contains("outside expected range"));
    }

    @Test
    void acceptsNominalRequest() {
        CreateVerificationRequestCommand command = new CreateVerificationRequestCommand(
            OWNER_ID,
            "TRIP-OK",
            new BigDecimal("120.0"),
            new BigDecimal("24.0"),
            "checksum-unique",
            null
        );
        when(repository.existsByChecksum("checksum-unique")).thenReturn(false);
        when(repository.existsByOwnerIdAndTripId(OWNER_ID, "TRIP-OK")).thenReturn(false);

        ValidationResult result = validationEngine.validate(command);

        assertThat(result.valid()).isTrue();
    }
}
