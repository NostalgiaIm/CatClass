import { IPC_CHANNELS } from "./channels.js";
import { VisionService } from "../services/VisionService.js";

const visionService = new VisionService();

// 图片识别相关 IPC 处理器
export function registerVisionHandlers(ipcMain: Electron.IpcMain) {
  ipcMain.handle(IPC_CHANNELS.visionImportImage, (_event, payload: { pathOrUri: string }) => {
    return visionService.importImage(payload.pathOrUri);
  });
}

