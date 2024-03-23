describe('SelectParticipant', () => {
    
    const applicationDeadline = '2024-02-06 17:58:21.402146';
    const creation_date = '2024-01-06 17:58:21.402146';
    const ending_date = '2024-02-08 17:58:21.402146';
    const region = 'Lisbon';
    const starting_date = '2024-02-07 17:58:21.402146';
    const state = 'APPROVED'
    const institution_id = 1;
    
    const ACTIVITIES = [
        {id: 1, description: 'Has vacancies', name: 'A1', participants_number_limit: 2},
        {id: 2, description: 'Has no vacancies', name: 'A2', participants_number_limit: 1},
    ];

    const ENROLLMENTS = [
        {id: 1, enrollment_date_time: '2024-02-06 18:51:37.595713', motivation: 'Has vacancies and do not participate', activity_id: 1, volunteer_id: 3},
        {id: 2, enrollment_date_time: '2024-02-06 19:51:37.595713', motivation: 'Has vacancies and participate', activity_id: 1, volunteer_id: 4},
        {id: 3, enrollment_date_time: '2024-02-06 18:51:37.595713', motivation: 'Has no vacancies and participate', activity_id: 2, volunteer_id: 3},
        {id: 4, enrollment_date_time: '2024-02-06 20:51:37.595713', motivation: 'Has no vacancies and do not participate', activity_id: 2, volunteer_id: 5},
    ];

    const PARTICIPATIONS = [
        {id: 5, acceptance_date: '2024-02-06 18:51:37.595713', rating: 5, activity_id: 1, volunteer_id: 4},
        {id: 6, acceptance_date: '2024-02-06 18:51:37.595713', rating: 5, activity_id: 2, volunteer_id: 3},
    ];
    
    beforeEach(() => {

      cy.deleteAllButArs();
      cy.createDemoEntities();

      for(const activity of ACTIVITIES) {
        cy.createActivity(activity.id, applicationDeadline, creation_date, activity.description, 
            ending_date, activity.name, activity.participants_number_limit, region, starting_date, state, institution_id);
      }

      for(const enrollment of ENROLLMENTS) {
        cy.createEnrollment(enrollment.id, enrollment.enrollment_date_time, enrollment.motivation,
           enrollment.activity_id, enrollment.volunteer_id);
      }

      for(const participation of PARTICIPATIONS) {
        cy.createParticipation(participation.id, participation.acceptance_date, participation.rating, 
          participation.activity_id, participation.volunteer_id);
      }

    });
  

    afterEach(() => {
      cy.deleteAllButArs();
    });


    it('Select Participant', () => { 
      // TODO

    });
});