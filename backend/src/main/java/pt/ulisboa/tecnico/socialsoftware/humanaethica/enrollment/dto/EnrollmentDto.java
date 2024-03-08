package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.dto.ActivityDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

public class EnrollmentDto {
    private Integer id;
    private String motivation;
    private String enrollmentDateTime;

    private ActivityDto activity;
    private UserDto volunteer;

    public EnrollmentDto(){
    }

    public EnrollmentDto(Enrollment enrollment, boolean deepCopyActivity, boolean deepCopyVolunteer){
        setId(enrollment.getId());
        setMotivation(enrollment.getMotivation());
        setEnrollmentDateTime(DateHandler.toISOString(enrollment.getEnrollmentDateTime()));

        if (deepCopyActivity && (enrollment.getActivity() != null)) {
            setActivity(new ActivityDto(enrollment.getActivity(), false));
        }

        if (deepCopyVolunteer && (enrollment.getVolunteer() != null)) {
            setVolunteer(new UserDto(enrollment.getVolunteer()));
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMotivation() {
        return this.motivation;
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


    @Override
    public String toString() {
        return "EnrollementDto{" +
                "id=" + id +
                ", motivation='" + motivation + '\'' +
                ", enrollmentDateTime='" + enrollmentDateTime + '\'' +
                ", activity=" + activity +
                ", user=" + volunteer +
                '}';
    }
}
