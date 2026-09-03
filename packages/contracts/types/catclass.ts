export type WeekMode = "all" | "odd" | "even" | "custom";
export type CourseExceptionAction = "cancel" | "move" | "replaceNote" | "replaceLocation";
export type ReminderChannel = "system" | "silent" | "widget";
export type Platform = "desktop-windows" | "desktop-macos" | "desktop-linux" | "android";
export type VisionJobStatus = "pending" | "processing" | "reviewing" | "committed" | "failed";
export type VisionSourceType = "file" | "clipboard" | "screenshot" | "camera" | "pdf";
export type ChangeEntityType =
  | "space"
  | "term"
  | "timeTemplate"
  | "course"
  | "scheduleRule"
  | "courseException"
  | "reminderRule";
export type ChangeOperation = "create" | "update" | "delete";

export interface EntityMeta {
  id: string;
  createdAt: string;
  updatedAt: string;
  deletedAt?: string | null;
  version: number;
}

export interface CatClassExport {
  schemaVersion: 1;
  exportedAt: string;
  device: SyncDevice;
  spaces: TimetableSpace[];
  changeLogs?: ChangeLog[];
  imageImportJobs?: ImageImportJob[];
}

export interface ImageImportJob extends EntityMeta {
  source: VisionSource;
  status: VisionJobStatus;
  previewImage?: string | null;
  recognizedLayout?: RecognizedLayout | null;
  drafts: CourseDraft[];
  errors?: VisionIssue[];
}

export interface VisionSource {
  type: VisionSourceType;
  uri: string;
  mimeType?: string | null;
  width?: number | null;
  height?: number | null;
}

export interface RecognizedLayout {
  gridRows: number;
  gridColumns: number;
  cellBoxes: CellBox[];
  confidence: number;
}

export interface CellBox {
  row: number;
  column: number;
  rowSpan?: number;
  columnSpan?: number;
  text?: string;
  confidence?: number;
}

export interface RecognizedTextBlock {
  text: string;
  x: number;
  y: number;
  width: number;
  height: number;
  confidence: number;
}

export interface CourseDraft {
  id: string;
  title?: string | null;
  teacher?: string | null;
  location?: string | null;
  color?: string | null;
  note?: string | null;
  dayOfWeek?: number | null;
  startPeriod?: number | null;
  periodCount?: number | null;
  weekMode?: WeekMode | null;
  weekSet?: number[] | null;
  confidence: number;
  sourceCellIds: string[];
}

export interface ImportReviewState {
  jobId: string;
  selectedDraftIds: string[];
  rejectedDraftIds: string[];
  notes?: string | null;
}

export interface VisionIssue {
  code: string;
  message: string;
  severity: "info" | "warning" | "error";
}

export interface TimetableSpace extends EntityMeta {
  name: string;
  academicYear?: string | null;
  weekStartDay: number;
  activeTermId?: string | null;
  terms: Term[];
  timeTemplates: TimeTemplate[];
  courses: CourseTemplate[];
  settings?: SpaceSettings;
}

export interface Term extends EntityMeta {
  name: string;
  startDate: string;
  endDate: string;
  totalWeeks: number;
  timezone: string;
}

export interface TimeTemplate extends EntityMeta {
  name: string;
  periods: Period[];
}

export interface Period {
  index: number;
  label: string;
  startTime: string;
  endTime: string;
}

export interface CourseTemplate extends EntityMeta {
  title: string;
  teacher?: string | null;
  location?: string | null;
  color: string;
  note?: string | null;
  tags?: string[];
  scheduleRules: ScheduleRule[];
  reminderRules?: ReminderRule[];
}

export interface ScheduleRule extends EntityMeta {
  termId: string;
  dayOfWeek: number;
  startPeriod: number;
  periodCount: number;
  weekMode: WeekMode;
  weekSet?: number[];
  dateRange?: DateRange | null;
  exceptions?: CourseException[];
}

export interface DateRange {
  startDate?: string;
  endDate?: string;
}

export interface CourseException extends EntityMeta {
  date: string;
  action: CourseExceptionAction;
  replacement?: CourseReplacement | null;
}

export interface CourseReplacement {
  date?: string;
  dayOfWeek?: number;
  startPeriod?: number;
  periodCount?: number;
  location?: string | null;
  note?: string | null;
}

export interface ReminderRule extends EntityMeta {
  offsetMinutes: number;
  channel: ReminderChannel;
  enabled: boolean;
}

export interface SpaceSettings {
  visibleDays?: number[];
  showWeekend?: boolean;
  compactMode?: boolean;
}

export interface ChangeLog {
  id: string;
  deviceId: string;
  entityType: ChangeEntityType;
  entityId: string;
  opType: ChangeOperation;
  payload: Record<string, unknown>;
  baseVersion: number;
  createdAt: string;
}

export interface SyncDevice {
  id: string;
  deviceName: string;
  platform: Platform;
  lastSeenAt?: string | null;
  publicKey?: string | null;
}
