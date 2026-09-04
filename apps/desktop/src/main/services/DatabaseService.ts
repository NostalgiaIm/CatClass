import type {
  CourseTemplate,
  EntityMeta,
  Period,
  ScheduleRule,
  Term,
  TimeTemplate,
  TimetableSpace,
} from "../../../../../packages/contracts/types/catclass.js";

const seedTimestamp = "2026-09-04T00:00:00.000Z";
const defaultTermId = "term-2026-fall";

function meta(id: string): EntityMeta {
  return {
    id,
    createdAt: seedTimestamp,
    updatedAt: seedTimestamp,
    version: 1,
  };
}

function createRule(id: string, dayOfWeek: number, startPeriod: number, periodCount: number): ScheduleRule {
  return {
    ...meta(id),
    termId: defaultTermId,
    dayOfWeek,
    startPeriod,
    periodCount,
    weekMode: "all",
    weekSet: [],
    exceptions: [],
  };
}

const defaultPeriods: Period[] = [
  { index: 1, label: "Period 1", startTime: "08:00", endTime: "08:45" },
  { index: 2, label: "Period 2", startTime: "08:55", endTime: "09:40" },
  { index: 3, label: "Period 3", startTime: "10:00", endTime: "10:45" },
  { index: 4, label: "Period 4", startTime: "10:55", endTime: "11:40" },
  { index: 5, label: "Period 5", startTime: "14:00", endTime: "14:45" },
  { index: 6, label: "Period 6", startTime: "14:55", endTime: "15:40" },
  { index: 7, label: "Period 7", startTime: "16:00", endTime: "16:45" },
  { index: 8, label: "Period 8", startTime: "16:55", endTime: "17:40" },
];

const defaultTerm: Term = {
  ...meta(defaultTermId),
  name: "Fall 2026",
  startDate: "2026-09-01",
  endDate: "2027-01-15",
  totalWeeks: 20,
  timezone: "Asia/Shanghai",
};

const defaultTimeTemplate: TimeTemplate = {
  ...meta("time-template-standard"),
  name: "Standard day",
  periods: defaultPeriods,
};

const defaultCourses: CourseTemplate[] = [
  {
    ...meta("course-product-design"),
    title: "Product Design Studio",
    teacher: "Prof. Lin",
    location: "Studio A",
    color: "#2563EB",
    tags: ["studio"],
    scheduleRules: [createRule("rule-product-design-mon", 1, 1, 2)],
    reminderRules: [],
  },
  {
    ...meta("course-algorithms"),
    title: "Algorithms",
    teacher: "Dr. Chen",
    location: "Room B204",
    color: "#0F766E",
    tags: ["core"],
    scheduleRules: [createRule("rule-algorithms-tue", 2, 3, 2)],
    reminderRules: [],
  },
  {
    ...meta("course-ux-research"),
    title: "UX Research",
    teacher: "Mira Zhou",
    location: "Lab 3",
    color: "#C2410C",
    tags: ["research"],
    scheduleRules: [createRule("rule-ux-thu", 4, 5, 2)],
    reminderRules: [],
  },
  {
    ...meta("course-open-lab"),
    title: "Open Lab",
    teacher: null,
    location: "Innovation Hub",
    color: "#7C3AED",
    tags: ["practice"],
    scheduleRules: [createRule("rule-open-lab-fri", 5, 7, 1)],
    reminderRules: [],
  },
];

const defaultSpace: TimetableSpace = {
  ...meta("space-default"),
  name: "Default Timetable",
  academicYear: "2026-2027",
  weekStartDay: 1,
  activeTermId: defaultTermId,
  terms: [defaultTerm],
  timeTemplates: [defaultTimeTemplate],
  courses: defaultCourses,
  settings: {
    visibleDays: [1, 2, 3, 4, 5],
    showWeekend: false,
    compactMode: false,
  },
};

// Data service: seed data first, then replace this boundary with SQLite persistence.
export class DatabaseService {
  listSpaces(): TimetableSpace[] {
    return [defaultSpace];
  }
}
