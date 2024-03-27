describe('Assessment', () => {
    beforeEach(() => {
        cy.deleteAllButArs();
        cy.createDemoEntities();
        cy.initializeAssessmentTest();
    });

    afterEach(() => {
        cy.deleteAllButArs();
    });

    it('create assessment', () => {
        const NAME = 'A1';
        const REVIEW = 'Review Example';

        // As volunteer
        cy.demoVolunteerLogin()

        // Go to activities view
        cy.intercept('GET', '/activities').as('getActivities');
        cy.get('[data-cy="volunteerActivities"]').click();
        cy.wait('@getActivities');

        // Verify number of activities
        cy.get('[data-cy="volunteerActivitiesTable"] tbody tr')
            .should('have.length', 6);

        // Verify name of first activity
        cy.get('[data-cy="volunteerActivitiesTable"] tbody tr')
            .eq(0).children().eq(0).should('contain', NAME)

        // Create an assessment
        cy.intercept('POST', '/institutions/*/assessments', (req) => {
        }).as('register');
        cy.get('[data-cy="createAssessmentButton"]').first().click();
        cy.get('[data-cy="reviewInput"]').type(REVIEW);
        cy.get('[data-cy="createAssessment"]').click();
        cy.wait('@register')

        cy.logout();

        // As member
        cy.demoMemberLogin()

        // Go to assessments view
        cy.intercept('GET', '/users/*/getInstitution').as('getInstitutions');
        cy.intercept('GET', '/institutions/*/assessments').as('assessments')
        cy.get('[data-cy="institution"]').click();
        cy.get('[data-cy="assessments"]').click();
        cy.wait('@getInstitutions');
        cy.wait('@assessments');

        // Verify number of assessments
        cy.get('[data-cy="institutionAssessmentsTable"] tbody tr')
            .should('have.length', 1);

        // Verify motivation field
        cy.get('[data-cy="institutionAssessmentsTable"] tbody tr')
            .eq(0).children().eq(0).should('contain', REVIEW)

        cy.logout();
    });
});
