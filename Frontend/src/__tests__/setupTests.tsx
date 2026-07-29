/* eslint-disable @next/next/no-img-element */
import '@testing-library/jest-dom';
import { vi } from 'vitest';

const createStoreMock = () => {
  const store: Record<string, string> = {};
  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => { store[key] = value; },
    removeItem: (key: string) => { delete store[key]; },
    clear: () => { Object.keys(store).forEach(k => delete store[k]); },
    get length() { return Object.keys(store).length; },
    key: (index: number) => Object.keys(store)[index] || null,
  };
};

if (typeof window !== 'undefined') {
  Object.defineProperty(window, 'localStorage', { value: createStoreMock(), configurable: true });
  Object.defineProperty(window, 'sessionStorage', { value: createStoreMock(), configurable: true });
}

// Mock Next.js router
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
    refresh: vi.fn(),
  }),
  usePathname: () => '/dashboard',
}));

// Mock i18next
vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => key,
    i18n: { changeLanguage: vi.fn() },
  }),
}));

// Mock Next.js fonts
vi.mock('next/font/google', () => ({
  Inter: () => ({ className: 'font-inter' }),
  Outfit: () => ({ className: 'font-outfit' }),
  Space_Grotesk: () => ({ className: 'font-space-grotesk', variable: '--font-space-grotesk' }),
  Vazirmatn: () => ({ className: 'font-vazirmatn', variable: '--font-vazirmatn' }),
}));

// Mock next/image
vi.mock('next/image', () => ({
  default: ({ src, alt, width, height }: { src: string; alt: string; width?: number; height?: number }) => {
    return <img src={src} alt={alt} width={width} height={height} />;
  },
}));

// Mock next/link
vi.mock('next/link', () => ({
  default: ({ href, children }: { href: string; children: React.ReactNode }) => <a href={href}>{children}</a>,
}));