export interface PromptConfig {
  idPromptConfig: number;
  promptTemplate: string;
  updatedAt: string | null;
  updatedBy: string | null;
}

export interface UpdatePromptRequest {
  promptTemplate: string;
}
