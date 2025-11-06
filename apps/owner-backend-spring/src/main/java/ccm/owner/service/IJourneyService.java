package ccm.owner.service;

import ccm.owner.entitys.Journey;
import ccm.owner.entitys.JourneyStatus;

import java.util.List;
import java.util.Optional;

public interface IJourneyService {

    Journey saveJourney(Journey journey);

    List<Journey> getAllJourneys();

    Optional<Journey> getJourneyById(Long id);

    List<Journey> getJourneysByOwnerId(Long ownerId);

    List<Journey> getJourneysByStatus(JourneyStatus journeyStatus);

    void deleteJourney(Long id);
}