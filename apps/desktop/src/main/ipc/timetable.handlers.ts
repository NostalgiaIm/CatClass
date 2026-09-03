import { IPC_CHANNELS } from "./channels.js";
import { DatabaseService } from "../services/DatabaseService.js";

const databaseService = new DatabaseService();

// 课表相关 IPC 处理器
export function registerTimetableHandlers(ipcMain: Electron.IpcMain) {
  ipcMain.handle(IPC_CHANNELS.timetableListSpaces, () => {
    return databaseService.listSpaces();
  });
}

