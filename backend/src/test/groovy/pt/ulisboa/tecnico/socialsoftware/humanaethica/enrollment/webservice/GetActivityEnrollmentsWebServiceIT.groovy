package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.webservice

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.dto.ThemeDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetActivityEnrollmentsWebServiceIT extends SpockTest {
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
                IN_ONE_DAY,IN_TWO_DAYS,IN_THREE_DAYS,themesDto)

        def activity = activityService.registerActivity(user.id, activityDto)

        activityId = activity.id

        def enrollmentDto = createEnrollmentDto(MOTIVATION_1)
        def enrollmentDto2 = createEnrollmentDto(MOTIVATION_2)

        def volunteer = new Volunteer(USER_1_NAME, User.State.APPROVED)
        def volunteer2 = new Volunteer(USER_2_NAME, User.State.APPROVED)
        userRepository.save(volunteer)
        userRepository.save(volunteer2)

        enrollmentService.createEnrollment(volunteer.id, activityId, enrollmentDto)
        enrollmentService.createEnrollment(volunteer2.id, activityId, enrollmentDto2)
    }

    def "login as member, get activity enrollments"() {
        given:
        demoMemberLogin()

        when:
        def response = webClient.get()
                .uri('/enrollments/' + activityId + '/list')
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(EnrollmentDto.class)
                .collectList()
                .block()

        then: "check response"
        response.size() == 2
        response.get(0).motivation == MOTIVATION_1
        response.get(1).motivation == MOTIVATION_2

        cleanup:
        deleteAll()
    }

    def "login as member of another institution and cannot get activity enrollments"() {
        given:
        def otherInstitution = new Institution(INSTITUTION_1_NAME, INSTITUTION_1_EMAIL, INSTITUTION_1_NIF)
        institutionRepository.save(otherInstitution)
        def otherMember = createMember(USER_1_NAME,USER_1_USERNAME,USER_1_PASSWORD,USER_1_EMAIL, AuthUser.Type.NORMAL, otherInstitution, User.State.APPROVED)
        normalUserLogin(USER_1_USERNAME, USER_1_PASSWORD)

        when:
        def response = webClient.get()
                .uri('/enrollments/' + activityId + '/list')
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(EnrollmentDto.class)
                .collectList()
                .block()

        then: "check response status"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN

        cleanup:
        deleteAll()
    }
}
