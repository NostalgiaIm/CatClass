# CatClass

CatClass is a highly flexible cross-platform timetable app. This branch starts the desktop implementation one feature at a time.

## Current Desktop Feature

The first desktop slice is a read-only timetable preview:

- Electron main process creates the desktop window.
- Preload exposes a small `window.catclass` API.
- Renderer calls IPC through the preload bridge.
- Vue displays a seeded timetable space, week grid, and course list.

This is intentionally small. It proves the desktop path works before adding editing, storage, import, sync, and image recognition.

## Tech Stack

- Electron
- electron-vite
- Vue 3
- TypeScript
- Shared CatClass contract package

## Start The Desktop App

From the repository root:

```bash
npm install
npm run desktop:dev
```

The Electron window should open automatically. You should see:

- The CatClass sidebar
- A `Default Timetable` workspace
- A week grid with seeded courses
- A course list on the right
- A bridge status line that says `CatClass desktop bridge ready`

## Useful Commands

```bash
npm run desktop:dev
npm run desktop:build
npm run check
```

## Repository Layout

```text
apps/desktop/       Electron + Vue desktop client
apps/android/       Android client scaffold
packages/contracts/ Shared schema and TypeScript contracts
```

## Not Included Yet

- Desktop SQLite persistence
- Course editing on desktop
- Image timetable recognition on desktop
- Sync service
- Production packaging
