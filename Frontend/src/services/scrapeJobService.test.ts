import { describe, it, expect, vi, beforeEach } from 'vitest';
import ScrapeJobService from './scrapeJobService';
import api, { PageDTO } from './api';

vi.mock('./api', () => ({
  default: {
    get: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('ScrapeJobService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('calls getAllScrapeJobs with correct endpoint', async () => {
    const mockResponse: PageDTO<{ id: string; link: string }> = {
      content: [],
      totalPages: 1,
      totalElements: 0,
      currentPage: 1,
      pageSize: 10,
      numberOfElements: 0,
      hasNext: false,
      hasPrevious: false,
      isFirst: true,
      isLast: true,
    };

    (api.get as any).mockResolvedValue(mockResponse);

    const result = await ScrapeJobService.getAllScrapeJobs(1);

    expect(api.get).toHaveBeenCalledWith(expect.stringContaining('/scrape_jobs'));
    expect(result).toEqual(mockResponse);
  });

  it('calls getScrapeJobById with correct endpoint', async () => {
    const mockJob = { id: '123', link: 'https://test.com' };
    (api.get as any).mockResolvedValue(mockJob);

    const result = await ScrapeJobService.getScrapeJobById('123');

    expect(api.get).toHaveBeenCalledWith(expect.stringContaining('/scrape_jobs/123'));
    expect(result).toEqual(mockJob);
  });

  it('calls getStats with correct endpoint', async () => {
    (api.get as any).mockResolvedValue({ total: 100, completed: 50, failed: 10, pending: 40 });

    const result = await ScrapeJobService.getStats();

    expect(api.get).toHaveBeenCalledWith(expect.stringContaining('/scrape_jobs/stats'));
    expect(result).toEqual({ total: 100, completed: 50, failed: 10, pending: 40 });
  });

  it('calls deleteScrapeJob correctly', async () => {
    (api.delete as any).mockResolvedValue(undefined);

    await ScrapeJobService.deleteScrapeJob('123');

    expect(api.delete).toHaveBeenCalledWith(expect.stringContaining('/scrape_jobs/123'));
  });

  it('calls restoreScrapeJob correctly', async () => {
    (api.put as any).mockResolvedValue(undefined);

    await ScrapeJobService.restoreScrapeJob('123');

    expect(api.put).toHaveBeenCalledWith(expect.stringContaining('/scrape_jobs/123/restore'), {});
  });
});