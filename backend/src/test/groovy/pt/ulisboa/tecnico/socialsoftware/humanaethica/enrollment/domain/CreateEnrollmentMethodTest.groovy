package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain


import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer

@DataJpaTest
class CreateEnrollmentMethodTest extends SpockTest{
    Activity activity = Mock()
    Activity activity2 = Mock()
    Volunteer volunteer = Mock()
    Enrollment otherEnrollment = Mock()
    def enrollmentDto

    def setup() {
        given: "enrollment info"
        enrollmentDto = new EnrollmentDto()
        enrollmentDto.motivation = MOTIVATION_1
        enrollmentDto.enrollmentDateTime = NOW
    }

    def "create enrollment and volunteer is enrolled in another activity"() {
        given:
        otherEnrollment.getActivity() >> activity2
        otherEnrollment.getVolunteer() >> volunteer
        activity.getApplicationDeadline() >> IN_ONE_DAY
        volunteer.getEnrollments() >> [otherEnrollment]

        when:
        def result = new Enrollment(enrollmentDto, activity, volunteer)

        then: "check result"
        result.getEnrollmentDateTime() != null
        result.getActivity() == activity
        result.getVolunteer() == volunteer
        result.getMotivation() == MOTIVATION_1
        and: "invocations"
        1 * activity.addEnrollment(_)
        1 * volunteer.addEnrollment(_)
    }

    def "create enrollment with invalid motivation: motivation=#motivation"(){
        given:
        def enrollmentDto
        enrollmentDto = new EnrollmentDto()
        enrollmentDto.setMotivation(motivation)

        when:
        new Enrollment(enrollmentDto, activity, volunteer)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == errorMessage

        where:
        motivation   || errorMessage
        null         || ErrorMessage.ENROLLMENT_MOTIVATION_INVALID
        ""           || ErrorMessage.ENROLLMENT_MOTIVATION_INVALID
    }

    def "create enrollment with invalid motivation length"(){
        given:
        def enrollmentDto
        enrollmentDto = new EnrollmentDto()
        enrollmentDto.setMotivation(SHORT_MOTIVATION_1)

        when:
        new Enrollment(enrollmentDto, activity, volunteer)

        then:
        def error = thrown(HEException)
        error.getErrorMessage()== ErrorMessage.ENROLLMENT_MOTIVATION_AT_LEAST_TEN_CHARACTERS
    }

    def "create enrollment with volunteer who is already enrolled in that activity"(){
        given:
        otherEnrollment.getActivity() >> activity
        otherEnrollment.getVolunteer() >> volunteer
        volunteer.getEnrollments() >> [otherEnrollment]

        when:
        new Enrollment(enrollmentDto, activity, volunteer)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ENROLLMENT_VOLUNTEER_ONCE_PER_ACTIVITY
    }

    def "create enrollment with date time after application deadline"(){
        given:
        otherEnrollment.getActivity() >> activity
        otherEnrollment.getVolunteer() >> volunteer
        volunteer.getEnrollments() >> [otherEnrollment]
        activity2.getApplicationDeadline() >> NOW

        when:
        new Enrollment(enrollmentDto, activity2, volunteer)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ENROLLMENT_TIME_BEFORE_DEADLINE
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
