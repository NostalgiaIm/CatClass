// 统一管理 IPC 通道名，避免字符串散落各处
export const IPC_CHANNELS = {
  timetableListSpaces: "timetable:listSpaces",
  timetableGetSpace: "timetable:getSpace",
  timetableSaveCourse: "timetable:saveCourse",
  timetableDeleteCourse: "timetable:deleteCourse",
  importExportImportJson: "importExport:importJson",
  importExportExportJson: "importExport:exportJson",
  visionImportImage: "vision:importImage",
  visionGetJob: "vision:getJob",
  visionCommitDraft: "vision:commitDraft",
  syncGetStatus: "sync:getStatus",
  syncRunOnce: "sync:runOnce",
} as const;

