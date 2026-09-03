import { contextBridge } from "electron";

// 预加载层：只向渲染进程暴露有限 API，避免直接接触 Node 能力
contextBridge.exposeInMainWorld("catclass", {
  ping: () => "CatClass",
});

export {};

