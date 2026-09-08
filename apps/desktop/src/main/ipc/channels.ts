// 统一管理 IPC 通道名，避免主进程、preload、渲染层各自散落字符串。
export const IPC_CHANNELS = {
  timetableListSpaces: "timetable:listSpaces",
  timetableGetSpace: "timetable:getSpace",
  timetableSaveSpace: "timetable:saveSpace",
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
