import { toPersian, toArabic, numberToPersian, formatDecimal, formatCurrency } from './persianNumbers';

describe('persianNumbers utilities', () => {
  describe('toPersian', () => {
    it('converts Arabic numerals to Persian numerals', () => {
      expect(toPersian('1234567890')).toBe('۱۲۳۴۵۶۷۸۹۰');
    });

    it('handles mixed content', () => {
      expect(toPersian('User 123 has 456 items')).toBe('User ۱۲۳ has ۴۵۶ items');
    });

    it('returns empty string for null input', () => {
      expect(toPersian(null)).toBe('');
    });

    it('returns empty string for undefined input', () => {
      expect(toPersian(undefined)).toBe('');
    });

    it('returns unchanged string with no digits', () => {
      expect(toPersian('hello world')).toBe('hello world');
    });

    it('handles empty string', () => {
      expect(toPersian('')).toBe('');
    });
  });

  describe('toArabic', () => {
    it('converts Persian numerals to Arabic numerals', () => {
      expect(toArabic('۰۱۲۳۴۵۶۷۸۹')).toBe('0123456789');
    });

    it('handles mixed content', () => {
      expect(toArabic('User ۱۲۳ has ۴۵۶ items')).toBe('User 123 has 456 items');
    });

    it('returns empty string for null input', () => {
      expect(toArabic(null)).toBe('');
    });

    it('returns empty string for undefined input', () => {
      expect(toArabic(undefined)).toBe('');
    });

    it('returns unchanged string with no Persian digits', () => {
      expect(toArabic('hello world')).toBe('hello world');
    });
  });

  describe('numberToPersian', () => {
    it('converts number to Persian numeral string', () => {
      expect(numberToPersian(1234567890)).toBe('۱۲۳۴۵۶۷۸۹۰');
    });

    it('handles zero', () => {
      expect(numberToPersian(0)).toBe('۰');
    });

    it('handles negative numbers', () => {
      expect(numberToPersian(-123)).toBe('-۱۲۳');
    });
  });

  describe('formatDecimal', () => {
    it('formats decimal number with Persian numerals', () => {
      const result = formatDecimal(1234.56);
      expect(result).toContain('۱٬۲۳۴');
      expect(result).toMatch(/.*٬.*۵۶/);
    });

    it('handles integer values', () => {
      expect(formatDecimal(1000)).toMatch(/۱٬۰۰۰/);
    });
  });

  describe('formatCurrency', () => {
    it('formats currency with default Iranian Rial', () => {
      const result = formatCurrency(1000);
      expect(result).toContain('ریال');
      expect(result).toContain('۱٬۰۰۰');
    });

    it('allows custom currency', () => {
      const result = formatCurrency(500, 'USD');
      expect(result).toContain('USD');
      expect(result).toContain('۵۰۰');
    });
  });
});