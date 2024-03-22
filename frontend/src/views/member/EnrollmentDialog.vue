<template>
  <v-dialog v-model="dialog" persistent width="1300">
    <v-card>
      <v-card-title>
        <span class="headline">
          Create Enrollment
        </span>
      </v-card-title>
      <v-card-text>
        <v-form ref="form" lazy-validation>
          <v-row>
            <v-col cols="12">
              <v-text-field
                label="*Motivation"
                :rules="[
                  (v) =>
                    (v && v.length >= 10) ||
                    'Motivation must be at least 10 characters',
                ]"
                required
                v-model="createdEnrollment.motivation"
                data-cy="motivationInput"
              ></v-text-field>
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          color="blue-darken-1"
          variant="text"
          @click="$emit('close-enrollment-dialog')"
        >
          Close
        </v-btn>
        <v-btn
          color="blue darken-1"
          variant="text"
          @click="createEnrollment"
          data-cy="createEnrollment"
        >
          Create
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script lang="ts">
import { Vue, Component, Prop, Model } from 'vue-property-decorator';
import Activity from '@/models/activity/Activity';
import Theme from '@/models/theme/Theme';
import Enrollment from '@/models/enrollment/Enrollment';
import RemoteServices from '@/services/RemoteServices';
import VueCtkDateTimePicker from 'vue-ctk-date-time-picker';
import 'vue-ctk-date-time-picker/dist/vue-ctk-date-time-picker.css';
import { ISOtoString } from '@/services/ConvertDateService';

Vue.component('VueCtkDateTimePicker', VueCtkDateTimePicker);
@Component({
  methods: { ISOtoString },
})
export default class EnrollmentDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Enrollment, required: true }) readonly enrollment!: Enrollment;
  @Prop({ type: Activity, required: true }) readonly activity!: Activity;

  createdEnrollment: Enrollment = new Enrollment();
  enrollmentActivity: Activity = new Activity();

  cypressCondition: boolean = false;

  async created() {
    this.createdEnrollment = new Enrollment(this.enrollment);
    this.enrollmentActivity = new Activity(this.activity);
  }

  get canCreate(): boolean {
    return this.cypressCondition || !!this.createdEnrollment.motivation;
  }

  async createEnrollment() {
    if ((this.$refs.form as Vue & { validate: () => boolean }).validate()) {
      try {
        const enrollmentActivityId: number = this.enrollmentActivity?.id as number;

        const result = await RemoteServices.createEnrollment(
          this.$store.getters.getUser.id,
          enrollmentActivityId,
          this.createdEnrollment,
        );
        this.$emit('create-enrollment', { result, enrollmentActivityId });
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }
}
</script>

<style scoped lang="scss"></style>
