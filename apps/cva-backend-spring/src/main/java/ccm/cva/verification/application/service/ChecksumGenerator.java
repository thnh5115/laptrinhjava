package ccm.cva.verification.application.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Generates deterministic checksums for EV trip submissions so that clients can avoid duplicating
 * the hashing logic when importing multiple verification requests.
 */
@Component
public class ChecksumGenerator {

    private static final HexFormat HEX = HexFormat.of();

    public String generate(UUID ownerId, String tripId, BigDecimal distanceKm, BigDecimal energyKwh) {
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        if (!StringUtils.hasText(tripId)) {
            throw new IllegalArgumentException("tripId must not be blank");
        }
        Objects.requireNonNull(distanceKm, "distanceKm must not be null");
        Objects.requireNonNull(energyKwh, "energyKwh must not be null");

        String payload = ownerId + "|" + tripId.trim() + "|" + distanceKm.stripTrailingZeros().toPlainString()
            + "|" + energyKwh.stripTrailingZeros().toPlainString();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HEX.formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", ex);
        }
    }
}
