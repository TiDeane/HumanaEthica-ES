package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.repository.EnrollmentRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.repository.InstitutionRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.repository.ActivityRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.repository.UserRepository;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

import java.util.Comparator;
import java.util.List;

@Service
public class EnrollmentService {
    @Autowired
    EnrollmentRepository enrollmentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ActivityRepository activityRepository;
    @Autowired
    InstitutionRepository institutionRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public EnrollmentDto createEnrollment(Integer userId, Integer activityId, EnrollmentDto enrollmentDto) {
        if (userId == null) throw new HEException(USER_NOT_FOUND);
        if (activityId == null) throw new HEException(ACTIVITY_NOT_FOUND);

        User user = userRepository.findById(userId).orElseThrow(() -> new HEException(USER_NOT_FOUND, userId));

        if (!(user instanceof Volunteer))
            throw new HEException(ENROLLMENT_USER_MUST_BE_VOLUNTEER);

        Volunteer volunteer = (Volunteer) userRepository.findById(userId).orElseThrow(() -> new HEException(USER_NOT_FOUND, userId));

        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new HEException(ACTIVITY_NOT_FOUND, activityId));

        Enrollment enrollment = new Enrollment(enrollmentDto, activity, volunteer);

        enrollmentRepository.save(enrollment);

        return new EnrollmentDto(enrollment, true, true);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<EnrollmentDto> getEnrollmentsByActivity(Integer activityId) {
        if (activityId == null) throw new HEException(ACTIVITY_NOT_FOUND);
        activityRepository.findById(activityId).orElseThrow(() -> new HEException(ACTIVITY_NOT_FOUND, activityId));
        List<Enrollment> enrollments = enrollmentRepository.getEnrollmentsByActivityId(activityId);

        return enrollments.stream()
                .map(enrollment -> new EnrollmentDto(enrollment, true, true))
                .sorted(Comparator.comparing(enrollmentDto -> enrollmentDto.getVolunteer().getName(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

}