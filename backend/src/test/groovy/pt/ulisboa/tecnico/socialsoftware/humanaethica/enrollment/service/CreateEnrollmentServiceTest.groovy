package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.dto.ActivityDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler
import spock.lang.Unroll

@DataJpaTest
class CreateEnrollmentServiceTest extends SpockTest {
    public static final String EXIST = "exist"
    public static final String NO_EXIST = "noExist"

    def activity
    def activityDto
    def volunteer
    def enrollmentDto
    def institution
    def theme

    def setup() {
        enrollmentDto = createEnrollmentDto(MOTIVATION_1)

        volunteer = authUserService.loginDemoVolunteerAuth().getUser()

        institution = institutionService.getDemoInstitution()

        theme = new Theme(THEME_NAME_1, Theme.State.APPROVED,null)
        themeRepository.save(theme)
        List<Theme> themes = [theme]

        activityDto = new ActivityDto()
        activityDto.name = ACTIVITY_NAME_1
        activityDto.region = ACTIVITY_REGION_1
        activityDto.participantsNumberLimit = 2
        activityDto.description = ACTIVITY_DESCRIPTION_1
        activityDto.startingDate = DateHandler.toISOString(IN_TWO_DAYS)
        activityDto.endingDate = DateHandler.toISOString(IN_THREE_DAYS)
        activityDto.applicationDeadline = DateHandler.toISOString(IN_ONE_DAY)

        activity = new Activity(activityDto, institution, themes)
        activityRepository.save(activity)
    }

    def "create enrollment"() {

        when:
        def result = enrollmentService.createEnrollment(volunteer.getId(), activity.getId(), enrollmentDto)

        then: "the returned data is correct"
        result.motivation ==  MOTIVATION_1
        result.enrollmentDateTime != null
        and: "the enrollment is saved in the database"
        enrollmentRepository.findAll().size() == 1
        and: "the stored data is correct"
        def storedEnrollment = enrollmentRepository.findById(result.id).get()
        storedEnrollment.motivation == MOTIVATION_1
        storedEnrollment.enrollmentDateTime != null
        storedEnrollment.activity.id == activity.id
        storedEnrollment.volunteer.id == volunteer.id
    }

    def "create enrollment with member instead of volunteer and fail"() {
        given:
        def member = authUserService.loginDemoMemberAuth().getUser() // Login as a member

        when:
        enrollmentService.createEnrollment(member.getId(), activity.getId(), enrollmentDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ENROLLMENT_USER_MUST_BE_VOLUNTEER
        enrollmentRepository.findAll().size() == 0
    }


    @Unroll
    def 'invalid arguments: volunteerId=#volunteerId | activityId=#activityId'() {

        when:
        enrollmentService.createEnrollment(getVolunteerId(volunteerId), getActivityId(activityId), enrollmentDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == errorMessage
        and: "no enrollment is stored in the database"
        enrollmentRepository.findAll().size() == 0

        where:
        volunteerId | activityId || errorMessage
        null        | EXIST      || ErrorMessage.USER_NOT_FOUND
        NO_EXIST    | EXIST      || ErrorMessage.USER_NOT_FOUND
        EXIST       | null       || ErrorMessage.ACTIVITY_NOT_FOUND
        EXIST       | NO_EXIST   || ErrorMessage.ACTIVITY_NOT_FOUND
    }

    def getVolunteerId(volunteerId){
        if (volunteerId == EXIST)
            return volunteer.id
        else if (volunteerId == NO_EXIST)
            return 222
        return null
    }

    def getActivityId(activityId){
        if (activityId == EXIST)
            return activity.id
        else if (activityId == NO_EXIST)
            return 222
        return null
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}