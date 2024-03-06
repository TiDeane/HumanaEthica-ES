<<<<<<< Updated upstream
package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler
import spock.lang.Unroll

import java.time.LocalDateTime


@DataJpaTest
class CreateActivityMethodTest extends SpockTest {
    Activity activity = Mock()
    Activity otherActivity = Mock()
    Volunteer volunteer = Mock()
    Volunteer otherVolunteer = Mock()
    Participation otherParticipation = Mock()
    def participationDto

    def setup() {
        given: "participation info"
        participationDto = new ParticipationDto()
        participationDto.rating = RATING_1
        participationDto.acceptanceDate = DateHandler.toISOString(NOW)
    }

    @Unroll
    def "create participation violates unique participation by volunteer in an activity invariant"() {
        given:
        volunteer.getParticipations() >> [otherParticipation]
        otherParticipation.getActivity() >> activity
        activity.getParticipantsNumberLimit() >> 4
        activity.getApplicationDeadline() >> TWO_DAYS_AGO
        activity.getParticipations() >> [otherParticipation]

        when:
        new Participation(activity, volunteer, participationDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.PARTICIPATION_VOLUNTEER_ONCE_PER_ACTIVITY
    }

    @Unroll
    def "create participation violates AcceptanceDate invariant"() {
        given:
        volunteer.getParticipations() >> [otherParticipation]
        otherParticipation.getActivity() >> otherActivity
        activity.getParticipantsNumberLimit() >> 4
        activity.getApplicationDeadline() >> applicationDeadline
        activity.getParticipations() >> [otherParticipation]

        when:
        new Participation(activity, volunteer, participationDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == errorMessage

        where:
        applicationDeadline || errorMessage
        IN_ONE_DAY          || ErrorMessage.PARTICIPATION_TIME_AFTER_DEADLINE
        IN_TWO_DAYS         || ErrorMessage.PARTICIPATION_TIME_AFTER_DEADLINE

    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}

=======
package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler
import spock.lang.Unroll

import java.time.LocalDateTime


@DataJpaTest
class CreateActivityMethodTest extends SpockTest {
    Activity activity = Mock()
    Activity otherActivity = Mock()
    Volunteer volunteer = Mock()
    Volunteer otherVolunteer = Mock()
    Participation otherParticipation = Mock()
    def participationDto

    def setup() {
        given: "participation info"
        participationDto = new ParticipationDto()
        participationDto.rating = RATING_1
        participationDto.acceptanceDate = DateHandler.toISOString(NOW)

        volunteer.getParticipations() >> [otherParticipation]
        otherParticipation.getActivity() >> otherActivity
    }

    def "create participation object"() {
        given:
        activity.getParticipantsNumberLimit() >> 5
        activity.getParticipantsNumber() >> 1

        activity.getApplicationDeadline() >> TWO_DAYS_AGO
        activity.getParticipations() >> [otherParticipation]

        when:
        def result = new Participation(activity, volunteer, participationDto)

        then: "check result"
        result.getAcceptanceDate() != null

        result.getActivity() == activity
        result.getVolunteer() == volunteer
        result.getRating() == RATING_1
        and: "invocations"
        1 * activity.addParticipation(_)
        1 * volunteer.addParticipation(_)

    }

    def "create participation where participant number is higher than the activity's total participant limit"() {
        given:
        activity.getParticipantsNumberLimit() >> 5
        activity.getParticipantsNumber() >> 5
        activity.getApplicationDeadline() >> TWO_DAYS_AGO

        when:
        def result = new Participation(activity, volunteer, participationDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.PARTICIPATION_TOTAL_PARTICIPANTS_GREATER_THAN_LIMIT

    }

    @Unroll
    def "create participation violates unique participation by volunteer in an activity invariant"() {
        given:
        activity.getParticipantsNumberLimit() >> 4
        activity.getApplicationDeadline() >> TWO_DAYS_AGO
        activity.getParticipations() >> [otherParticipation]

        when:
        new Participation(activity, volunteer, participationDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.PARTICIPATION_VOLUNTEER_ONCE_PER_ACTIVITY
    }

    @Unroll
    def "create participation violates AcceptanceDate invariant"() {
        given:
        activity.getParticipantsNumberLimit() >> 4
        activity.getApplicationDeadline() >> applicationDeadline
        activity.getParticipations() >> [otherParticipation]

        when:
        new Participation(activity, volunteer, participationDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == errorMessage

        where:
        applicationDeadline || errorMessage
        IN_ONE_DAY          || ErrorMessage.PARTICIPATION_TIME_AFTER_DEADLINE
        IN_TWO_DAYS         || ErrorMessage.PARTICIPATION_TIME_AFTER_DEADLINE

    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}

>>>>>>> Stashed changes
}