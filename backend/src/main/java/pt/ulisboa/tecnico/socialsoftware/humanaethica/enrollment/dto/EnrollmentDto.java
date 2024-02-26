package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.dto.ActivityDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

public class EnrollmentDto {
    private Integer id;
    private String motivation;
    private String enrollmentDateTime;

    private ActivityDto activity;
    private Volunteer volunteer;
    public EnrollmentDto(){
    }


    public EnrollmentDto(Enrollment enrollment, boolean deepCopyActivity){
        setId(enrollment.getId());
        setMotivation(enrollment.getMotivation());
        setEnrollmentDateTime(DateHandler.toISOString(enrollment.getEnrollmentDateTime()));
        setVolunteer(enrollment.getVolunteer());

        if (deepCopyActivity && (enrollment.getActivity() != null)) {
            setActivity(new ActivityDto(enrollment.getActivity(), false));
        }
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



    public ActivityDto getActivity() {
        return activity;
    }

    public void setActivity(ActivityDto activity) {
        this.activity = activity;
    }

    public Volunteer getVolunteer(){
        return volunteer;
    }

    public void setVolunteer(Volunteer volunteer){
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
