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

    public void update(ParticipationDto participationDto) {
        setRating(participationDto.getRating());
        setAcceptanceDate(DateHandler.toLocalDateTime(participationDto.getAcceptanceDate()));

        verifyInvariants();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) { this.id = id; }

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

    private void verifyInvariants() {
        ratingIsRequired();
        acceptanceDateIsRequired();
        totalParticipantsLimit();
        volunteerCanOnlyParticipateOnce();
        volunteerPlacedAfterApplicationPeriod();
    };

    private void ratingIsRequired() {
        if (this.rating == null) {
            throw new HEException(PARTICIPATION_RATING_INVALID);
        }
    }

    private void acceptanceDateIsRequired() {
        if (this.acceptanceDate == null) {
            throw new HEException(PARTICIPATION_INVALID_DATE, "acceptance date");
        }
    }

    private void totalParticipantsLimit() {
        if(this.activity.getParticipantsNumberLimit() < this.activity.getParticipations().size()) {
            throw new HEException(PARTICIPATION_TOTAL_PARTICIPANTS_GREATER_THAN_LIMIT, this.activity.getParticipations().size());
        }
    }

    private void volunteerCanOnlyParticipateOnce() {
        for (Participation participation : this.volunteer.getParticipations()) {
            if (participation.getActivity() == activity) {
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
