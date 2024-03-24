describe('Enrollment', () => {

    const applicationDeadline = '2024-08-06 17:58:21.402146';
    const applicationDeadlinePassed = '2024-02-06 17:58:21.402146';
    const creation_date = '2024-08-06 17:58:21.402146';
    const ending_date = '2024-08-08 17:58:21.402146';
    const region = 'Lisbon';
    const starting_date = '2024-08-07 17:58:21.402146';
    const state = 'APPROVED'
    const institution_id = 1;
    const motivation = 'sql-inserted-motivation';
    const enrollment_date_time = '2024-02-06 18:51:37.595713';

    const ACTIVITIES = [
        {id: 1, description: 'Enrollment is open', name: 'A1', participants_number_limit: 1},
        {id: 2, description: 'Enrollment is open and it is already enrolled', name: 'A2', participants_number_limit: 2},
        {id: 3, description: 'Enrollment is closed', name: 'A3', participants_number_limit: 3},
    ];

    beforeEach(() => {

        cy.deleteAllButArs();
        cy.createDemoEntities();

        cy.createActivity(ACTIVITIES[0].id, applicationDeadline, creation_date, ACTIVITIES[0].description, ending_date,
            ACTIVITIES[0].name, ACTIVITIES[0].participants_number_limit, region, starting_date, state, institution_id);
        cy.createActivity(ACTIVITIES[1].id, applicationDeadline, creation_date, ACTIVITIES[1].description, ending_date,
            ACTIVITIES[1].name, ACTIVITIES[0].participants_number_limit, region, starting_date, state, institution_id);
        cy.createActivity(ACTIVITIES[2].id, applicationDeadlinePassed, creation_date, ACTIVITIES[2].description, ending_date,
            ACTIVITIES[2].name, ACTIVITIES[2].participants_number_limit, region, starting_date, state, institution_id);

        cy.createEnrollment(5, enrollment_date_time, motivation, 2, 3);

    });


    afterEach(() => {
        cy.deleteAllButArs();
    });


    it('Enrollment', () => {

        cy.demoMemberLogin()
        cy.get('[data-cy="institution"]').click();

        cy.intercept('GET', '/users/*/getInstitution').as('getInstitutions');
        cy.intercept('GET', '/themes/availableThemes').as('availableTeams');

        cy.get('[data-cy="activities"]').click();
        cy.wait('@getInstitutions');
        cy.wait('@availableTeams');

        cy.get('[data-cy="memberActivitiesTable"] tbody tr')
            .should('have.length', 3);

        cy.get('[data-cy="memberActivitiesTable"] tbody tr')
            .eq(0).children().eq(3).should('contain', 0);

        cy.logout();

        cy.demoVolunteerLogin();
        // intercept get activities request
        cy.intercept('GET', '/activities').as('getActivities');
        // go to volunteer activities view
        cy.get('[data-cy="volunteerActivities"]').click();
        // check request was done
        cy.wait('@getActivities');
        // check results
        cy.get('[data-cy="volunteerActivitiesTable"] tbody tr')
            .should('have.length', 3)


        // Intercept create enrollment request
        cy.intercept('POST', '/activities/*/enrollments', (req) => {
            req.body = {
                motivation: motivation
            };
        }).as('register');

        cy.get('[data-cy="createEnrollmentButton"]').click();
        cy.get('[data-cy="motivationInput"]').type(motivation);
        cy.get('[data-cy="createEnrollment"]').click();
        cy.wait('@register')

        cy.logout();

        cy.demoMemberLogin();

        cy.get('[data-cy="institution"]').click();

        cy.intercept('GET', '/users/*/getInstitution').as('getInstitutions');
        cy.intercept('GET', '/themes/availableThemes').as('availableTeams');

        cy.get('[data-cy="activities"]').click();
        cy.wait('@getInstitutions');
        cy.wait('@availableTeams');

        cy.get('[data-cy="memberActivitiesTable"] tbody tr')
            .eq(0).children().eq(3).should('contain', 1);

        cy.intercept('GET', '/activities/*/enrollments').as('activityEnrollments');
        cy.get('[data-cy="showEnrollments"]').eq(0).click();
        cy.wait('@activityEnrollments');

        cy.get('[data-cy="activityEnrollmentsTable"] tbody tr').should('have.length', 1);

        cy.get('[data-cy="activityEnrollmentsTable"] tbody tr')
            .eq(0).children().eq(0).should('contain', motivation);

        cy.logout();
    });
});
