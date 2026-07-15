/**
 * Utility for converting Arabic numerals to Persian (Farsi) numerals.
 * Persian numerals: ۰۱۲۳۴۵۶۷۸۹
 */

const PERSIAN_DIGITS: string[] = ['۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'];
const ARABIC_DIGITS: string[] = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9'];

/**
 * Convert Arabic numerals in a string to Persian numerals.
 */
export function toPersian(input: string | null | undefined): string {
  if (input == null) return '';
  return input.replace(/[0-9]/g, (digit) => PERSIAN_DIGITS[parseInt(digit, 10)]);
}

/**
 * Convert Persian numerals in a string to Arabic numerals.
 */
export function toArabic(input: string | null | undefined): string {
  if (input == null) return '';
  return input.replace(/[۰-۹]/g, (digit) => ARABIC_DIGITS[PERSIAN_DIGITS.indexOf(digit)].toString());
}

/**
 * Convert a number to Persian numeral string.
 */
export function numberToPersian(number: number): string {
  return toPersian(number.toString());
}

/**
 * Format a decimal number with Persian numerals.
 */
export function formatDecimal(number: number): string {
  return toPersian(number.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }));
}

/**
 * Format currency with Persian numerals.
 */
export function formatCurrency(number: number, currency: string = 'ریال'): string {
  return `${toPersian(number.toLocaleString('en-US'))} ${currency}`;
}