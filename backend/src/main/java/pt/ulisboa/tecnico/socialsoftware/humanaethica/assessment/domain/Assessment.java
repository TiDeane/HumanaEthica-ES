package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

import java.time.LocalDateTime;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

@Entity
@Table(name = "assessment")
public class Assessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String review;

    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    @ManyToOne
    private Institution institution;

    @ManyToOne
    private Volunteer volunteer;

    public Assessment() {
    }

    public Assessment(AssessmentDto assessmentDto, Institution institution, Volunteer volunteer) {
        setReview(assessmentDto.getReview());
        setReviewDate(DateHandler.now());
        setInstitution(institution);
        setVolunteer(volunteer);

        verifyInvariants();
    }

    public Integer getId() {
        return id;
    }

    public String getReview() {
        return review;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public void setReviewDate(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
        institution.addAssessment(this);
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
        volunteer.addAssessment(this);
    }

    private void verifyInvariants() {
        reviewHasAtLeastTenCharacters();
        volunteerReviewsOneTimeInstitution();
        institutionMustHaveOneActivityCocluded();
    }

    private void reviewHasAtLeastTenCharacters() {
        if (this.review == null || this.review.trim().isEmpty() || !(this.review.length() >= 10)) {
            throw new HEException(ASSESSMENT_REVIEW_SHOULD_HAVE_AT_LEAST_TEN_CHARACTERS);
        }
    }

    private void volunteerReviewsOneTimeInstitution() {
        if (this.volunteer.getAssessments().stream()
                .anyMatch(assessment -> assessment != this && assessment.getInstitution() == this.getInstitution())) {
            throw new HEException(ASSESSMENT_VOLUNTEER_ONLY_REIEWS_ONE_TIME_AN_INSTITUTION);
        }
    }

    public void institutionMustHaveOneActivityCocluded() {
        for (Activity activity : this.institution.getActivities()) {
            if (activity.getEndingDate().isBefore(DateHandler.now())) {
                return;
            }
        }
        throw new HEException(ASSESSMENT_INSTITUTION_MUST_HAVE_AN_ACTIVITY_CONLUDED);
    }
}