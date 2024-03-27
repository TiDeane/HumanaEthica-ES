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

        cy.logout();

    });
});
