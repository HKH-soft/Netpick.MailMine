import api, { PageDTO } from './api';

export interface CalendarEvent {
  id: string;
  title: string;
  start: string;
  end?: string;
  allDay?: boolean;
  color?: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

class CalendarEventService {
  private basePath = '/api/v1/core/calendar-events';

  public async getAllEvents(page: number = 1): Promise<PageDTO<CalendarEvent>> {
    return await api.get<PageDTO<CalendarEvent>>(`${this.basePath}?page=${page}`);
  }

  public async getEventById(id: string): Promise<CalendarEvent> {
    return await api.get<CalendarEvent>(`${this.basePath}/${id}`);
  }

  public async createEvent(event: Partial<CalendarEvent>): Promise<CalendarEvent> {
    return await api.post<CalendarEvent>(this.basePath, event);
  }

  public async updateEvent(id: string, event: Partial<CalendarEvent>): Promise<CalendarEvent> {
    return await api.put<CalendarEvent>(`${this.basePath}/${id}`, event);
  }

  public async deleteEvent(id: string): Promise<void> {
    await api.delete(`${this.basePath}/${id}`);
  }
}

const calendarEventService = new CalendarEventService();
export default calendarEventService;
