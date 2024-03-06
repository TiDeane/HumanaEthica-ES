package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto;

import java.time.LocalDateTime;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

@Entity
@Table(name = "enrollment")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String motivation;

    @Column(name = "enrollment_date_time")
    private LocalDateTime enrollmentDateTime;

    @ManyToOne
    private Activity activity;

    @ManyToOne
    private Volunteer volunteer;

    public Enrollment() {
    }

    public Enrollment(EnrollmentDto enrollmentDto, Activity activity, Volunteer volunteer) {
        setActivity(activity);
        setVolunteer(volunteer);
        setMotivation(enrollmentDto.getMotivation());
        setEnrollmentDateTime(DateHandler.now());

        verifyInvariants();
    }

    public void update(EnrollmentDto enrollmentDto) {
        setMotivation(enrollmentDto.getMotivation());

        verifyInvariants();
    }


    public Integer getId() {
        return id;
    }

    public String getMotivation() {
        return this.motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
        volunteer.addEnrollment(this);
    }

    public Volunteer getVolunteer(){
        return this.volunteer;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        activity.addEnrollment(this);
    }

    public LocalDateTime getEnrollmentDateTime() {
        return enrollmentDateTime;
    }

    public void setEnrollmentDateTime(LocalDateTime enrollmentDateTime) {
        this.enrollmentDateTime = enrollmentDateTime;
    }

    private void verifyInvariants() {
        motivationIsRequired();
        enrollmentDateTimeIsRequired();
        motivationAtLeastTenCharacters();
        enrollOncePerActivity();
        enrollmentTimeBeforeDeadline();
    }

    private void motivationIsRequired() {
        if (this.motivation == null || this.motivation.trim().isEmpty()) {
            throw new HEException(ENROLLMENT_MOTIVATION_INVALID, this.motivation);
        }
    }

    private void enrollmentDateTimeIsRequired() {
        if (this.enrollmentDateTime == null) {
            throw new HEException(ENROLLMENT_DATE_TIME_INVALID, "enrollment date time");
        }
    }

    private void motivationAtLeastTenCharacters() {
        if (this.motivation.length() < 10) {
            throw new HEException(ENROLLMENT_MOTIVATION_AT_LEAST_TEN_CHARACTERS, "motivation");
        }
    }

    private void enrollOncePerActivity() {
        for (Enrollment enrollment : this.volunteer.getEnrollments()) {
            if (enrollment != this && enrollment.getActivity() == activity) {
                throw new HEException(ENROLLMENT_VOLUNTEER_ONCE_PER_ACTIVITY);
            }
        }
    }

    private void enrollmentTimeBeforeDeadline() {
        if (!this.enrollmentDateTime.isBefore(this.activity.getApplicationDeadline())) {
            throw new HEException(ENROLLMENT_TIME_BEFORE_DEADLINE);
        }
    }

}