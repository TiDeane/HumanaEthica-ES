package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler
import spock.lang.Unroll

@DataJpaTest
class CreateParticipationServiceTest extends SpockTest {
    public static final String EXIST = "exist"
    public static final String NO_EXIST = "noExist"

    def activity
    def member
    def volunteer
    def participationDto
    def institution
    def themes

    def setup() {
        participationDto = new ParticipationDto()
        participationDto.rating = RATING_1
        participationDto.acceptanceDate = DateHandler.toISOString(NOW)

        member = authUserService.loginDemoMemberAuth().getUser()

        volunteer = new Volunteer(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.ACTIVE)
        userRepository.save(volunteer)

        institution = institutionService.getDemoInstitution()

        themes = new ArrayList<>()
        themes.add(createTheme(THEME_NAME_1,Theme.State.APPROVED,null))

        def activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,1,ACTIVITY_DESCRIPTION_1,
                ONE_DAY_AGO,IN_TWO_DAYS,IN_THREE_DAYS,null)

        activity = new Activity(activityDto, institution, themes)
        activityRepository.save(activity)

    }

    def "create participation"() {
        given: "an participation dto"
        def participationDto = createParticipationDto(RATING_2, volunteer.id)

        when:
        def result = participationService.createParticipation(activity.getId(), participationDto)

        then: "the returned data is correct"
        result.rating == RATING_2
        result.acceptanceDate != null
        and: "the participation is saved in the database"
        participationRepository.findAll().size() == 1
        and: "the stored data is correct"
        def storedParticipation = participationRepository.findById(result.id).get()
        storedParticipation.rating == RATING_2
        storedParticipation.acceptanceDate != null
        storedParticipation.activity.id == activity.id
        storedParticipation.volunteer.id == volunteer.id
    }


    @Unroll
    def 'invalid arguments: volunteerId=#volunteerId | activityId=#activityId'() {
        given: "an participation dto"

        def participationDto = createParticipationDto(RATING_1, getVolunteerId(volunteerId))

        when:
        participationService.createParticipation(getActivityId(activityId), participationDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == errorMessage
        and: "no participation is stored in the database"
        participationRepository.findAll().size() == 0

        where:
        volunteerId     | activityId     || errorMessage
        null            | EXIST          || ErrorMessage.USER_NOT_FOUND
        NO_EXIST        | EXIST          || ErrorMessage.USER_NOT_FOUND
        EXIST           | null           || ErrorMessage.ACTIVITY_NOT_FOUND
        EXIST           | NO_EXIST       || ErrorMessage.ACTIVITY_NOT_FOUND

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
