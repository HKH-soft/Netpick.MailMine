import '@testing-library/jest-dom';
import { vi } from 'vitest';

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
  default: (props: any) => {
    const { src, alt, width, height } = props;
    return <img src={src} alt={alt} width={width} height={height} />;
  },
}));

// Mock next/link
vi.mock('next/link', () => ({
  default: ({ href, children }: any) => <a href={href}>{children}</a>,
}));