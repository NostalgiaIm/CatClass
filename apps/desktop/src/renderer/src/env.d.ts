/// <reference types="vite/client" />

import type { TimetableSpace } from "../../../../../packages/contracts/types/catclass.js";

declare global {
  interface Window {
    catclass?: {
      ping: () => string;
      listSpaces: () => Promise<TimetableSpace[]>;
    };
  }
}

export {};
