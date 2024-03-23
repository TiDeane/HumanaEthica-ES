const credentials = {
  user: Cypress.env('psql_db_username'),
  host: Cypress.env('psql_db_host'),
  database: Cypress.env('psql_db_name'),
  password: Cypress.env('psql_db_password'),
  port: Cypress.env('psql_db_port'),
};

const INSTITUTION_COLUMNS = "institutions (id, active, confirmation_token, creation_date, email, name, nif, token_generation_date)";
const USER_COLUMNS = "users (user_type, id, creation_date, name, role, state, institution_id)";
const AUTH_USERS_COLUMNS = "auth_users (auth_type, id, active, email, username, user_id)";
const ACTIVITY_COLUMNS = "activity (id, application_deadline, creation_date, description, ending_date, name, participants_number_limit, region, starting_date, state, institution_id)";
const ENROLLMENT_COLUMNS = "enrollment (id, enrollment_date_time, motivation, activity_id, volunteer_id)";
const PARTICIPATION_COLUMNS = "participation (id, acceptance_date, rating, activity_id, volunteer_id)";

const now = new Date();
const tomorrow = new Date(now);
tomorrow.setDate(now.getDate() + 1);
const dayAfterTomorrow = new Date(now);
dayAfterTomorrow.setDate(now.getDate() + 2);
const yesterday = new Date(now);
yesterday.setDate(now.getDate() - 1);
const dayBeforeYesterday = new Date(now);
dayBeforeYesterday.setDate(now.getDate() - 2);

Cypress.Commands.add('deleteAllButArs', () => {
  cy.task('queryDatabase', {
    query: "DELETE FROM ENROLLMENT",
    credentials: credentials,
  });
  cy.task('queryDatabase', {
    query: "DELETE FROM PARTICIPATION",
    credentials: credentials,
  });
  cy.task('queryDatabase', {
    query: "DELETE FROM ASSESSMENT",
    credentials: credentials,
  });
  cy.task('queryDatabase', {
    query: "DELETE FROM ACTIVITY",
    credentials: credentials,
  })
  cy.task('queryDatabase', {
    query: "DELETE FROM AUTH_USERS WHERE NOT (username = 'ars')",
    credentials: credentials,
  });
  cy.task('queryDatabase', {
    query: "DELETE FROM USERS WHERE NOT (name = 'ars')",
    credentials: credentials,
  });
  cy.task('queryDatabase', {
    query: "DELETE FROM INSTITUTIONS",
    credentials: credentials,
  });
});

Cypress.Commands.add('createDemoEntities', () => {
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + INSTITUTION_COLUMNS + generateInstitutionTuple(1),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + USER_COLUMNS + generateUserTuple(2, "MEMBER","DEMO-MEMBER", "MEMBER", 1),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + AUTH_USERS_COLUMNS + generateAuthUserTuple(2, "DEMO", "demo-member", 2),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + USER_COLUMNS + generateUserTuple(3, "VOLUNTEER","DEMO-VOLUNTEER", "VOLUNTEER", "NULL"),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + USER_COLUMNS + generateUserTuple(4, "VOLUNTEER","DEMO-VOLUNTEER2", "VOLUNTEER", "NULL"),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + USER_COLUMNS + generateUserTuple(5, "VOLUNTEER","DEMO-VOLUNTEER3", "VOLUNTEER", "NULL"),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + AUTH_USERS_COLUMNS + generateAuthUserTuple(3, "DEMO", "demo-volunteer", 3),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + AUTH_USERS_COLUMNS + generateAuthUserTuple(4, "DEMO", "demo-volunteer-2", 4),
    credentials: credentials,
  })
  cy.task('queryDatabase',  {
    query: "INSERT INTO " + AUTH_USERS_COLUMNS + generateAuthUserTuple(5, "DEMO", "demo-volunteer-3", 5),
    credentials: credentials,
  })
});

Cypress.Commands.add(
  'createActivity',
  (id, application_deadline, creation_date, description, ending_date, name, participants_number_limit, region, starting_date, state, institution_id) => {
  cy.task('queryDatabase', {
    query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(id, application_deadline, creation_date, description, ending_date, name, participants_number_limit, region, starting_date, state, institution_id),
    credentials: credentials,
  })
})

Cypress.Commands.add(
  'createEnrollment',
  (id, enrollment_date_time, motivation, activity_id, volunteer_id) => {
  cy.task('queryDatabase', {
    query: "INSERT INTO " + ENROLLMENT_COLUMNS + generateEnrollmentTuple(id, enrollment_date_time, motivation, activity_id, volunteer_id),
    credentials: credentials,
  })
})

Cypress.Commands.add(
  'createParticipation',
  (id, acceptance_date, rating, activity_id, volunteer_id) => {
  cy.task('queryDatabase', {
    query: "INSERT INTO " + PARTICIPATION_COLUMNS + generateParticipationTuple(id, acceptance_date, rating, activity_id, volunteer_id),
    credentials: credentials,
  })
  }
)

function generateAuthUserTuple(id, authType, username, userId) {
  return "VALUES ('"
    + authType + "', '"
    + id + "', 't', 'demo_member@mail.com','"
    + username + "', '"
    + userId + "')"
}

function generateUserTuple(id, userType, name, role, institutionId) {
  return "VALUES ('"
    + userType + "', '"
    + id + "', '2022-02-06 17:58:21.419878', '"
    + name + "', '"
    + role + "', 'ACTIVE', "
    + institutionId + ")";
}

function generateInstitutionTuple(id) {
  return "VALUES ('"
    + id + "', 't', 'abca428c09862e89', '2022-08-06 17:58:21.402146','demo_institution@mail.com', 'DEMO INSTITUTION', '000000000', '2024-02-06 17:58:21.402134')";
}

Cypress.Commands.add(
  'createActivity',
  (id, application_deadline, creation_date, description, ending_date, name, participants_number_limit, region, starting_date, state, institution_id) => {
    cy.task('queryDatabase', {
      query: "INSERT INTO " + ACTIVITY_COLUMNS + generateActivityTuple(id, application_deadline, creation_date, description, ending_date, name, participants_number_limit, region, starting_date, state, institution_id),
      credentials: credentials,
    })
  })

Cypress.Commands.add(
  'createEnrollment',
  (id, enrollment_date_time, motivation, activity_id, volunteer_id) => {
    cy.task('queryDatabase', {
      query: "INSERT INTO " + ENROLLMENT_COLUMNS + generateEnrollmentTuple(id, enrollment_date_time, motivation, activity_id, volunteer_id),
      credentials: credentials,
    })
  })

function generateActivityTuple(id, application_deadline, creation_date, description, ending_date, name, participants_number_limit, region, starting_date, state, institution_id) {
  return "VALUES ('"
    + id + "', '"
    + application_deadline + "', '"
    + creation_date + "', '"
    + description + "', '"
    + ending_date + "', '"
    + name + "', '"
    + participants_number_limit + "', '"
    + region + "', '"
    + starting_date + "', '"
    + state + "', "
    + institution_id + ")";
}

function generateEnrollmentTuple(id, enrollment_date_time, motivation, activity_id, volunteer_id) {
  return "VALUES ('"
    + id + "', '"
    + enrollment_date_time + "', '"
    + motivation + "', '"
    + activity_id + "', "
    + volunteer_id + ")";
}

function generateParticipationTuple(id, acceptance_date, rating, activity_id, volunteer_id) {
  return "VALUES ('"
    + id +  "', '"
    + acceptance_date +  "', '"
    + rating +  "', '"
    + activity_id +  "', "
    + volunteer_id +  ")";
}

Cypress.Commands.add(
    'initializeEnrollmentTest', () => {
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

      cy.createActivity(ACTIVITIES[0].id, applicationDeadline, creation_date, ACTIVITIES[0].description, ending_date,
          ACTIVITIES[0].name, ACTIVITIES[0].participants_number_limit, region, starting_date, state, institution_id);
      cy.createActivity(ACTIVITIES[1].id, applicationDeadline, creation_date, ACTIVITIES[1].description, ending_date,
          ACTIVITIES[1].name, ACTIVITIES[1].participants_number_limit, region, starting_date, state, institution_id);
      cy.createActivity(ACTIVITIES[2].id, applicationDeadlinePassed, creation_date, ACTIVITIES[2].description, ending_date,
          ACTIVITIES[2].name, ACTIVITIES[2].participants_number_limit, region, starting_date, state, institution_id);

      cy.createEnrollment(5, enrollment_date_time, motivation, 2, 3);
    }
)
