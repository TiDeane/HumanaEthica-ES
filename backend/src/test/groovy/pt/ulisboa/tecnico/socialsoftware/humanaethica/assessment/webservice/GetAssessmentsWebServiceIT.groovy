package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.webservice

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.dto.ThemeDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.repository.AssessmentRepository
import org.springframework.web.reactive.function.client.WebClientResponseException
import spock.lang.Unroll

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetAssessmentsWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def user
    def institution
    def institutionId
    def activityDto
    def themes
    def activity
    def activityId
    def volunteer
    def volunteerOther
    def assessmentDto
    def assessmentDtoOther

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

        and: "2 volunteers"
        volunteer = new Volunteer(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.ACTIVE)
        userRepository.save(volunteer)
        volunteerOther = new Volunteer(USER_2_NAME, USER_2_USERNAME, USER_2_EMAIL, AuthUser.Type.NORMAL, User.State.ACTIVE)
        userRepository.save(volunteerOther)

        and: "2 assessments"
        assessmentDto = createAssessmentDto(ASSESSMENT_REVIEW_1)
        assessmentService.createAssessment(volunteer.id, institution.id, assessmentDto)
        assessmentDtoOther = createAssessmentDto(ASSESSMENT_REVIEW_2)
        assessmentService.createAssessment(volunteerOther.id, institution.id, assessmentDtoOther)

        activityId = activity.id
        institutionId = institution.id
    }

    @Unroll
    def "anyone can get institution assessments with #role"() {
        given:
        switch (role) {
            case "volunteer":
                demoVolunteerLogin()
                break
            case "member":
                demoMemberLogin()
                break
            case "admin":
                demoAdminLogin()
                break
            case "none":
                break
        }

        when:
        def response = webClient.get()
                .uri('/assessments/' + institutionId + '/reviewers')
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(AssessmentDto.class)
                .collectList()
                .block()

        then: "check response"
        response.size() == 2
        response.get(0).review == ASSESSMENT_REVIEW_1
        response.get(1).review == ASSESSMENT_REVIEW_2

        cleanup:
        deleteAll()

        where:
        role << ["volunteer", "member", "admin", "none"]
    }
}