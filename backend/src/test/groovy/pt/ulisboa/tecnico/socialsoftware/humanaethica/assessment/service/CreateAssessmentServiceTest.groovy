package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.service;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme;
import spock.lang.Unroll;

@DataJpaTest
class CreateAssessmentServiceTest extends SpockTest {
    public static final String EXIST = "exist";
    public static final String NO_EXIST = "noExist";

    def volunteer;
    def institution;

    def setup() {

        volunteer = authUserService.loginDemoVolunteerAuth().getUser();
        institution = institutionService.getDemoInstitution();

        given: "a theme"
        def theme = createTheme(THEME_NAME_1, Theme.State.APPROVED,null)
        List<Theme> themes = [theme]
        themeRepository.save(theme)

        and: "an activity"
        def activityDto = createActivityDto(ACTIVITY_NAME_1, ACTIVITY_REGION_1, 2,
                ACTIVITY_DESCRIPTION_1, TWO_DAYS_AGO, ONE_DAY_AGO, NOW, null)
        def activity = new Activity(activityDto, institution, themes)
        activityRepository.save(activity)
    }

    def "register assessment"() {

        given: "an assessment dto"
        def assessmentDto = createAssessmentDto(ASSESSMENT_REVIEW_1);

        when:
        def result = assessmentService.createAssessment(volunteer.getId(), institution.getId(), assessmentDto);

        then: "the returned data is correct"
        result.review == ASSESSMENT_REVIEW_1
        result.reviewDate != null

        and: "the assessment is saved in the database"
        assessmentRepository.findAll().size() == 1

        and: "the stored data is correct"
        def storedAssessment = assessmentRepository.findById(result.id).get()
        storedAssessment.volunteer.id == volunteer.id
        storedAssessment.institution.id == institution.id
        storedAssessment.review == ASSESSMENT_REVIEW_1
        storedAssessment.reviewDate != null
    }

    @Unroll
    def 'invalid arguments: review=#review | volunteerId=#volunteerId | institutionId=#institutionId'() {
        given: "an assessment dto"
        def assessmentDto = createAssessmentDto(review);

        when:
        assessmentService.createAssessment(getVolunteerId(volunteerId), getInstitutionId(institutionId), assessmentDto);

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == errorMessage

        and: "no assessment is stored in the database"
        assessmentRepository.findAll().size() == 0

        where:
        review                  | volunteerId | institutionId || errorMessage
        ASSESSMENT_REVIEW_1     | null        | EXIST         || ErrorMessage.USER_NOT_FOUND
        ASSESSMENT_REVIEW_1     | NO_EXIST    | EXIST         || ErrorMessage.USER_NOT_FOUND
        ASSESSMENT_REVIEW_1     | EXIST       | null          || ErrorMessage.INSTITUTION_NOT_FOUND
        ASSESSMENT_REVIEW_1     | EXIST       | NO_EXIST      || ErrorMessage.INSTITUTION_NOT_FOUND
    }

    def getVolunteerId(volunteerId){
        if (volunteerId == EXIST)
            return volunteer.id
        else if (volunteerId == NO_EXIST)
            return 222
        return null
    }

    def getInstitutionId(institutionId){
        if (institutionId == EXIST)
            return institution.id
        else if (institutionId == NO_EXIST)
            return 222
        return null
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
