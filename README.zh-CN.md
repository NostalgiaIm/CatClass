# CatClass

CatClass 是一款高度自由化的跨端课程表应用，目标覆盖桌面端和安卓端。当前 `Vera` 分支优先推进 Electron + Vue + TypeScript 桌面端，并按功能切片逐步完善。

## 当前桌面端范围

桌面端现在已经从静态预览推进到可交互课程表工作台：

- 左侧导航可在「课程表 / 课程 / 导入 / 设置」之间切换。
- 课程表网格支持点击格子选中位置。
- 支持新增、编辑、删除课程。
- 课程可编辑名称、教师、教室、星期、起始节次、连续节数、颜色和备注。
- 支持通过弹窗创建新的课表，并在多个课表之间切换。
- 「课程」栏目可查看课程列表，并通过按钮进入编辑。
- 「导入」栏目提供图片课表识别的交互占位，后续会接入 OCR 和草稿校对流程。
- 「设置」栏目可调整课表名称、学年、显示日期、周末显示、紧凑模式和节次时间。
- preload 桥接已暴露 `listSpaces` 和 `saveSpace` IPC API。
- 主进程已提供临时内存课表仓库，后续会替换为 SQLite 持久化。

## 技术栈

- Electron
- electron-vite
- Vue 3
- TypeScript
- CatClass 共享数据契约包

## 启动桌面端

在仓库根目录执行：

```bash
npm install
npm run desktop:dev
```

Electron 窗口会自动打开。如果 `5173` 端口已被旧进程占用，Vite 可能会自动切换到 `5174`，这是开发环境下的正常现象。

## 验证命令

```bash
npm run check
npm run desktop:build
```

`npm run check` 会执行桌面端 TypeScript 检查。`npm run desktop:build` 会构建 Electron 主进程、preload 桥接和 Vue 渲染层。

## Electron 下载问题处理

如果遇到 `ECONNRESET` 或 `Electron uninstall`，说明 Electron npm 包已经安装，但运行时二进制没有下载完整。Windows PowerShell 可以这样重试：

```powershell
$env:ELECTRON_MIRROR = "https://npmmirror.com/mirrors/electron/"
node node_modules/electron/install.js
npm run desktop:dev
```

## 仓库结构

```text
apps/desktop/       Electron + Vue 桌面端
apps/android/       Android 客户端骨架
packages/contracts/ 共享 Schema 和 TypeScript 类型契约
```

## 暂未包含

- 桌面端 SQLite 持久化。
- 生产打包和自动更新。
- 完整图片课程表 OCR 识别流程。
- 跨设备同步。
- 安卓端和桌面端功能完全对齐。

## 开发说明

当前桌面端编辑结果会保存到 Electron 主进程的内存仓库中，应用进程退出后数据会重置。这样可以先让 UI、IPC 和领域数据契约稳定下来，再接入正式数据库。
