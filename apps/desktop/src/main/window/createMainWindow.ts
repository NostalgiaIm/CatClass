import { BrowserWindow } from "electron";
import path from "node:path";

// Create the desktop shell. Dev loads Vite; production loads the built renderer.
export function createMainWindow() {
  const window = new BrowserWindow({
    width: 1280,
    height: 860,
    minWidth: 1024,
    minHeight: 720,
    title: "CatClass",
    webPreferences: {
      preload: path.join(__dirname, "../preload/index.mjs"),
      contextIsolation: true,
      nodeIntegration: false,
      // 先关闭沙盒：当前 preload 以 ESM 方式编译，沙盒化 preload 会阻止这类导入，桥接就不会暴露。
      // 后续如果要重新启用沙盒，需要把 preload 改成打包后的 CJS 方案。
      sandbox: false,
    },
  });

  if (process.env.ELECTRON_RENDERER_URL) {
    window.loadURL(process.env.ELECTRON_RENDERER_URL);
  } else {
    window.loadFile(path.join(__dirname, "../../renderer/index.html"));
  }

  return window;
}



