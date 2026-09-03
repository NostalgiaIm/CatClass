// 这里先保留一个最小校验入口，后续可以接 JSON Schema / Zod
export function validatePayload<T>(payload: T): T {
  return payload;
}

