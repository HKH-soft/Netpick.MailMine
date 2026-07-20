import { describe, it, expect, vi, beforeEach } from 'vitest';
import PipelineService from './pipelineService';
import api, { PageDTO } from './api';

vi.mock('./api', () => ({
  default: {
    get: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('PipelineService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('calls getAllPipelines with correct endpoint', async () => {
    const mockResponse: PageDTO<{ id: string; stage: string }> = {
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

    const result = await PipelineService.getAllPipelines(1);

    expect(api.get).toHaveBeenCalledWith(expect.stringContaining('/pipelines'));
    expect(result).toEqual(mockResponse);
  });

  it('calls getPipelineById with correct endpoint', async () => {
    const mockPipeline = { id: '123', stage: 'SCRAPING', state: 'ACTIVE' };
    (api.get as any).mockResolvedValue(mockPipeline);

    const result = await PipelineService.getPipelineById('123');

    expect(api.get).toHaveBeenCalledWith(expect.stringContaining('/pipelines/123'));
    expect(result).toEqual(mockPipeline);
  });

  it('calls getStats with correct endpoint', async () => {
    (api.get as any).mockResolvedValue({ total: 50, active: 20, completed: 10, failed: 5, totalContactsFound: 500 });

    const result = await PipelineService.getStats();

    expect(api.get).toHaveBeenCalledWith(expect.stringContaining('/pipelines/stats'));
    expect(result).toEqual({ total: 50, active: 20, completed: 10, failed: 5, totalContactsFound: 500 });
  });

  it('calls deletePipeline correctly', async () => {
    (api.delete as any).mockResolvedValue(undefined);

    await PipelineService.deletePipeline('123');

    expect(api.delete).toHaveBeenCalledWith(expect.stringContaining('/pipelines/123'));
  });

  it('calls restorePipeline correctly', async () => {
    (api.put as any).mockResolvedValue(undefined);

    await PipelineService.restorePipeline('123');

    expect(api.put).toHaveBeenCalledWith(expect.stringContaining('/pipelines/123/restore'), {});
  });
});