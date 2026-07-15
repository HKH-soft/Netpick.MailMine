import { Outfit } from 'next/font/google';
import './globals.css';

import { SidebarProvider } from '@/context/SidebarContext';
import { ThemeProvider } from '@/context/ThemeContext';
import { AuthProvider } from '@/context/AuthContext';
import { ToastProvider } from '@/context/ToastContext';
import { QueryProvider } from '@/context/QueryProvider';
import ThemeProviderWrapper from '@/components/common/ThemeProviderWrapper';
import AuthProviderWrapper from '@/components/common/AuthProviderWrapper';
import SidebarProviderWrapper from '@/components/common/SidebarProviderWrapper';
import InactivityHandler from '@/components/auth/InactivityHandler';
import LanguageDetector from '@/components/common/LanguageDetector';
import PwaRegister from '@/components/common/PwaRegister';

const outfit = Outfit({
  subsets: ["latin"],
});

const vazirmatn = Outfit({
  subsets: ["latin"],
  variable: "--font-vazirmatn",
});

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="dark" dir="ltr">
      <head>
        <link rel="manifest" href="/manifest.json" />
        <meta name="theme-color" content="#1E40AF" />
        <meta name="apple-mobile-web-app-capable" content="yes" />
        <meta name="apple-mobile-web-app-status-bar-style" content="default" />
        <link rel="apple-touch-icon" href="/icons/icon-192x192.png" />
      </head>
      <body className={`${outfit.className} ${vazirmatn.className} dark:bg-gray-900`}>
        <LanguageDetector />
        <PwaRegister />
        <script
          dangerouslySetInnerHTML={{
            __html: `
              (function() {
                try {
                  const savedTheme = localStorage.getItem('theme');
                  const theme = savedTheme || 'dark';
                  if (theme === 'dark') {
                    document.documentElement.classList.add('dark');
                  } else {
                    document.documentElement.classList.remove('dark');
                  }
                } catch (e) {}
              })();
            `,
          }}
        />
        <QueryProvider>
          <AuthProvider>
            <ThemeProvider>
              <ToastProvider>
                <AuthProviderWrapper>
                  <InactivityHandler />
                  <ThemeProviderWrapper>
                    <SidebarProvider>
                      <SidebarProviderWrapper>
                        {children}
                      </SidebarProviderWrapper>
                    </SidebarProvider>
                  </ThemeProviderWrapper>
                </AuthProviderWrapper>
              </ToastProvider>
            </ThemeProvider>
          </AuthProvider>
        </QueryProvider>
      </body>
    </html>
  );
}


