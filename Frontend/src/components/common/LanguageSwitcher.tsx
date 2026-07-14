"use client";

import React from 'react';
import { useTranslation } from 'react-i18next';
import { ChevronDownSmIcon } from '@/icons';

const LanguageSwitcher: React.FC = () => {
  const { i18n, t } = useTranslation('common');

  const toggleLanguage = () => {
    const newLang = i18n.language === 'en' ? 'fa' : 'en';
    i18n.changeLanguage(newLang);
  };

  return (
    <button
      onClick={toggleLanguage}
      className="flex items-center justify-center w-10 h-10 rounded-full bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors"
      aria-label={t('language.switch')}
    >
      <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
        {i18n.language === 'en' ? 'FA' : 'EN'}
      </span>
    </button>
  );
};

export default LanguageSwitcher;