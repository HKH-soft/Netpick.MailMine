import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';
import { useSidebar, SidebarProvider } from './SidebarContext';
import React from 'react';

// Mock window.innerWidth before tests
Object.defineProperty(window, 'innerWidth', {
  writable: true,
  configurable: true,
  value: 1024,
});

describe('SidebarContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('throws error when used outside provider', () => {
    expect(() => {
      const { result } = renderHook(() => useSidebar());
    }).toThrow('useSidebar must be used within a SidebarProvider');
  });

  describe('with provider', () => {
    it('returns initial expanded state as true after initialization', async () => {
      const { result } = renderHook(() => useSidebar(), {
        wrapper: ({ children }) => React.createElement(SidebarProvider, null, children),
      });

      await waitFor(() => expect(result.current.isInitialized).toBe(true));
      expect(result.current.isExpanded).toBe(true);
    });

    it('toggles sidebar state', async () => {
      const { result } = renderHook(() => useSidebar(), {
        wrapper: ({ children }) => React.createElement(SidebarProvider, null, children),
      });

      await waitFor(() => expect(result.current.isInitialized).toBe(true));

      act(() => {
        result.current.toggleSidebar();
      });

      expect(result.current.isExpanded).toBe(false);
    });

    it('toggles mobile sidebar', async () => {
      const { result } = renderHook(() => useSidebar(), {
        wrapper: ({ children }) => React.createElement(SidebarProvider, null, children),
      });

      await waitFor(() => expect(result.current.isInitialized).toBe(true));

      act(() => {
        result.current.toggleMobileSidebar();
      });

      expect(result.current.isMobileOpen).toBe(true);
    });

    it('sets hovered state', async () => {
      const { result } = renderHook(() => useSidebar(), {
        wrapper: ({ children }) => React.createElement(SidebarProvider, null, children),
      });

      await waitFor(() => expect(result.current.isInitialized).toBe(true));

      act(() => {
        result.current.setIsHovered(true);
      });

      expect(result.current.isHovered).toBe(true);
    });

    it('toggles submenu', async () => {
      const { result } = renderHook(() => useSidebar(), {
        wrapper: ({ children }) => React.createElement(SidebarProvider, null, children),
      });

      await waitFor(() => expect(result.current.isInitialized).toBe(true));

      act(() => {
        result.current.toggleSubmenu('dashboard');
      });

      expect(result.current.openSubmenu).toBe('dashboard');

      act(() => {
        result.current.toggleSubmenu('dashboard');
      });

      expect(result.current.openSubmenu).toBeNull();
    });
  });
});