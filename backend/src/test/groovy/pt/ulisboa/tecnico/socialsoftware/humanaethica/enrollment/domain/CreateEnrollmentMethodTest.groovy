package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain


import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.dto.ActivityDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler
import spock.lang.Unroll

import java.time.LocalDateTime

class CreateEnrollmentMethodTest extends SpockTest{
    Activity activity = Mock()
    Volunteer volunteer = Mock()
    Enrollment otherEnrollment = Mock()
    def enrollmentDto

    def setup() {
        given: "enrollment info"
        enrollmentDto = new EnrollmentDto()
        enrollmentDto.motivation = MOTIVATION_1
        enrollmentDto.enrollmentDateTime = NOW
    }

    @Unroll
    def "create enrollment with volunteer and activity has another enrollment"(){
        given:
        otherEnrollment.getActivity() >> activity
        otherEnrollment.getVolunteer() >> volunteer
        volunteer.getEnrollments() >> [otherEnrollment]
        and: "an enrollment dto"
        enrollmentDto = new EnrollmentDto()
        enrollmentDto.enrollmentDateTime = NOW
        enrollmentDto.motivation = MOTIVATION_2

        when:
        new Enrollment(enrollmentDto, activity, volunteer)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ENROLLMENT_VOLUNTEER_ONCE_PER_ACTIVITY
    }

    def "create enrollment with invalid motivation length"(){
        given:
        def enrollmentDto
        enrollmentDto = new EnrollmentDto()
        enrollmentDto.setMotivation(SHORT_MOTIVATION_1)
        enrollmentDto.setEnrollmentDateTime(DateHandler.toISOString(NOW))

        when:
        new Enrollment(enrollmentDto, activity, volunteer)

        then:
        def error = thrown(HEException)
        error.getErrorMessage()== ErrorMessage.ENROLLMENT_MOTIVATION_AT_LEAST_TEN_CHARACTERS
    }
}
