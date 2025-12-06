// aiService.ts
import api from './api';

export interface GenerateTextRequest {
  prompt: string;
}

export interface GenerateTextResponse {
  response: string;
}

class AIService {
  private basePath = '/api/v1/ai';

  /**
   * Generate text using AI
   */
  public async generateText(prompt: string): Promise<GenerateTextResponse> {
    return await api.post<GenerateTextResponse>(`${this.basePath}/generate`, { prompt });
  }

  /**
   * Generate short text using AI
   */
  public async generateShortText(prompt: string): Promise<GenerateTextResponse> {
    return await api.post<GenerateTextResponse>(`${this.basePath}/generate/short`, { prompt });
  }
}

const aiService = new AIService();
export default aiService;
