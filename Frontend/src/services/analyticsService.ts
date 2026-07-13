// analyticsService.ts
import api from './api';

export interface DailyStats {
  date: string;
  emailsReceived: number;
  emailsReplied: number;
  emailsRead: number;
  averageResponseTimeHours: number;
}

export interface WeeklyStats {
  period: string;
  totalReceived: number;
  totalReplied: number;
  dailyBreakdown: DailyStats[];
  topSenders: Array<{ email: string; count: number }>;
  unansweredCount: number;
}

export interface ResponseTimeMetrics {
  averageHours: number;
  medianHours: number;
  p95Hours: number;
  sampleSize: number;
}

export interface DashboardSummary {
  today: DailyStats;
  thisWeek: WeeklyStats;
  responseTime: ResponseTimeMetrics;
  volumeTrend: DailyStats[];
  unansweredCount: number;
  topSenders: Array<{ email: string; count: number }>;
}

class AnalyticsService {
  private basePath = '/api/v1/mailmine/analytics';

  async getDashboard(): Promise<DashboardSummary> {
    return await api.get<DashboardSummary>(`${this.basePath}/dashboard`);
  }

  async getDailyStats(date: string): Promise<DailyStats> {
    return await api.get<DailyStats>(`${this.basePath}/daily?date=${date}`);
  }

  async getWeeklyStats(): Promise<WeeklyStats> {
    return await api.get<WeeklyStats>(`${this.basePath}/weekly`);
  }

  async getResponseTimeMetrics(): Promise<ResponseTimeMetrics> {
    return await api.get<ResponseTimeMetrics>(`${this.basePath}/response-times`);
  }

  async getVolumeTrend(): Promise<DailyStats[]> {
    return await api.get<DailyStats[]>(`${this.basePath}/volume-trend`);
  }
}

const analyticsService = new AnalyticsService();
export default analyticsService;



