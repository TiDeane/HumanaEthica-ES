package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer

@DataJpaTest
class GetParticipationsByActivityServiceTest extends SpockTest {
    Activity activity

    def setup() {
        given: "create activity"
        def institution = institutionService.getDemoInstitution()

        def themes = new ArrayList<>()
        themes.add(createTheme(THEME_NAME_1, Theme.State.APPROVED,null))

        def activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,3,ACTIVITY_DESCRIPTION_1,
                NOW,IN_ONE_DAY,IN_TWO_DAYS,null)
        activity = new Activity(activityDto, institution, themes)
        activityRepository.save(activity)

        and: "create volunteers"
        def volunteer = new Volunteer(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.ACTIVE)
        def volunteer2 = new Volunteer(USER_2_NAME, USER_2_USERNAME, USER_2_EMAIL, AuthUser.Type.NORMAL, User.State.ACTIVE)
        userRepository.save(volunteer)
        userRepository.save(volunteer2)

        and: "participation info"
        def participationDto = createParticipationDto(RATING_1, volunteer.id)

        def participation = new Participation(activity, volunteer, participationDto)
        participationRepository.save(participation)

        and:"one more participation"
        def participationDto2 = createParticipationDto(RATING_2, volunteer2.id)
        def participation2 = new Participation(activity, volunteer2, participationDto2)
        participationRepository.save(participation2)
    }

    def 'get all participations'() {
        when:
        def result = participationService.getParticipationsByActivity(activity.getId())

        then:
        result.size() == 2
        result.get(0).rating == RATING_1
        result.get(1).rating == RATING_2
    }

    def 'null and non-existant Activity IDs'() {
        when:
        def result = participationService.getParticipationsByActivity(null)
        def result2 = participationService.getParticipationsByActivity(ID_1)

        then: "Activity ID = null"
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ACTIVITY_NOT_FOUND
        and: "Non existent Activity ID"
        result2 == null
    }


    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}

