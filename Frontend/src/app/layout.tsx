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
      <body className={`${outfit.className} ${vazirmatn.className} dark:bg-gray-900`}>
        <LanguageDetector />
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


