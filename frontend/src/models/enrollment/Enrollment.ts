import { ISOtoString } from '@/services/ConvertDateService';

export default class Enrollment {
  id: number | null = null;
  motivation!: string;
  enrollmentDateTime!: string;
  activityId!: number;
  volunteerName!: string;
  participating!: boolean;

  constructor(jsonObj?: Enrollment) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.motivation = jsonObj.motivation;
      this.enrollmentDateTime = ISOtoString(jsonObj.enrollmentDateTime);
      this.activityId = jsonObj.activityId;
      this.volunteerName = jsonObj.volunteerName;
      this.participating = jsonObj.participating;
    }
  }
}
