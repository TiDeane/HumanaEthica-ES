package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain;

import jakarta.persistence.*;
import org.apache.catalina.User;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.dto.ActivityDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;
import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.ACTIVITY_ALREADY_EXISTS;

@Entity
@Table(name = "enrollment")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String motivation;
    private LocalDateTime enrollmentDateTime;
    ;

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
        setEnrollmentDateTime(DateHandler.toLocalDateTime(enrollmentDto.getEnrollmentDateTime()));

        verifyInvariants();
    }


    public Integer getId() {
        return id;
    }

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
    }

    public Volunteer getVolunteer(){
        return this.volunteer;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public LocalDateTime getEnrollmentDateTime() {
        return enrollmentDateTime;
    }

    public void setEnrollmentDateTime(LocalDateTime enrollmentDateTime) {
        this.enrollmentDateTime = enrollmentDateTime;
    }



    private void verifyInvariants() {
        motivationIsRequired();
        volunteerIsRequired();
        activityIsRequired();
        enrollmentDateTimeIsRequired();
    }

    private void motivationIsRequired() {
        if (this.motivation == null || this.motivation.trim().isEmpty()) {
            throw new HEException(ACTIVITY_NAME_INVALID, this.motivation);
        }
        if(this.motivation.length()<9){
            throw new HEException(ACTIVITY_NAME_INVALID, this.motivation);
        }
    }


    private void enrollmentDateTimeIsRequired() {
        if (this.enrollmentDateTime == null) {
            throw new HEException(ACTIVITY_INVALID_DATE, "application deadline");
        }
    }

    private void volunteerIsRequired() {
        if (this.volunteer == null) {
            throw new HEException(ACTIVITY_INVALID_DATE, "application deadline");
        }
    }

    private void activityIsRequired() {
        if (this.activity == null) {
            throw new HEException(ACTIVITY_INVALID_DATE, "application deadline");
        }
    }

}