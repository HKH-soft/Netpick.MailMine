import { Inter, Outfit, Space_Grotesk, Vazirmatn } from 'next/font/google';
import { cookies } from 'next/headers';
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
import ExtensionProviderWrapper from '@/components/common/ExtensionProviderWrapper';
import I18nProviderWrapper from '@/components/common/I18nProviderWrapper';

const outfit = Outfit({
  subsets: ["latin"],
});

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
});

const spaceGrotesk = Space_Grotesk({
  subsets: ["latin"],
  variable: "--font-heading",
  display: "swap",
});

const vazirmatn = Vazirmatn({
  subsets: ["latin", "arabic"],
  variable: "--font-vazirmatn",
});

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const cookieStore = await cookies();
  const lang = cookieStore.get('i18nextLng')?.value || 'en';
  const dir = lang === 'fa' ? 'rtl' : 'ltr';

  return (
    <html lang={lang} dir={dir}>
      <head>
        <link rel="icon" href="/images/Netpick-Platform/Netpick.svg" type="image/svg+xml" />
        <link rel="manifest" href="/manifest.json" />
        <meta name="theme-color" content="#011829" />
        <meta name="apple-mobile-web-app-capable" content="yes" />
        <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
        <link rel="apple-touch-icon" href="/images/Netpick-Platform/Netpick.svg" />
      </head>
<body className={`${outfit.className} ${inter.variable} ${spaceGrotesk.variable} ${vazirmatn.className}`} style={{ backgroundColor: 'var(--color-bg-base)', color: 'var(--color-text-primary)' }}>
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
        <I18nProviderWrapper>
          <LanguageDetector />
          <QueryProvider>
            <AuthProvider>
              <ThemeProvider>
                <ToastProvider>
                  <AuthProviderWrapper>
                    <InactivityHandler />
                    <ThemeProviderWrapper>
                      <SidebarProvider>
                        <ExtensionProviderWrapper>
                          <SidebarProviderWrapper>
                            {children}
                          </SidebarProviderWrapper>
                        </ExtensionProviderWrapper>
                      </SidebarProvider>
                    </ThemeProviderWrapper>
                  </AuthProviderWrapper>
                </ToastProvider>
              </ThemeProvider>
            </AuthProvider>
          </QueryProvider>
        </I18nProviderWrapper>
      </body>
    </html>
  );
}


