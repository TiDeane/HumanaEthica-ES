<template>
  <v-dialog v-model="dialog" persistent width="1300">
    <v-card>
      <v-card-title>
        <span class="headline">
          Create Assessment
        </span>
      </v-card-title>
      <v-card-text>
        <v-form ref="form" lazy-validation>
          <v-row>
            <v-col cols="12">
              <v-text-field
                label="*Review"
                :rules="[
                  (v) =>
                    (v && v.length >= 10) ||
                    'Review must be at least 10 characters',
                ]"
                required
                v-model="createdAssessment.review"
                data-cy="reviewInput"
              ></v-text-field>
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
            color="white"
            variant="text"
            @click="$emit('close-assessment-dialog')"
        >
          Close
        </v-btn>
        <v-btn
            v-if="reviewLength() >= 10"
            color="white"
            variant="text"
            @click="createAssessment"
            data-cy="createAssessment"
        >
          Save
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script lang="ts">

import { Vue, Component, Prop, Model } from 'vue-property-decorator';
import Assessment from '@/models/assessment/Assessment';
import Activity from '@/models/activity/Activity';
import RemoteServices from '@/services/RemoteServices';
import VueCtkDateTimePicker from 'vue-ctk-date-time-picker';
import 'vue-ctk-date-time-picker/dist/vue-ctk-date-time-picker.css';
import { ISOtoString } from '@/services/ConvertDateService';

Vue.component('VueCtkDateTimePicker', VueCtkDateTimePicker);
@Component({
  methods: { ISOtoString },
})
export default class AssessmentDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Assessment, required: true }) readonly assessment!: Assessment;
  @Prop({ type: Activity, required: true }) readonly activity!: Activity;

  createdAssessment: Assessment = new Assessment();

  cypressCondition: boolean = false;

  async created() {
    this.createdAssessment = new Assessment(this.assessment);
  }

  get canSave(): boolean {
    return this.cypressCondition || !!this.createdAssessment.review;
  }

  async createAssessment() {
    if ((this.$refs.form as Vue & { validate: () => boolean }).validate()) {
      try {
        const assessmentInstitutionId: number = this.activity.institution?.id as number;

        const result = await RemoteServices.createAssessment(
            this.$store.getters.getUser.id,
            assessmentInstitutionId,
            this.createdAssessment,
        );
        this.$emit('save-assessment', { result, assessmentInstitutionId });
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  reviewLength() {
    if (this.createdAssessment.review) {
      return this.createdAssessment.review.length;
    } else {
      return 0;
    }
  }
}
</script>

<style scoped lang="scss"></style>
