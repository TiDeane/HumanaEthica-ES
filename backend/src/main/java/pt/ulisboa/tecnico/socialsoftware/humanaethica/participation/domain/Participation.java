package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

@Entity
@Table(name = "participation")
public class Participation {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Integer id;
    private Integer rating;
    private LocalDateTime acceptanceDate;

    @ManyToOne
    private Volunteer volunteer;

    @ManyToOne
    private Activity activity;

    public Participation() {
    }

    public Participation(Activity activity, Volunteer volunteer, ParticipationDto participationDto) {
        setActivity(activity);
        setVolunteer(volunteer);
        setRating(participationDto.getRating());
        setAcceptanceDate(DateHandler.now());

        verifyInvariants();
    }

    public Integer getId() {
        return id;
    }

    public Integer getRating() { return rating; }

    public void setRating(Integer rating) { this.rating = rating; }

    public LocalDateTime getAcceptanceDate() { return acceptanceDate; }

    public void setAcceptanceDate(LocalDateTime acceptanceDate) { this.acceptanceDate = acceptanceDate; }

    public void setActivity(Activity activity) {
        this.activity = activity;
        activity.addParticipation(this);
    }

    public Activity getActivity() { return activity; }

    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
        volunteer.addParticipation(this);
    }

    public Volunteer getVolunteer() { return volunteer; }

    private void verifyInvariants() {
        totalParticipantsLimit();
        volunteerCanOnlyParticipateOnce();
        volunteerPlacedAfterApplicationPeriod();
    };

    private void totalParticipantsLimit() {
        if(this.activity.getParticipantsNumberLimit() < this.activity.getParticipantsNumber()) {
            throw new HEException(PARTICIPATION_TOTAL_PARTICIPANTS_GREATER_THAN_LIMIT, this.activity.getParticipantsNumber());
        }
    }

    private void volunteerCanOnlyParticipateOnce() {
        for (Participation participation : this.volunteer.getParticipations()) {
            if (participation != this && participation.getActivity() == activity) {
                 throw new HEException(PARTICIPATION_VOLUNTEER_ONCE_PER_ACTIVITY);
            }
        }
    }

    private void volunteerPlacedAfterApplicationPeriod() {
        if(!this.acceptanceDate.isAfter(this.activity.getApplicationDeadline())) {
            throw new HEException(PARTICIPATION_TIME_AFTER_DEADLINE);
        }
    }

}
