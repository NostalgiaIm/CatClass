import { IPC_CHANNELS } from "./channels.js";
import { TimetableService } from "../services/TimetableService.js";

const timetableService = new TimetableService();

// Timetable IPC handlers exposed through preload only.
export function registerTimetableHandlers(ipcMain: Electron.IpcMain) {
  ipcMain.handle(IPC_CHANNELS.timetableListSpaces, () => {
    return timetableService.listSpaces();
  });
}
