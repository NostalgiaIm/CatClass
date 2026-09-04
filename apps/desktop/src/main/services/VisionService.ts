import type { ImageImportJob } from "../../../../../packages/contracts/types/catclass.js";

// 图片识别服务：负责图片导入、OCR、结构解析与草稿生成
export class VisionService {
  importImage(pathOrUri: string): ImageImportJob {
    return {
      id: crypto.randomUUID(),
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      version: 1,
      source: {
        type: "file",
        uri: pathOrUri,
      },
      status: "pending",
      drafts: [],
    };
  }
}


