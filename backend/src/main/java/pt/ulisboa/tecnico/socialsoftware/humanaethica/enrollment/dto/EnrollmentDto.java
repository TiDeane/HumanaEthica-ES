package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

public class EnrollmentDto {
    private Integer id;
    private String motivation;

    private String enrollmentDateTime;

    private Integer activityId;
    private String volunteerName;

    private int volunteerId;

    private boolean participating;


    public EnrollmentDto() {}

    public EnrollmentDto(Enrollment enrollment) {
        this.id = enrollment.getId();
        this.motivation = enrollment.getMotivation();
        this.enrollmentDateTime = DateHandler.toISOString(enrollment.getEnrollmentDateTime());
        this.activityId = enrollment.getActivity().getId();
        this.volunteerName = enrollment.getVolunteer().getName();
        this.volunteerId = enrollment.getVolunteer().getId();
        this.participating = isParticipating(enrollment);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    public String getEnrollmentDateTime() {
        return enrollmentDateTime;
    }

    public void setEnrollmentDateTime(String enrollmentDateTime) {
        this.enrollmentDateTime = enrollmentDateTime;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }
        
    public String getVolunteerName() {
        return volunteerName;
    }

    public void setVolunteerName(String volunteerName) {
        this.volunteerName = volunteerName;
    }

    public int getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(int volunteerId) {
        this.volunteerId = volunteerId;
    }

    public boolean getParticipating() {
        return participating;
    }

    public void setParticipating(boolean participating) {
        this.participating = participating;
    }

    public boolean isParticipating(Enrollment enrollment) {
        if (enrollment.getActivity().getParticipations().stream()
            .anyMatch(participation ->  participation.getVolunteer().getName() == this.volunteerName)) {
            return true;
        }
        return false;
    }

}
