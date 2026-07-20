import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { useDeals, useDeal, useDealStats } from './useDeals';
import DealService, { Deal, DealStats } from '@/services/dealService';
import { PageDTO } from '@/services/api';

vi.mock('@/services/dealService', () => ({
  default: {
    getAllDeals: vi.fn(),
    getDealById: vi.fn(),
    getStats: vi.fn(),
  },
}));

describe('useDeals hook', () => {
  const mockDeals: Deal[] = [
    { id: '1', title: 'Deal 1', value: 1000, stage: 'QUALIFICATION', probability: 25, closeDate: '2024-08-15', currency: 'USD', createdAt: '', updatedAt: '' },
    { id: '2', title: 'Deal 2', value: 2000, stage: 'PROPOSAL', probability: 50, closeDate: '2024-09-30', currency: 'USD', createdAt: '', updatedAt: '' },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('fetches deals on successful request', async () => {
    const mockResponse: PageDTO<Deal> = {
      content: mockDeals,
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

    (DealService.getAllDeals as any).mockResolvedValue(mockResponse);

    const { result } = renderHook(() => useDeals(1));

    expect(result.current.loading).toBe(true);

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.deals).toEqual(mockDeals);
    expect(result.current.error).toBeNull();
  });

  it('handles fetch error gracefully', async () => {
    (DealService.getAllDeals as any).mockRejectedValue(new Error('Network error'));

    const { result } = renderHook(() => useDeals(1));

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.error).toBe('Failed to fetch deals');
  });
});

describe('useDeal hook', () => {
  const mockDeal: Deal = { id: '1', title: 'Deal 1', value: 1000, stage: 'QUALIFICATION', probability: 25, closeDate: '2024-08-15', currency: 'USD', createdAt: '', updatedAt: '' };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('fetches deal by ID on successful request', async () => {
    (DealService.getDealById as any).mockResolvedValue(mockDeal);

    const { result } = renderHook(() => useDeal('1'));

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.deal).toEqual(mockDeal);
    expect(result.current.error).toBeNull();
  });

  it('does not fetch when id is null', async () => {
    const { result } = renderHook(() => useDeal(null));

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(DealService.getDealById).not.toHaveBeenCalled();
  });
});

describe('useDealStats hook', () => {
  const mockStats: DealStats = { totalDeals: 10, totalValue: 50000, winRate: 15, dealsByStage: { QUALIFICATION: 5 } };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('fetches stats on mount', async () => {
    (DealService.getStats as any).mockResolvedValue(mockStats);

    const { result } = renderHook(() => useDealStats());

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.stats).toEqual(mockStats);
    expect(result.current.error).toBeNull();
  });
});