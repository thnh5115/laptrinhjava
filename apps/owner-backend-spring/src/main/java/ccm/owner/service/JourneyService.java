package ccm.owner.service;

import ccm.owner.entitys.Journey;
import ccm.owner.entitys.JourneyStatus;
import ccm.owner.repo.JourneyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JourneyService implements IJourneyService {

    private final JourneyRepository journeyRepository;

    @Autowired
    public JourneyService(JourneyRepository journeyRepository) {
        this.journeyRepository = journeyRepository;
    }

    @Override
    public List<Journey> getAllJourneys() {
        return journeyRepository.findAll();
    }

    @Override
    public Optional<Journey> getJourneyById(Long id) {
        return journeyRepository.findById(id);
    }

    @Override
    public List<Journey> getJourneysByOwnerId(Long ownerId) {
        return journeyRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<Journey> getJourneysByStatus(JourneyStatus journeyStatus) {
        return journeyRepository.findByJourneyStatus(journeyStatus);
    }

    @Override
    public Journey saveJourney(Journey journey) {
        return journeyRepository.save(journey);
    }

    @Override
    public void deleteJourney(Long id) {
        journeyRepository.deleteById(id);
    }
}
