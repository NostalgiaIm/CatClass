import { BrowserWindow } from "electron";
import path from "node:path";

// 创建主窗口，后续会在这里接入 Vue 页面
export function createMainWindow() {
  const window = new BrowserWindow({
    width: 1280,
    height: 860,
    minWidth: 1024,
    minHeight: 720,
    title: "CatClass",
    webPreferences: {
      preload: path.join(__dirname, "../../preload/index.js"),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true,
    },
  });

  // 开发阶段先指向占位页面，后续替换为 Vite renderer 地址
  window.loadURL("data:text/html,<h1>CatClass Desktop</h1>");
  return window;
}

