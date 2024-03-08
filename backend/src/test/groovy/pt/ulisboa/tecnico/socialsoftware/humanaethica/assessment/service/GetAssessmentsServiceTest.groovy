package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment

@DataJpaTest
class GetAssessmentsServiceTest extends SpockTest {

    def institution

    def setup() {
        institution = institutionService.getDemoInstitution()

        given: "a theme"
        def theme = createTheme(THEME_NAME_1, Theme.State.APPROVED,null)
        List<Theme> themes = [theme]
        themeRepository.save(theme)

        and: "an activity"
        def activityDto = createActivityDto(ACTIVITY_NAME_1, ACTIVITY_REGION_1, 2,
                ACTIVITY_DESCRIPTION_1, TWO_DAYS_AGO, ONE_DAY_AGO, NOW, null)
        def activity = new Activity(activityDto, institution, themes)
        activityRepository.save(activity)

        and: "a volunteer"
        Volunteer volunteer1 = new Volunteer(USER_1_NAME,USER_1_USERNAME, USER_1_EMAIL, AuthUser.Type.DEMO, User.State.SUBMITTED)
        userRepository.save(volunteer1)

        and: "another volunteer"
        Volunteer volunteer2 = new Volunteer(USER_2_NAME,USER_2_USERNAME, USER_2_EMAIL, AuthUser.Type.DEMO, User.State.SUBMITTED)
        userRepository.save(volunteer2)

        and: "an assessment"
        def assessmentDto1 = createAssessmentDto(ASSESSMENT_REVIEW_1)
        def assessment1 = new Assessment(assessmentDto1, institution, volunteer1)
        assessmentRepository.save(assessment1)

        and: 'another assessment from different user'
        def assessmentDto2 = createAssessmentDto(ASSESSMENT_REVIEW_2)
        def assessment2 = new Assessment(assessmentDto2, institution, volunteer2)
        assessmentRepository.save(assessment2)
    }

    def 'get two assessments'() {
        when:
        def result = assessmentService.getAssessmentsByInstitution(institution.getId())

        then:
        result.size() == 2
        result.get(0).review == ASSESSMENT_REVIEW_1
        result.get(1).review == ASSESSMENT_REVIEW_2
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
