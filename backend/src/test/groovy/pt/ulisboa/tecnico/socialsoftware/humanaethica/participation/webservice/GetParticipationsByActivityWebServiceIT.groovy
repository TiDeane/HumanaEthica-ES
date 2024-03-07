package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.webservice

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.dto.ActivityDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.dto.ThemeDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer

////
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetParticipationsByActivityWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def activityDto
    def activityId

    def setup() {
        deleteAll()

        webClient = WebClient.create("http://localhost:" + port)
        headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        def user = demoMemberLogin()

        def theme = createTheme(THEME_NAME_1, Theme.State.APPROVED,null)
        def themesDto = new ArrayList<>()
        themesDto.add(new ThemeDto(theme,false,false,false))

        activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,2,ACTIVITY_DESCRIPTION_1,
                ONE_DAY_AGO,IN_TWO_DAYS,IN_THREE_DAYS,themesDto)

        def activity = activityService.registerActivity(user.id, activityDto)

        activityId = activity.id

        and: "2 volunteers"
        def volunteer = new Volunteer(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.ACTIVE)
        userRepository.save(volunteer)
        def volunteer2 = new Volunteer(USER_2_NAME, USER_2_USERNAME, USER_2_EMAIL, AuthUser.Type.NORMAL, User.State.ACTIVE)
        userRepository.save(volunteer2)

        and: "2 participations"
        def participationDto = createParticipationDto(RATING_1, volunteer.id)
        participationService.createParticipation(activityId, participationDto)

        def participationDto2 = createParticipationDto(RATING_2, volunteer2.id)
        participationService.createParticipation(activityId, participationDto2)

    }

    def "login as a member, and get activity participants"() {
        given:
        demoMemberLogin()

        when:
        def response = webClient.get()
                .uri('/participations/' + activityId + '/participants')
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(ParticipationDto.class)
                .collectList()
                .block()

        then: "check response"
        response.size() == 2
        response.get(1).rating == RATING_2
        response.get(0).rating == RATING_1

        cleanup:
        deleteAll()
    }

    def "login as volunteer, and get an error"() {
        given: 'a volunteer'
        demoVolunteerLogin()

        when: 'the volunteer registers the activity'
        webClient.get()
                .uri('/participations/' + activityId + '/participants')
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(ParticipationDto.class)
                .collectList()
                .block()

        then: "check response status"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN

        cleanup:
        deleteAll()
    }

    def "login as member of another institution and cannot get participations of activity"() {
        given:
        def otherInstitution = new Institution(INSTITUTION_1_NAME, INSTITUTION_1_EMAIL, INSTITUTION_1_NIF)
        institutionRepository.save(otherInstitution)
        def otherMember = createMember(USER_3_NAME,USER_3_USERNAME,USER_2_PASSWORD,USER_3_EMAIL, AuthUser.Type.NORMAL, otherInstitution, User.State.APPROVED)
        normalUserLogin(USER_3_USERNAME, USER_2_PASSWORD)

        when:
        webClient.get()
                .uri('/participations/' + activityId + '/participants')
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(ParticipationDto.class)
                .collectList()
                .block()

        then: "check response status"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN

        cleanup:
        deleteAll()
    }

}