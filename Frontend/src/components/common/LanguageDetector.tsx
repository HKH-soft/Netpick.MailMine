"use client";

import { useEffect } from 'react';
import i18n from '@/i18n/config';

const LanguageDetector = () => {
  useEffect(() => {
    const updateDirection = () => {
      const currentLang = i18n.language || 'en';
      const isRTL = currentLang === 'fa';
      document.documentElement.dir = isRTL ? 'rtl' : 'ltr';
      document.documentElement.lang = currentLang;
    };

    updateDirection();
    i18n.on('languageChanged', updateDirection);

    return () => {
      i18n.off('languageChanged', updateDirection);
    };
  }, []);

  return null;
};

export default LanguageDetector;