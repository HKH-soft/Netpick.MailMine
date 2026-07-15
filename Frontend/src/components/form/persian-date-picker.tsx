import { useEffect, useRef } from 'react';
import flatpickr from 'flatpickr';
import 'flatpickr/dist/flatpickr.css';
import Label from './Label';
import { CalenderIcon } from '../../icons';
import { toPersian } from '../../utils/persianNumbers';
import Hook = flatpickr.Options.Hook;
import DateOption = flatpickr.Options.DateOption;

type PropsType = {
  id: string;
  mode?: "single" | "multiple" | "range" | "time";
  onChange?: Hook | Hook[];
  defaultDate?: DateOption;
  label?: string;
  placeholder?: string;
};

/**
 * Persian date picker with Persian numeral formatting.
 * Uses flatpickr with Persian calendar support.
 */
export default function PersianDatePicker({
  id,
  mode,
  onChange,
  label,
  defaultDate,
  placeholder,
}: PropsType) {
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!inputRef.current) return;

    const fp = flatpickr(inputRef.current, {
      mode: mode || "single",
      static: true,
      monthSelectorType: "static",
      dateFormat: "Y-m-d",
      defaultDate,
      locale: {
        firstDayOfWeek: 1,
        weekdays: {
          shorthand: ["یک", "دو", "سه", "چهار", "پنج", "جمعه", "شنبه"],
          longhand: ["یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه", "شنبه"]
        },
        months: {
          shorthand: ["فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"],
          longhand: ["فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"]
        }
      },
      onChange: (selectedDates, dateStr, instance) => {
        // Convert displayed date to Persian numerals
        if (inputRef.current && dateStr) {
          inputRef.current.value = toPersian(dateStr);
        }
        if (onChange) {
          if (Array.isArray(onChange)) {
            onChange.forEach(cb => cb(selectedDates, dateStr, instance));
          } else {
            onChange(selectedDates, dateStr, instance);
          }
        }
      },
    });

    return () => {
      if (fp && !Array.isArray(fp)) {
        fp.destroy();
      }
    };
  }, [mode, onChange, id, defaultDate]);

  return (
    <div>
      {label && <Label htmlFor={id}>{label}</Label>}

      <div className="relative">
        <input
          ref={inputRef}
          id={id}
          placeholder={placeholder}
          className="h-11 w-full rounded-lg border appearance-none px-4 py-2.5 text-sm shadow-theme-xs placeholder:text-gray-400 focus:outline-hidden focus:ring-3  dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30  bg-transparent text-gray-800 border-gray-300 focus:border-brand-300 focus:ring-brand-500/20 dark:border-gray-700  dark:focus:border-brand-800"
          dir="rtl"
        />

        <span className="absolute text-gray-500 -translate-y-1/2 pointer-events-none left-3 top-1/2 dark:text-gray-400">
          <CalenderIcon className="size-6" />
        </span>
      </div>
    </div>
  );
}