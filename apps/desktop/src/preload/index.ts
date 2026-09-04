import { contextBridge, ipcRenderer } from "electron";
import type { TimetableSpace } from "../../../../packages/contracts/types/catclass.js";
import { IPC_CHANNELS } from "../main/ipc/channels.js";

export interface CatClassDesktopApi {
  ping(): string;
  listSpaces(): Promise<TimetableSpace[]>;
}

// Preload bridge: expose a tiny, typed API instead of Node or Electron primitives.
const api: CatClassDesktopApi = {
  ping: () => "CatClass desktop bridge ready",
  listSpaces: () => ipcRenderer.invoke(IPC_CHANNELS.timetableListSpaces),
};

contextBridge.exposeInMainWorld("catclass", api);

export {};
