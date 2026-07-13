// logger.ts - Environment-based logging utility

const isDevelopment = process.env.NODE_ENV === 'development';
const isProduction = process.env.NODE_ENV === 'production';

export const logger = {
  debug: (...args: unknown[]): void => {
    if (isDevelopment) {
      console.debug('[DEBUG]', ...args);
    }
  },

  info: (...args: unknown[]): void => {
    if (isDevelopment) {
      console.info('[INFO]', ...args);
    }
  },

  warn: (...args: unknown[]): void => {
    if (isDevelopment || isProduction) {
      console.warn('[WARN]', ...args);
    }
  },

  error: (...args: unknown[]): void => {
    // Always log errors, even in production
    console.error('[ERROR]', ...args);
  },
};

export default logger;