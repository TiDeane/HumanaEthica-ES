describe('Enrollment', () => {
    beforeEach(() => {
        cy.deleteAllButArs();
        cy.createDemoEntities();
        cy.initializeEnrollmentTest();
    });

    afterEach(() => {
        cy.deleteAllButArs();
    });

    it('Enrollment', () => {
        const MOTIVATION = 'new motivation';

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

        cy.intercept('GET', '/activities').as('getActivities');
        cy.get('[data-cy="volunteerActivities"]').click();
        cy.wait('@getActivities');

        cy.get('[data-cy="volunteerActivitiesTable"] tbody tr')
            .should('have.length', 3)

        cy.intercept('POST', '/activities/*/enrollments', (req) => {
            req.body = {
                motivation: MOTIVATION
            };
        }).as('register');

        cy.get('[data-cy="createEnrollmentButton"]').click();
        cy.get('[data-cy="motivationInput"]').type(MOTIVATION);
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
            .eq(0).children().eq(0).should('contain', MOTIVATION);

        cy.logout();
    });
});
