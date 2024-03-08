package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.webservice

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.dto.ThemeDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateAssessmentsWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def user
    def institution
    def institutionId
    def assessmentDto
    def activityDto
    def themes
    def activity
    def activityId
    def volunteer

    def setup() {
        deleteAll()

        webClient = WebClient.create("http://localhost:" + port)
        headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        user = demoMemberLogin()

        institution = institutionService.getDemoInstitution()
        given: "activity info"
        activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,1,ACTIVITY_DESCRIPTION_1,
                THREE_DAYS_AGO,TWO_DAYS_AGO,ONE_DAY_AGO,null)

        and: "a theme"
        themes = new ArrayList<>()
        themes.add(createTheme(THEME_NAME_1, Theme.State.APPROVED,null))

        and: "an activity"
        activity = new Activity(activityDto, institution, themes)
        activityRepository.save(activity)

        institution.addActivity(activity)

        and: "a volunteer"
        volunteer = new Volunteer(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.ACTIVE)
        userRepository.save(volunteer)

        assessmentDto = createAssessmentDto(ASSESSMENT_REVIEW_1)

        activityId = activity.id
        institutionId = institution.id
    }

    def "login as volunteer, and create an assessment"() {
        given: 'a demo'
        def volunteer = demoVolunteerLogin()

        when: 'the admin registers the activity'
        def response = webClient.post()
                .uri('/assessments/' + institutionId)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(assessmentDto)
                .retrieve()
                .bodyToMono(AssessmentDto.class)
                .block()

        then: "check response data"
        response.review == ASSESSMENT_REVIEW_1
        response.reviewDate != null

        and: 'check database data'
        assessmentRepository.count() == 1
        def assessment = assessmentRepository.findAll().get(0)
        assessment.getReview() == ASSESSMENT_REVIEW_1
        assessment.getVolunteer().getId() == volunteer.id
        assessment.getReviewDate() != null

        cleanup:
        deleteAll()
    }

    def "login as admin, and create an assessment"() {
        given: 'a demo'
        demoAdminLogin()

        when: 'the admin registers the activity'
        webClient.post()
                .uri('/assessments/'+ institutionId)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(assessmentDto)
                .retrieve()
                .bodyToMono(AssessmentDto.class)
                .block()

        then: "an error is returned"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN
        assessmentRepository.count() == 0

        cleanup:
        deleteAll()
    }

    def "login as volunteer, and create an assessment with error"() {
        given: 'a volunteer'
        demoVolunteerLogin()
        and: 'a name with blanks'
        assessmentDto.review = null

        when: 'the volunteer registers the assessment'
        webClient.post()
                .uri('/assessments/' + institutionId)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(assessmentDto)
                .retrieve()
                .bodyToMono(AssessmentDto.class)
                .block()

        then: "check response status"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.BAD_REQUEST
        assessmentRepository.count() == 0

        cleanup:
        deleteAll()
    }
}