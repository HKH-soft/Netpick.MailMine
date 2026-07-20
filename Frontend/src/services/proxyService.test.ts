import { describe, it, expect, vi, beforeEach } from 'vitest';
import ProxyService from './proxyService';
import api, { PageDTO } from './api';

vi.mock('./api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    request: vi.fn(),
  },
}));

describe('ProxyService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('calls getAllProxies with correct endpoint', async () => {
    const mockResponse: PageDTO<{ id: string; host: string }> = {
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

    const result = await ProxyService.getAllProxies(1);

    expect(api.get).toHaveBeenCalledWith(expect.stringContaining('/proxies'));
    expect(result).toEqual(mockResponse);
  });

  it('calls getProxyById with correct endpoint', async () => {
    const mockProxy = { id: '123', host: 'proxy.example.com', port: 8080, protocol: 'HTTP' as const, status: 'ACTIVE' as const };
    (api.get as any).mockResolvedValue(mockProxy);

    const result = await ProxyService.getProxyById('123');

    expect(api.get).toHaveBeenCalledWith(expect.stringContaining('/proxies/123'));
    expect(result).toEqual(mockProxy);
  });

  it('calls createProxy correctly', async () => {
    const proxyRequest = { host: 'newproxy.com', port: 8080 };
    const mockProxy = { id: '123', host: 'newproxy.com', port: 8080, protocol: 'HTTP' as const, status: 'UNTESTED' as const };
    (api.post as any).mockResolvedValue(mockProxy);

    const result = await ProxyService.createProxy(proxyRequest);

    expect(api.post).toHaveBeenCalledWith(expect.stringContaining('/proxies'), proxyRequest);
    expect(result).toEqual(mockProxy);
  });

  it('calls updateProxy correctly', async () => {
    const proxyRequest = { host: 'updatedproxy.com', port: 9090 };
    (api.put as any).mockResolvedValue({});

    await ProxyService.updateProxy('123', proxyRequest);

    expect(api.put).toHaveBeenCalledWith(expect.stringContaining('/proxies/123'), proxyRequest);
  });

  it('calls deleteProxy correctly', async () => {
    (api.delete as any).mockResolvedValue(undefined);

    await ProxyService.deleteProxy('123');

    expect(api.delete).toHaveBeenCalledWith(expect.stringContaining('/proxies/123'));
  });

  it('calls getStats with correct endpoint', async () => {
    (api.get as any).mockResolvedValue({ total: 100, active: 80, inactive: 5, untested: 10, failed: 5 });

    const result = await ProxyService.getStats();

    expect(api.get).toHaveBeenCalledWith(expect.stringContaining('/proxies/stats'));
    expect(result).toEqual({ total: 100, active: 80, inactive: 5, untested: 10, failed: 5 });
  });

  it('calls testProxy correctly', async () => {
    (api.post as any).mockResolvedValue({});

    await ProxyService.testProxy('123');

    expect(api.post).toHaveBeenCalledWith(expect.stringContaining('/proxies/123/test'), {});
  });
});