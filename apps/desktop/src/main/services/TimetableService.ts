import type { TimetableSpace } from "../../../../../packages/contracts/types/catclass.js";
import { DatabaseService } from "./DatabaseService.js";

// Timetable service: keeps business orchestration out of IPC handlers.
export class TimetableService {
  constructor(private readonly databaseService = new DatabaseService()) {}

  listSpaces(): TimetableSpace[] {
    return this.databaseService.listSpaces();
  }
}
