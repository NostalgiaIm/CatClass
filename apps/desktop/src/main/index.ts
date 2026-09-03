import { app, BrowserWindow } from "electron";
import { createMainWindow } from "./window/createMainWindow.js";

// 主进程入口：负责应用生命周期和窗口创建
app.whenReady().then(() => {
  createMainWindow();

  app.on("activate", () => {
    // macOS 场景下，点击 Dock 图标时恢复窗口
    if (BrowserWindow.getAllWindows().length === 0) {
      createMainWindow();
    }
  });
});

app.on("window-all-closed", () => {
  // macOS 保留菜单栏行为，其余平台关闭即退出
  if (process.platform !== "darwin") {
    app.quit();
  }
});

