package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.dto.ActivityDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

public class ParticipationDto {
    private Integer id;
    private Integer rating;
    private String acceptanceDate;

    private ActivityDto activity;
    private UserDto volunteer;
    private Integer volunteerId;

    public ParticipationDto(){}

    public ParticipationDto(Participation participation, boolean deepCopyActivity, boolean deepCopyVolunteer){
        setId(participation.getId());
        setRating(participation.getRating());
        setAcceptanceDate(DateHandler.toISOString(participation.getAcceptanceDate()));

        if (deepCopyActivity && (participation.getActivity() != null)) {
            setActivity(new ActivityDto(participation.getActivity(), false));
        }

        if(deepCopyVolunteer && (participation.getVolunteer() != null)) {
            setVolunteer(new UserDto(participation.getVolunteer()));
            this.setVolunteerId(participation.getVolunteer().getId());
        }

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getAcceptanceDate() {
        return acceptanceDate;
    }

    public void setAcceptanceDate(String acceptanceDate) {
        this.acceptanceDate = acceptanceDate;
    }

    public ActivityDto getActivity() {
        return activity;
    }

    public void setActivity(ActivityDto activity) {
        this.activity = activity;
    }

    public UserDto getVolunteer(){
        return volunteer;
    }

    public void setVolunteer(UserDto volunteer){
        this.volunteer = volunteer;
    }

    public Integer getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(Integer volunteerId) {
        this.volunteerId = volunteerId;
    }


    @Override
    public String toString() {
        return "ParticipationDto{" +
                "id=" + id +
                ", rating='" + rating + '\'' +
                ", acceptanceDate='" + acceptanceDate + '\'' +
                ", activity=" + activity +
                ", user=" + volunteer +
                '}';
    }

}