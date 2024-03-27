<template>
  <div>
    <v-card class="table">
      <v-data-table
        :headers="headers"
        :items="activities"
        :search="search"
        disable-pagination
        :hide-default-footer="true"
        :mobile-breakpoint="0"
        data-cy="volunteerActivitiesTable"
      >
        <template v-slot:top>
          <v-card-title>
            <v-text-field
              v-model="search"
              append-icon="search"
              label="Search"
              class="mx-2"
            />
            <v-spacer />
          </v-card-title>
        </template>
        <template v-slot:[`item.themes`]="{ item }">
          <v-chip v-for="theme in item.themes" v-bind:key="theme.id">
            {{ theme.completeName }}
          </v-chip>
        </template>
        <template v-slot:[`item.action`]="{ item }">
          <v-tooltip v-if="item.state === 'APPROVED'" bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                class="mr-2 action-button"
                color="red"
                v-on="on"
                data-cy="reportButton"
                @click="reportActivity(item)"
                >warning</v-icon
              >
            </template>
            <span>Report Activity </span>
          </v-tooltip>
          <v-tooltip v-if="item.state === 'APPROVED' && enrollmentDeadline(item) && !hasEnrolled(item) " bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                class="mr-2 action-button"
                color="blue"
                v-on=" on "
                data-cy="createEnrollmentButton"
                @click="newEnrollment(item)"
                >fa-solid fa-right-from-bracket</v-icon
              >
            </template>
            <span>Apply for Activity </span>
          </v-tooltip>
          <v-tooltip v-if="item.state === 'APPROVED' && isActivityterminated(item) && isAssessingFirstTimeInstitution(item) && hasOneParticipation(item)" bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                  class="mr-2 action-button"
                  color="blue"
                  v-on=" on "
                  data-cy="createAssessmentButton"
                  @click="newAssessment(item)"
              >fa-solid fa-pen-to-square</v-icon
              >
            </template>
            <span>Create Assessment</span>
          </v-tooltip>
        </template>
      </v-data-table>
      <enrollment-dialog
        v-if="currentEnrollment && editEnrollmentDialog"
        v-model="editEnrollmentDialog"
        :enrollment="currentEnrollment"
        :activity="currentActivity"
        v-on:create-enrollment="onCreateEnrollment"
        v-on:close-enrollment-dialog="onCloseEnrollmentDialog"
      />
      <assessment-dialog
          v-if="currentAssessment && editAssessmentDialog"
          v-model="editAssessmentDialog"
          :assessment="currentAssessment"
          :activity="currentActivity"
          v-on:save-assessment="onSaveAssessment"
          v-on:close-assessment-dialog="onCloseAssessmentDialog"
      />
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Activity from '@/models/activity/Activity';
import { show } from 'cli-cursor';
import EnrollmentDialog from '@/views/member/EnrollmentDialog.vue';
import Enrollment from '@/models/enrollment/Enrollment';
import AssessmentDialog from '@/views/volunteer/AssessmentDialog.vue';
import Assessment from '@/models/assessment/Assessment';
import Participation from '@/models/participation/Participation';

@Component({
  methods: { show },
  components: {
    'enrollment-dialog': EnrollmentDialog,
    'assessment-dialog': AssessmentDialog,
  },

})
export default class VolunteerActivitiesView extends Vue {
  activities: Activity[] = [];
  enrollments: Enrollment[] = [];
  assessments: Assessment[] = [];
  participations: Participation[] = [];
  search: string = '';

  currentEnrollment: Enrollment | null = null;
  currentActivity: Activity | null = null;
  currentAssessment: Assessment | null = null;
  editEnrollmentDialog: boolean = false;
  editAssessmentDialog: boolean = false;

  headers: object = [
    {
      text: 'Name',
      value: 'name',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Region',
      value: 'region',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Participants',
      value: 'participantsNumberLimit',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Themes',
      value: 'themes',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Description',
      value: 'description',
      align: 'left',
      width: '30%',
    },
    {
      text: 'State',
      value: 'state',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Start Date',
      value: 'formattedStartingDate',
      align: 'left',
      width: '5%',
    },
    {
      text: 'End Date',
      value: 'formattedEndingDate',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Application Deadline',
      value: 'formattedApplicationDeadline',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Actions',
      value: 'action',
      align: 'left',
      sortable: false,
      width: '5%',
    },
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      let userId = this.$store.getters.getUser.id;
      this.activities = await RemoteServices.getActivities();
      this.enrollments = await RemoteServices.getVolunteerEnrollments(userId);
      this.participations = await RemoteServices.getVolunteerParticipations(userId);
      this.assessments = await RemoteServices.getVolunteerAssessments(userId);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async reportActivity(activity: Activity) {
    if (activity.id !== null) {
      try {
        const result = await RemoteServices.reportActivity(
          this.$store.getters.getUser.id,
          activity.id,
        );
        this.activities = this.activities.filter((a) => a.id !== activity.id);
        this.activities.unshift(result);
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  newEnrollment(activity: Activity) {
    this.currentActivity = activity;
    this.currentEnrollment = new Enrollment();
    this.editEnrollmentDialog = true;
  }

  async onCreateEnrollment(enrollment: Enrollment, enrollmentActivityId: number) {
    const activity = this.activities.find(activity => activity.id === enrollmentActivityId);

    if (activity) {
      activity.enrollments.unshift(enrollment);
    }
    this.enrollments.unshift(enrollment);

    try {
      let userId = this.$store.getters.getUser.id;
      this.activities = await RemoteServices.getActivities();
      this.enrollments = await RemoteServices.getVolunteerEnrollments(userId);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }

    this.editEnrollmentDialog = false;
    this.currentEnrollment = null;
    this.currentActivity = null;
  }

  async onCloseEnrollmentDialog() {
    this.editEnrollmentDialog = false;
    this.currentEnrollment = null;
    this.currentActivity = null;
  }

  enrollmentDeadline(activity: Activity) {
    const currentDate = new Date();
    const DeadlineDate = new Date(activity.applicationDeadline);
    return currentDate <= DeadlineDate;
  }

  hasEnrolled(activity: Activity) {
    return this.enrollments.some(enrollment => enrollment.activityId === activity.id);
  }

  newAssessment(activity: Activity) {
    this.currentAssessment = new Assessment();
    this.currentActivity = activity;
    this.editAssessmentDialog = true;
  }

  async onSaveAssessment(assessment: Assessment, assessmentInstitutionId: number) {
    /* TODO */
  }

  async onCloseAssessmentDialog() {
    this.editAssessmentDialog = false;
    this.currentAssessment = null;
    this.currentActivity = null;
  }

  isActivityterminated(activity: Activity) {
    const currentDate = new Date();
    const DeadlineDate = new Date(activity.applicationDeadline);
    return currentDate > DeadlineDate;
  }

  isAssessingFirstTimeInstitution(activity: Activity) {
    if (!this.assessments) {
      return false;
    }
    return !this.assessments.some((a) => a.institutionId === activity.institution.id);
  }

  hasOneParticipation(activity: Activity) {
    if (!this.participations) {
      return false;
    }
    return this.participations.some((p) => p.activityId === activity.id);
  }
}
</script>

<style lang="scss" scoped></style>