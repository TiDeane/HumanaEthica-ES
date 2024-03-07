package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer

@DataJpaTest
class GetEnrollmentsByActivityServiceTest extends SpockTest {
    // Declare activity at the class level
    Activity activity

    def setup() {
        def institution = institutionService.getDemoInstitution()
        given: "activity info"
        def activityDto = createActivityDto(ACTIVITY_NAME_1, ACTIVITY_REGION_1, 1, ACTIVITY_DESCRIPTION_1,
                IN_ONE_DAY, IN_TWO_DAYS, IN_THREE_DAYS, null)
        and: "themes"
        def themes = new ArrayList<>()
        themes.add(createTheme(THEME_NAME_1, Theme.State.APPROVED, null))
        and: "an activity"
        activity = new Activity(activityDto, institution, themes)
        activityRepository.save(activity)
        and: "a volunteer"
        Volunteer volunteer = new Volunteer(USER_1_NAME,USER_1_USERNAME, USER_1_EMAIL, AuthUser.Type.DEMO, User.State.SUBMITTED)
        userRepository.save(volunteer)
        and: "another volunteer"
        Volunteer volunteer2 = new Volunteer(USER_2_NAME,USER_2_USERNAME, USER_2_EMAIL, AuthUser.Type.DEMO, User.State.SUBMITTED)
        userRepository.save(volunteer2)
        and: "an enrollment"
        def enrollmentDto = createEnrollmentDto(MOTIVATION_1)
        def enrollment = new Enrollment(enrollmentDto, activity, volunteer)
        enrollmentRepository.save(enrollment)
        and: "another enrollment"
        def enrollmentDto2 = createEnrollmentDto(MOTIVATION_2)
        def enrollment2 = new Enrollment(enrollmentDto2, activity, volunteer2)
        enrollmentRepository.save(enrollment2)
    }

    def "get two enrollments" () {
        given: "an activity id"
        def activityId = activity.getId()

        when:
        def result = enrollmentService.getEnrollmentsByActivity(activityId)

        then:
        result.size() == 2
        result.get(0).motivation == MOTIVATION_1
        result.get(1).motivation == MOTIVATION_2
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}