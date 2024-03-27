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
        cy.get('[data-cy="volunteerActivities"]').click();

        // Verify number of activities
        cy.get('[data-cy="volunteerActivitiesTable"] tbody tr')
            .should('have.length', 6);

        // Verify name of first activity
        cy.get('[data-cy="volunteerActivitiesTable"] tbody tr')
            .eq(0).children().eq(0).should('contain', NAME)

        // Create an assessment
        cy.get('[data-cy="createAssessmentButton"]').first().click();
        cy.get('[data-cy="reviewInput"]').type(REVIEW);
        cy.get('[data-cy="createAssessment"]').click();

        cy.logout();

        // As member
        cy.demoMemberLogin()

        // Go to assessments view
        cy.get('[data-cy="institution"]').click();
        cy.get('[data-cy="assessments"]').click();

        // Verify number of assessments
        cy.get('[data-cy="institutionAssessmentsTable"] tbody tr')
            .should('have.length', 1);

        // Verify motivation field
        cy.get('[data-cy="institutionAssessmentsTable"] tbody tr')
            .eq(0).children().eq(0).should('contain', REVIEW)

        cy.logout();

    });
});
