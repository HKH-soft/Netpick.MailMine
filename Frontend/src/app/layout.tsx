import { Outfit } from 'next/font/google';
import './globals.css';

import { SidebarProvider } from '@/context/SidebarContext';
import { ThemeProvider } from '@/context/ThemeContext';
import { AuthProvider } from '@/context/AuthContext';
import { ToastProvider } from '@/context/ToastContext';
import ThemeProviderWrapper from '@/components/common/ThemeProviderWrapper';
import AuthProviderWrapper from '@/components/common/AuthProviderWrapper';
import SidebarProviderWrapper from '@/components/common/SidebarProviderWrapper';

const outfit = Outfit({
  subsets: ["latin"],
});

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="dark">
      <body className={`${outfit.className} dark:bg-gray-900`}>
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
        <AuthProvider>
          <ThemeProvider>
            <ToastProvider>
              <AuthProviderWrapper>
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
      </body>
    </html>
  );
}