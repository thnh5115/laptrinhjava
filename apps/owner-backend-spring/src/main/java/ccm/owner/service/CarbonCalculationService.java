package ccm.owner.service;

import ccm.owner.entitys.Journey;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class CarbonCalculationService {

//EMISSION_FACTORS
//  "CAR", 0.192,      // Baseline
//  "TRAIN", 0.04,
//  "BIKE", 0.0,
//  "WALK", 0.0,
//  "BUS", 0.08

    private static final double BASELINE_FACTOR = 0.192;
    private static final BigDecimal KG_CO2_TO_CREDITS_RATE = new BigDecimal("1.0"); // 1 credit per 1kg CO2

    public BigDecimal calculateCarbonSaved(Journey journey) {
        double journeyEmissionFactor = 0.05; // EMISSION_FACTOR_FOR_EV

        double kgCo2Saved = (BASELINE_FACTOR - journeyEmissionFactor) * journey.getDistance();
        if (kgCo2Saved <= 0) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(kgCo2Saved).setScale(4, java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal convertCo2ToCredits(BigDecimal kgCo2Saved) {
        return kgCo2Saved.multiply(KG_CO2_TO_CREDITS_RATE)
                .setScale(4, java.math.RoundingMode.HALF_UP);
    }
}