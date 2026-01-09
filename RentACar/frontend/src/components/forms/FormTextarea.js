/**
 * FormTextarea - Wiederverwendbare Textarea-Komponente mit React Hook Form Integration
 * 
 * Unterstützt automatische Fehleranzeige und Zeichenzähler
 */

import React from 'react';
import { useFormContext, Controller } from 'react-hook-form';

const FormTextarea = ({
  name,
  label,
  placeholder,
  disabled = false,
  required = false,
  rows = 4,
  maxLength,
  helperText,
  showCharCount = false,
  className = '',
  ...rest
}) => {
  const {
    control,
    formState: { errors },
    watch,
  } = useFormContext();

  const error = errors[name];
  const fieldValue = watch(name) || '';
  const charCount = fieldValue.length;

  return (
    <div className={`mb-4 ${className}`}>
      {/* Label */}
      {label && (
        <label htmlFor={name} className="block text-sm font-medium text-gray-700 mb-1">
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
      )}

      {/* Textarea Field */}
      <Controller
        name={name}
        control={control}
        render={({ field }) => (
          <textarea
            {...field}
            id={name}
            placeholder={placeholder}
            disabled={disabled}
            rows={rows}
            maxLength={maxLength}
            className={`
              w-full px-4 py-2.5 border rounded-lg resize-none
              ${error ? 'border-red-500 focus:ring-red-500 focus:border-red-500' : 'border-gray-300 focus:ring-primary focus:border-primary'}
              ${disabled ? 'bg-gray-100 cursor-not-allowed text-gray-500' : 'bg-white text-gray-900'}
              focus:outline-none focus:ring-2
              transition-colors duration-200
            `}
            {...rest}
          />
        )}
      />

      {/* Helper Text oder Zeichenzähler */}
      <div className="mt-1 flex justify-between items-start">
        {/* Helper Text */}
        {helperText && !error && (
          <p className="text-xs text-gray-500">{helperText}</p>
        )}

        {/* Zeichenzähler */}
        {showCharCount && maxLength && (
          <p
            className={`text-xs ml-auto ${
              charCount > maxLength * 0.9 ? 'text-orange-500' : 'text-gray-400'
            }`}
          >
            {charCount} / {maxLength}
          </p>
        )}
      </div>

      {/* Error Message */}
      {error && (
        <div className="mt-1 flex items-start gap-1">
          <span className="material-symbols-outlined text-red-500 text-sm mt-0.5">error</span>
          <p className="text-xs text-red-600">{error.message}</p>
        </div>
      )}
    </div>
  );
};

export default FormTextarea;
