import type { TimetableSpace } from "../../../../../../packages/contracts/types/catclass.js";

// Renderer-visible API contract. It mirrors the preload bridge.
export interface CatClassApi {
  ping(): string;
  listSpaces(): Promise<TimetableSpace[]>;
  saveSpace(space: TimetableSpace): Promise<TimetableSpace>;
}
