import { app, BrowserWindow, ipcMain } from "electron";
import { registerTimetableHandlers } from "./ipc/timetable.handlers.js";
import { registerVisionHandlers } from "./ipc/vision.handlers.js";
import { createMainWindow } from "./window/createMainWindow.js";

// Main process entry: owns app lifecycle, IPC registration, and window creation.
app.whenReady().then(() => {
  registerTimetableHandlers(ipcMain);
  registerVisionHandlers(ipcMain);
  createMainWindow();

  app.on("activate", () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createMainWindow();
    }
  });
});

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});
