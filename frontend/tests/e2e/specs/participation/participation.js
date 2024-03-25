describe('SelectParticipant', () => {
  beforeEach(() => {
    cy.deleteAllButArs();
    cy.populateParticipationTest();
  });

  afterEach(() => {
    cy.deleteAllButArs();
  });

  it('Select Participant', () => {
    const rating = 5;

    cy.demoMemberLogin();

    cy.get('[data-cy="institution"]').click();
    cy.intercept('GET', '/users/*/getInstitution').as('getInstitutions');
    cy.intercept('GET', '/themes/availableThemes').as('availableTeams');

    cy.get('[data-cy="activities"]').click();
    cy.wait('@getInstitutions');
    cy.wait('@availableTeams');

    cy.get('[data-cy="memberActivitiesTable"] tbody tr').should(
      'have.length',
      2,
    );

    cy.get('[data-cy="memberActivitiesTable"] tbody tr')
      .eq(0)
      .children()
      .eq(4)
      .should('contain', 1);

    cy.intercept('GET', '/activities/*/enrollments').as('activityEnrollments');

    cy.get('[data-cy="showEnrollments"]').eq(0).click();
    cy.wait('@activityEnrollments');

    cy.get('[data-cy="activityEnrollmentsTable"] tbody tr').should(
      'have.length',
      2,
    );

    cy.get('[data-cy="activityEnrollmentsTable"] tbody tr')
      .eq(0)
      .children()
      .eq(2)
      .should('contain', false);

    cy.get('[data-cy="selectParticipant"]').eq(0).click();
    cy.get('[data-cy="ratingInput"]').type(rating);
    cy.get('[data-cy=saveParticipation]').click();

    cy.get('[data-cy="activityEnrollmentsTable"] tbody tr')
      .eq(0)
      .children()
      .eq(2)
      .should('contain', true);

    cy.get('[data-cy="getActivities"]').click();
    cy.wait('@getInstitutions');

    cy.get('[data-cy="memberActivitiesTable"] tbody tr')
      .eq(0)
      .children()
      .eq(4)
      .should('contain', 2);
  });
});
