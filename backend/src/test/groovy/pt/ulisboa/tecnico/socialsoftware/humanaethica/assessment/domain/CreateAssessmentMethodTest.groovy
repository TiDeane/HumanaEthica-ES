package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler
import spock.lang.Unroll

import java.time.LocalDateTime

@DataJpaTest
class CreateAssessmentMethodTest extends SpockTest {
    Volunteer volunteer = Mock()
    Institution institution = Mock()
    Institution otherInstitution = Mock()
    Assessment otherAssessment = Mock()
    Activity activity = Mock()
    def assessmentDto

    def setup() {
        given: "assessment info"
        assessmentDto = new AssessmentDto()
        assessmentDto.review = ASSESSMENT_REVIEW_1
    }

    def "create assessment with volunteer and institution has another assessment"() {
        given:
        volunteer.getAssessments() >> [otherAssessment]
        otherAssessment.getInstitution() >> otherInstitution
        institution.getActivities() >> [activity]
        activity.getEndingDate() >> ONE_DAY_AGO

        when:
        def result = new Assessment(assessmentDto, institution, volunteer)

        then: "check result"
        result.getReview() == ASSESSMENT_REVIEW_1
        result.getInstitution() == institution
        result.getVolunteer() == volunteer
        and: "invocations"
        1 * institution.addAssessment(_)
        1 * volunteer.addAssessment(_)
    }

    @Unroll
    def "create assessment and violate review has at least ten characters : review=#review"() {
        given:
        volunteer.getAssessments() >> [otherAssessment]
        otherAssessment.getInstitution() >> otherInstitution
        institution.getActivities() >> [activity]
        activity.getEndingDate() >> ONE_DAY_AGO
        and: "an assessment dto"
        assessmentDto = new AssessmentDto()
        assessmentDto.setReview(review)

        when:
        new Assessment(assessmentDto, institution, volunteer)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == errorMessage

        where:
        review          || errorMessage
        null            || ErrorMessage.ASSESSMENT_REVIEW_SHOULD_HAVE_AT_LEAST_TEN_CHARACTERS
        "           "   || ErrorMessage.ASSESSMENT_REVIEW_SHOULD_HAVE_AT_LEAST_TEN_CHARACTERS
        "123456789"     || ErrorMessage.ASSESSMENT_REVIEW_SHOULD_HAVE_AT_LEAST_TEN_CHARACTERS
    }

    @Unroll
    def "create assessment and violate volunteer reviewing more than one time the same institution"() {
        given:
        volunteer.getAssessments() >> [otherAssessment]
        otherAssessment.getInstitution() >> institution
        institution.getActivities() >> [activity]
        activity.getEndingDate() >> ONE_DAY_AGO
        and: "an assessment dto"
        assessmentDto = new AssessmentDto()
        assessmentDto.setReview(ASSESSMENT_REVIEW_1)

        when:
        new Assessment(assessmentDto, institution, volunteer)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ASSESSMENT_VOLUNTEER_ONLY_REIEWS_ONE_TIME_AN_INSTITUTION
    }

    @Unroll
    def "create assessment and violate reviewing an institution with no activities concluded"() {
        given:
        volunteer.getAssessments() >> [otherAssessment]
        otherAssessment.getInstitution() >> otherInstitution
        institution.getActivities() >> [activity]
        activity.getEndingDate() >> IN_ONE_DAY
        and: "an assessment dto"
        assessmentDto = new AssessmentDto()
        assessmentDto.setReview(ASSESSMENT_REVIEW_1)

        when:
        new Assessment(assessmentDto, institution, volunteer)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ASSESSMENT_INSTITUTION_MUST_HAVE_AN_ACTIVITY_CONLUDED
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}