import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { usePipelines, usePipeline } from './usePipelines';
import PipelineService from '@/services/pipelineService';
import { PageDTO } from '@/services/api';

vi.mock('@/services/pipelineService', () => ({
  default: {
    getAllPipelines: vi.fn(),
    getPipelineById: vi.fn(),
  },
}));

describe('usePipelines hook', () => {
  const mockPipelines = [
    { id: '1', stage: 'SCRAPING', state: 'ACTIVE', createdAt: '', updatedAt: '', itemsProcessed: 100, itemsTotal: 200 },
    { id: '2', stage: 'PARSING', state: 'ACTIVE', createdAt: '', updatedAt: '', itemsProcessed: 50, itemsTotal: 100 },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('fetches pipelines on successful request', async () => {
    const mockResponse: PageDTO<typeof mockPipelines[0]> = {
      content: mockPipelines,
      totalPages: 1,
      totalElements: 2,
      currentPage: 1,
      pageSize: 10,
      numberOfElements: 2,
      hasNext: false,
      hasPrevious: false,
      isFirst: true,
      isLast: true,
    };

    (PipelineService.getAllPipelines as any).mockResolvedValue(mockResponse);

    const { result } = renderHook(() => usePipelines(1));

    expect(result.current.loading).toBe(true);

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.pipelines).toEqual(mockPipelines);
    expect(result.current.error).toBeNull();
  });

  it('handles fetch error gracefully', async () => {
    (PipelineService.getAllPipelines as any).mockRejectedValue(new Error('Network error'));

    const { result } = renderHook(() => usePipelines(1));

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.error).toBe('Failed to fetch pipelines');
  });
});

describe('usePipeline hook', () => {
  const mockPipeline = { id: '1', stage: 'SCRAPING', state: 'ACTIVE', createdAt: '', updatedAt: '' };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('fetches pipeline by ID on successful request', async () => {
    (PipelineService.getPipelineById as any).mockResolvedValue(mockPipeline);

    const { result } = renderHook(() => usePipeline('1'));

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.pipeline).toEqual(mockPipeline);
    expect(result.current.error).toBeNull();
  });

  it('does not fetch when id is null', async () => {
    const { result } = renderHook(() => usePipeline(null));

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(PipelineService.getPipelineById).not.toHaveBeenCalled();
  });
});