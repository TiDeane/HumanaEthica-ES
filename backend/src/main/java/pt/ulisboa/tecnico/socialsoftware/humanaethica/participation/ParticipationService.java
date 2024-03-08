package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.repository.ActivityRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.repository.InstitutionRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.repository.ParticipationRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.repository.UserRepository;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

import java.util.Comparator;
import java.util.List;

@Service
public class ParticipationService {
    @Autowired
    ParticipationRepository participationRepository;
    @Autowired
    ActivityRepository activityRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    InstitutionRepository institutionRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<ParticipationDto> getParticipationsByActivity(Integer activityId) {
        if (activityId == null) throw new HEException(ACTIVITY_NOT_FOUND);

        List<Participation> participations = participationRepository.getParticipationsByActivityId(activityId);

        return participations.stream()
                .map(participation -> new ParticipationDto(participation, true, true))
                .sorted(Comparator.comparing(participationDto -> participationDto.getVolunteer().getName(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ParticipationDto createParticipation(Integer activityId, ParticipationDto participationDto) {
        if (activityId == null) throw new HEException(ACTIVITY_NOT_FOUND);

        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new HEException(ACTIVITY_NOT_FOUND, activityId));

        Integer userId = participationDto.getVolunteerId();
        if (userId == null) throw new HEException(USER_NOT_FOUND);

        Volunteer volunteer = (Volunteer) userRepository.findById(userId).orElseThrow(() -> new HEException(USER_NOT_FOUND, userId));

        Participation participation = new Participation(activity, volunteer, participationDto);

        participationRepository.save(participation);

        return new ParticipationDto(participation, true, true);
    }

}