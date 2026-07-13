// aiService.ts
import api from './api';

export interface GenerateTextRequest {
  prompt: string;
}

export interface GenerateTextResponse {
  response: string;
}

export interface CustomerStatus {
  customer: string;
  subject: string;
  emailCount: number;
  hoursSinceLastReply: number;
  awaitingReply: boolean;
  aiStatus: string;
  thread: Array<{
    id: string;
    subject: string;
    from: string;
    date: string;
    isRead: boolean;
  }>;
}

export interface SentimentResult {
  sentiment: 'positive' | 'neutral' | 'negative' | 'angry';
  confidence: number;
  urgency: 'low' | 'medium' | 'high' | 'critical';
  emotion: string;
  keyPhrases?: string[];
}

export interface SpamResult {
  is_spam: boolean;
  is_phishing: boolean;
  is_scam: boolean;
  risk_level: 'low' | 'medium' | 'high' | 'critical';
  confidence: number;
  threats?: string[];
  recommendation?: string;
}

class AIService {
  private basePath = '/api/v1/ai';
  private spamPath = '/api/v1/spam-detection';

  // === Text Generation ===
  public async generateText(prompt: string): Promise<GenerateTextResponse> {
    return await api.post<GenerateTextResponse>(`${this.basePath}/generate`, { prompt });
  }

  public async generateShortText(prompt: string): Promise<GenerateTextResponse> {
    return await api.post<GenerateTextResponse>(`${this.basePath}/generate/short`, { prompt });
  }

  // === Email Summaries ===
  async summarizeEmail(emailId: string): Promise<string> {
    return await api.get<string>(`${this.basePath}/email/${emailId}/summary`);
  }

  async summarizeThread(threadId: string): Promise<string> {
    return await api.get<string>(`${this.basePath}/thread/${threadId}/summary`);
  }

  async getCustomerStatus(emailId: string): Promise<CustomerStatus> {
    return await api.get<CustomerStatus>(`${this.basePath}/email/${emailId}/status`);
  }

  // === Draft Replies ===
  async generateDraft(emailId: string): Promise<string> {
    return await api.get<string>(`${this.basePath}/email/${emailId}/draft`);
  }

  async generateDraftWithTemplate(emailId: string, templateId: string): Promise<string> {
    return await api.get<string>(`${this.basePath}/email/${emailId}/draft/template/${templateId}`);
  }

  async generateSubjectSuggestions(emailId: string): Promise<string[]> {
    return await api.get<string[]>(`${this.basePath}/email/${emailId}/subject-suggestions`);
  }

  async improveDraft(draft: string, instructions: string): Promise<string> {
    return await api.post<string>(`${this.basePath}/draft/improve`, { draft, instructions });
  }

  // === Sentiment Analysis ===
  async analyzeSentiment(emailId: string): Promise<SentimentResult> {
    return await api.get<SentimentResult>(`${this.basePath}/email/${emailId}/sentiment`);
  }

  async analyzeThreadSentiment(threadId: string): Promise<Array<{ date: string; sentiment: string }>> {
    return await api.get(`${this.basePath}/thread/${threadId}/sentiment-trend`);
  }

  // === Spam Detection ===
  async detectSpam(emailId: string): Promise<SpamResult> {
    return await api.get<SpamResult>(`${this.spamPath}/email/${emailId}`);
  }

  async batchDetectSpam(emailIds: string[]): Promise<Array<{ emailId: string; result: SpamResult }>> {
    return await api.post(`${this.spamPath}/batch`, emailIds);
  }

  async checkSenderReputation(email: string): Promise<{ email: string; reputation: string; riskLevel: string }> {
    return await api.get(`${this.spamPath}/sender-reputation?email=${encodeURIComponent(email)}`);
  }
}

const aiService = new AIService();
export default aiService;
