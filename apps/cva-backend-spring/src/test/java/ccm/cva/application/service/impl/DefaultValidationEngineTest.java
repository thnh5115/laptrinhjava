package ccm.cva.application.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ccm.cva.application.service.dto.CreateVerificationRequestCommand;
import ccm.cva.domain.exception.DuplicateTripVerificationRequestException;
import ccm.cva.domain.exception.DuplicateVerificationRequestException;
import ccm.cva.domain.exception.InvalidTripMetricsException;
import ccm.cva.infrastructure.persistence.jpa.VerificationRequestRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultValidationEngineTest {

	private static final UUID OWNER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
	private static final String TRIP_ID = "trip-001";
	private static final String CHECKSUM = "checksum-001";

	@Mock
	private VerificationRequestRepository verificationRequestRepository;

	private DefaultValidationEngine validationEngine;

	@BeforeEach
	void setUp() {
		validationEngine = new DefaultValidationEngine(verificationRequestRepository);
	}

	@Test
	void validateNewRequestThrowsWhenChecksumExists() {
		CreateVerificationRequestCommand command = buildCommand(new BigDecimal("120.0"), new BigDecimal("24.0"));
		when(verificationRequestRepository.existsByChecksum(CHECKSUM)).thenReturn(true);

		assertThatThrownBy(() -> validationEngine.validateNewRequest(command))
				.isInstanceOf(DuplicateVerificationRequestException.class);

		verify(verificationRequestRepository, never()).existsByOwnerIdAndTripId(OWNER_ID, TRIP_ID);
	}

	@Test
	void validateNewRequestThrowsWhenOwnerAlreadySubmittedTrip() {
		CreateVerificationRequestCommand command = buildCommand(new BigDecimal("150.0"), new BigDecimal("30.0"));
		when(verificationRequestRepository.existsByChecksum(CHECKSUM)).thenReturn(false);
		when(verificationRequestRepository.existsByOwnerIdAndTripId(OWNER_ID, TRIP_ID)).thenReturn(true);

		assertThatThrownBy(() -> validationEngine.validateNewRequest(command))
				.isInstanceOf(DuplicateTripVerificationRequestException.class);
	}

	@Test
	void validateNewRequestThrowsWhenTripMetricsAreOutOfRange() {
		CreateVerificationRequestCommand command = buildCommand(new BigDecimal("10.0"), new BigDecimal("8.0"));
		when(verificationRequestRepository.existsByChecksum(CHECKSUM)).thenReturn(false);
		when(verificationRequestRepository.existsByOwnerIdAndTripId(OWNER_ID, TRIP_ID)).thenReturn(false);

		assertThatThrownBy(() -> validationEngine.validateNewRequest(command))
				.isInstanceOf(InvalidTripMetricsException.class)
				.hasMessageContaining("outside expected range");
	}

	@Test
	void validateNewRequestPassesForNominalRequest() {
		CreateVerificationRequestCommand command = buildCommand(new BigDecimal("100.0"), new BigDecimal("25.0"));
		when(verificationRequestRepository.existsByChecksum(CHECKSUM)).thenReturn(false);
		when(verificationRequestRepository.existsByOwnerIdAndTripId(OWNER_ID, TRIP_ID)).thenReturn(false);

		assertDoesNotThrow(() -> validationEngine.validateNewRequest(command));
	}

	private CreateVerificationRequestCommand buildCommand(BigDecimal distanceKm, BigDecimal energyKwh) {
		return new CreateVerificationRequestCommand(
				OWNER_ID,
				TRIP_ID,
				distanceKm,
				energyKwh,
				CHECKSUM,
				"notes"
		);
	}
}
