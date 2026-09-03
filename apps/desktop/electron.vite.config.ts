import { defineConfig } from "electron-vite";

// 桌面端构建配置入口
export default defineConfig({
  main: {
    build: {
      outDir: "dist/main",
    },
  },
  preload: {
    build: {
      outDir: "dist/preload",
    },
  },
  renderer: {
    build: {
      outDir: "dist/renderer",
    },
  },
});

