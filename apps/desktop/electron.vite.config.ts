import { defineConfig } from "electron-vite";
import vue from "@vitejs/plugin-vue";

// Desktop build entry. Main, preload, and renderer stay separated for Electron safety.
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
    root: "src/renderer",
    plugins: [vue()],
    build: {
      outDir: "../../dist/renderer",
    },
  },
});
