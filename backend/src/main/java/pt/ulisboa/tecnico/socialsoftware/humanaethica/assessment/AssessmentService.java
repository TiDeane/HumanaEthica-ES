package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.repository.AssessmentRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution; 
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.repository.InstitutionRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.repository.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

import java.util.Comparator;
import java.util.List;             

@Service
public class AssessmentService {
    @Autowired
    private AssessmentRepository assessmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private InstitutionRepository institutionRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<AssessmentDto> getAssessmentsByInstitution(Integer institutionId) {
        if (institutionId == null) throw new HEException(INSTITUTION_NOT_FOUND);

        List<Assessment> assessments = assessmentRepository.getAssessmentsByInstitutionId(institutionId);

        // Is name ordering really what we would want??
        return assessments.stream()
                .map(assessment -> new AssessmentDto(assessment, true, true))
                .sorted(Comparator.comparing(assessmentDto -> assessmentDto.getVolunteer().getName(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AssessmentDto createAssessment(Integer userId, Integer institutionId, AssessmentDto assessmentDto) {
        if (userId == null) throw new HEException(USER_NOT_FOUND);
        if (institutionId == null) throw new HEException(INSTITUTION_NOT_FOUND);

        Volunteer volunteer = (Volunteer) userRepository.findById(userId)
                .orElseThrow(() -> new HEException(USER_NOT_FOUND, userId));

        Institution institution = institutionRepository.findById(institutionId)
            .orElseThrow(() -> new HEException(INSTITUTION_NOT_FOUND, institutionId));
        
        Assessment assessment = new Assessment(assessmentDto, institution, volunteer);
       
        assessment.setReview(assessmentDto.getReview());
            
        assessment = assessmentRepository.save(assessment);

        return new AssessmentDto(assessment, true, true);
    }

}