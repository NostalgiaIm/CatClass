# CatClass

<a href="README.zh.md">中文</a>

CatClass is a highly flexible cross-platform timetable app for desktop and Android. The current `Vera` branch focuses on the Electron + Vue + TypeScript desktop client and grows the product one usable feature slice at a time.

## Current Desktop Scope

The desktop app now includes an interactive timetable workspace:

- Switchable sidebar navigation for Timetable, Courses, Import, and Settings.
- Timetable grid with selectable cells.
- Course creation, editing, color selection, and deletion.
- Course fields for title, teacher, room, weekday, start period, duration, color, and notes.
- Multiple timetable spaces with a create timetable dialog.
- Courses view with editable course list actions.
- Import view with interactive placeholder actions for image timetable recognition.
- Settings view for timetable name, academic year, visible days, weekend display, compact mode, and period time editing.
- Electron preload bridge with `listSpaces` and `saveSpace` IPC APIs.
- Main-process in-memory timetable store. This is temporary and will later be replaced by SQLite persistence.

## Tech Stack

- Electron
- electron-vite
- Vue 3
- TypeScript
- Shared CatClass contracts package

## Start The Desktop App

From the repository root:

```bash
npm install
npm run desktop:dev
```

The Electron window should open automatically. If port `5173` is already occupied, Vite may choose another port such as `5174`; this is normal during development.

## Verify The Desktop App

```bash
npm run check
npm run desktop:build
```

`npm run check` runs the desktop TypeScript check. `npm run desktop:build` builds the Electron main process, preload bridge, and Vue renderer.

## Electron Download Troubleshooting

If Electron fails with `ECONNRESET` or `Electron uninstall`, the Electron package was installed but its runtime binary was not downloaded. On Windows PowerShell, retry with a mirror:

```powershell
$env:ELECTRON_MIRROR = "https://npmmirror.com/mirrors/electron/"
node node_modules/electron/install.js
npm run desktop:dev
```

## Repository Layout

```text
apps/desktop/       Electron + Vue desktop client
apps/android/       Android client scaffold
packages/contracts/ Shared schema and TypeScript contracts
```

## Not Included Yet

- SQLite persistence for desktop data.
- Production packaging and auto-update.
- Full OCR image timetable recognition workflow.
- Cross-device sync.
- Android and desktop feature parity.

## Development Notes

Desktop editing currently saves to an in-memory store inside the Electron main process. Data will reset after the app process exits. This keeps the current branch small while the UI, IPC, and domain contracts settle.
