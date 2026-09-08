import type { TimetableSpace } from "../../../../../packages/contracts/types/catclass.js";
import { DatabaseService } from "./DatabaseService.js";

// Timetable service: IPC handler 只负责转发，业务编排统一放在这一层。
export class TimetableService {
  constructor(private readonly databaseService = new DatabaseService()) {}

  listSpaces(): TimetableSpace[] {
    return this.databaseService.listSpaces();
  }

  saveSpace(space: TimetableSpace): TimetableSpace {
    return this.databaseService.saveSpace(space);
  }
}
