/**
 * FormInput - Wiederverwendbare Input-Komponente mit React Hook Form Integration
 * 
 * Unterstützt automatische Fehleranzeige und verschiedene Input-Typen
 */

import React from 'react';
import { useFormContext, Controller } from 'react-hook-form';

const FormInput = ({
  name,
  label,
  type = 'text',
  placeholder,
  disabled = false,
  required = false,
  autoComplete,
  icon,
  helperText,
  className = '',
  ...rest
}) => {
  const {
    control,
    formState: { errors },
  } = useFormContext();

  const error = errors[name];

  return (
    <div className={`mb-4 ${className}`}>
      {/* Label */}
      {label && (
        <label htmlFor={name} className="block text-sm font-medium text-gray-700 mb-1">
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
      )}

      {/* Input-Container */}
      <div className="relative">
        {/* Icon (optional) */}
        {icon && (
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <span className="material-symbols-outlined text-gray-400 text-xl">{icon}</span>
          </div>
        )}

        {/* Input Field */}
        <Controller
          name={name}
          control={control}
          render={({ field }) => (
            <input
              {...field}
              id={name}
              type={type}
              placeholder={placeholder}
              disabled={disabled}
              autoComplete={autoComplete}
              className={`
                w-full px-4 py-2.5 border rounded-lg
                ${icon ? 'pl-10' : ''}
                ${error ? 'border-red-500 focus:ring-red-500 focus:border-red-500' : 'border-gray-300 focus:ring-primary focus:border-primary'}
                ${disabled ? 'bg-gray-100 cursor-not-allowed text-gray-500' : 'bg-white text-gray-900'}
                focus:outline-none focus:ring-2
                transition-colors duration-200
              `}
              {...rest}
            />
          )}
        />
      </div>

      {/* Helper Text (Info-Text unter dem Input) */}
      {helperText && !error && (
        <p className="mt-1 text-xs text-gray-500">{helperText}</p>
      )}

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

export default FormInput;
