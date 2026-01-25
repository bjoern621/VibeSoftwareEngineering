import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  validateEmail,
  validatePassword,
  validatePasswordMatch,
  validateRequired,
  calculatePasswordStrength,
} from '../utils/validation';

/**
 * RegisterPage - User registration form
 * Based on Google Stitch template '02.1 user registration page'
 */
const RegisterPage = () => {
  const navigate = useNavigate();
  const { register, login } = useAuth();

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    acceptTerms: false,
  });

  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [apiError, setApiError] = useState('');

  const passwordStrength = calculatePasswordStrength(formData.password);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value,
    });
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors({ ...errors, [name]: '' });
    }
    setApiError('');
  };

  const validate = () => {
    const newErrors = {};

    const firstNameValidation = validateRequired(formData.firstName, 'Vorname');
    if (!firstNameValidation.isValid) {
      newErrors.firstName = firstNameValidation.error;
    }

    const lastNameValidation = validateRequired(formData.lastName, 'Nachname');
    if (!lastNameValidation.isValid) {
      newErrors.lastName = lastNameValidation.error;
    }

    const emailValidation = validateEmail(formData.email);
    if (!emailValidation.isValid) {
      newErrors.email = emailValidation.error;
    }

    const passwordValidation = validatePassword(formData.password);
    if (!passwordValidation.isValid) {
      newErrors.password = passwordValidation.error;
    }

    const confirmPasswordValidation = validatePasswordMatch(
      formData.password,
      formData.confirmPassword
    );
    if (!confirmPasswordValidation.isValid) {
      newErrors.confirmPassword = confirmPasswordValidation.error;
    }

    if (!formData.acceptTerms) {
      newErrors.acceptTerms = 'Sie müssen die AGB akzeptieren';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError('');

    if (!validate()) {
      return;
    }

    setLoading(true);

    try {
      // Register user
      await register({
        email: formData.email,
        password: formData.password,
        firstName: formData.firstName,
        lastName: formData.lastName,
      });

      // Auto-login after registration
      await login(formData.email, formData.password);

      // Redirect to concerts page
      navigate('/concerts', { replace: true });
    } catch (error) {
      console.error('Registration failed:', error);
      const message =
        error.response?.data?.message ||
        'Registrierung fehlgeschlagen. Bitte versuchen Sie es erneut.';
      setApiError(message);
    } finally {
      setLoading(false);
    }
  };

  const getPasswordStrengthColor = () => {
    if (passwordStrength.strength === 'weak') return 'bg-orange-500';
    if (passwordStrength.strength === 'medium') return 'bg-orange-500';
    return 'bg-green-500';
  };

  const getPasswordStrengthText = () => {
    if (passwordStrength.strength === 'weak') return 'Schwaches Passwort';
    if (passwordStrength.strength === 'medium') return 'Mittelstarkes Passwort';
    return 'Starkes Passwort';
  };

  const getPasswordStrengthTextColor = () => {
    if (passwordStrength.strength === 'weak') return 'text-orange-600';
    if (passwordStrength.strength === 'medium') return 'text-orange-600';
    return 'text-green-600';
  };

  const renderPasswordStrengthBars = () => {
    const bars = 4;
    const filledBars = Math.ceil((passwordStrength.percentage / 100) * bars);

    return (
      <div className="flex gap-1 mt-1">
        {Array.from({ length: bars }).map((_, index) => (
          <div
            key={index}
            className={`h-1 flex-1 rounded-full ${
              index < filledBars ? getPasswordStrengthColor() : 'bg-[#dce1e5]'
            }`}
          ></div>
        ))}
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-[#F5F5F5] flex flex-col">
      {/* Header */}
      <header className="w-full bg-white border-b border-[#f0f3f4] sticky top-0 z-50">
        <div className="max-w-[1440px] mx-auto px-4 sm:px-10 py-3 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="flex items-center justify-center size-10 rounded-lg bg-primary/10 text-primary">
              <span className="material-symbols-outlined text-3xl">confirmation_number</span>
            </div>
            <h2 className="text-[#121517] text-xl font-bold leading-tight tracking-tight">
              ConcertFinder
            </h2>
          </div>
          <div className="hidden md:flex items-center gap-8">
            <nav className="flex items-center gap-9">
              <Link
                className="text-[#121517] hover:text-primary transition-colors text-sm font-medium"
                to="/concerts"
              >
                Events
              </Link>
            </nav>
            <div className="flex gap-3">
              <Link to="/login">
                <button className="flex items-center justify-center rounded-lg h-10 px-6 text-[#121517] font-bold text-sm hover:bg-gray-100 transition-colors">
                  Login
                </button>
              </Link>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-grow flex items-center justify-center py-10 px-4">
        <div className="w-full max-w-[500px]">
          {/* Card Container */}
          <div className="bg-white rounded-xl shadow-sm border border-[#e5e7eb] p-6 sm:p-8 flex flex-col gap-6">
            {/* Heading */}
            <div className="text-center flex flex-col gap-2">
              <h1 className="text-[#121517] text-3xl font-black leading-tight tracking-[-0.033em]">
                Konto erstellen
              </h1>
              <p className="text-[#657886] text-base font-normal">
                Schnell registriert, schnell Tickets kaufen
              </p>
            </div>

            {/* API Error */}
            {apiError && (
              <div className="p-4 rounded-lg bg-red-50 border border-red-200">
                <p className="text-sm text-red-600">{apiError}</p>
              </div>
            )}

            {/* Form */}
            <form onSubmit={handleSubmit} className="flex flex-col gap-5">
              {/* Names Row */}
              <div className="flex flex-col sm:flex-row gap-4">
                <label className="flex flex-col flex-1 gap-2">
                  <span className="text-[#121517] text-sm font-medium">Vorname</span>
                  <input
                    className={`form-input w-full rounded-lg border ${
                      errors.firstName ? 'border-red-500' : 'border-[#dce1e5]'
                    } bg-white text-[#121517] h-12 px-4 focus:border-primary focus:ring-1 focus:ring-primary placeholder:text-[#657886] transition-all text-sm font-normal`}
                    placeholder="Max"
                    type="text"
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleChange}
                  />
                  {errors.firstName && (
                    <span className="text-sm text-red-600">{errors.firstName}</span>
                  )}
                </label>
                <label className="flex flex-col flex-1 gap-2">
                  <span className="text-[#121517] text-sm font-medium">Nachname</span>
                  <input
                    className={`form-input w-full rounded-lg border ${
                      errors.lastName ? 'border-red-500' : 'border-[#dce1e5]'
                    } bg-white text-[#121517] h-12 px-4 focus:border-primary focus:ring-1 focus:ring-primary placeholder:text-[#657886] transition-all text-sm font-normal`}
                    placeholder="Mustermann"
                    type="text"
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleChange}
                  />
                  {errors.lastName && (
                    <span className="text-sm text-red-600">{errors.lastName}</span>
                  )}
                </label>
              </div>

              {/* Email */}
              <label className="flex flex-col gap-2">
                <span className="text-[#121517] text-sm font-medium">E-Mail-Adresse</span>
                <div className="relative flex items-center">
                  <input
                    className={`form-input w-full rounded-lg border ${
                      errors.email ? 'border-red-500' : 'border-[#dce1e5]'
                    } bg-white text-[#121517] h-12 pl-4 pr-12 focus:border-primary focus:ring-1 focus:ring-primary placeholder:text-[#657886] transition-all text-sm font-normal`}
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    placeholder="max.mustermann@example.com"
                  />
                </div>
                {errors.email && <span className="text-sm text-red-600">{errors.email}</span>}
              </label>

              {/* Password */}
              <div className="flex flex-col gap-2">
                <label className="flex flex-col gap-2">
                  <span className="text-[#121517] text-sm font-medium">Passwort</span>
                  <div className="relative flex items-center">
                    <input
                      className={`form-input w-full rounded-lg border ${
                        errors.password ? 'border-red-500' : 'border-[#dce1e5]'
                      } bg-white text-[#121517] h-12 pl-4 pr-12 focus:border-primary focus:ring-1 focus:ring-primary placeholder:text-[#657886] transition-all text-sm font-normal`}
                      type={showPassword ? 'text' : 'password'}
                      name="password"
                      value={formData.password}
                      onChange={handleChange}
                      placeholder="Mindestens 8 Zeichen"
                    />
                    <button
                      className="absolute right-4 text-[#657886] hover:text-primary transition-colors flex items-center cursor-pointer focus:outline-none"
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                    >
                      <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>
                        {showPassword ? 'visibility_off' : 'visibility'}
                      </span>
                    </button>
                  </div>
                  {errors.password && (
                    <span className="text-sm text-red-600">{errors.password}</span>
                  )}
                </label>

                {/* Password Strength Indicator */}
                {formData.password && (
                  <>
                    {renderPasswordStrengthBars()}
                    <p className={`text-xs ${getPasswordStrengthTextColor()} mt-0.5`}>
                      {getPasswordStrengthText()}
                    </p>
                  </>
                )}
              </div>

              {/* Confirm Password */}
              <label className="flex flex-col gap-2">
                <span className="text-[#121517] text-sm font-medium">Passwort bestätigen</span>
                <div className="relative flex items-center">
                  <input
                    className={`form-input w-full rounded-lg border ${
                      errors.confirmPassword ? 'border-red-500' : 'border-[#dce1e5]'
                    } bg-white text-[#121517] h-12 pl-4 pr-12 focus:border-primary focus:ring-1 focus:ring-primary placeholder:text-[#657886] transition-all text-sm font-normal`}
                    type={showConfirmPassword ? 'text' : 'password'}
                    name="confirmPassword"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    placeholder="Passwort wiederholen"
                  />
                  <button
                    className="absolute right-4 text-[#657886] hover:text-primary transition-colors flex items-center cursor-pointer focus:outline-none"
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  >
                    <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>
                      {showConfirmPassword ? 'visibility_off' : 'visibility'}
                    </span>
                  </button>
                </div>
                {errors.confirmPassword && (
                  <span className="text-sm text-red-600">{errors.confirmPassword}</span>
                )}
              </label>

              {/* Checkbox */}
              <label className="flex items-start gap-3 cursor-pointer mt-1">
                <div className="relative flex items-center">
                  <input
                    className="form-checkbox size-5 rounded border-gray-300 text-primary focus:ring-primary"
                    type="checkbox"
                    name="acceptTerms"
                    checked={formData.acceptTerms}
                    onChange={handleChange}
                  />
                </div>
                <span className="text-sm text-[#657886] leading-tight select-none">
                  Ich habe die{' '}
                  <a className="text-primary hover:underline font-medium" href="/terms">
                    Allgemeinen Geschäftsbedingungen
                  </a>{' '}
                  und die{' '}
                  <a className="text-primary hover:underline font-medium" href="/privacy">
                    Datenschutzrichtlinie
                  </a>{' '}
                  gelesen und akzeptiere diese.
                </span>
              </label>
              {errors.acceptTerms && (
                <span className="text-sm text-red-600 -mt-3">{errors.acceptTerms}</span>
              )}

              {/* Actions */}
              <div className="flex flex-col gap-4 mt-2">
                <button
                  className="w-full flex items-center justify-center h-12 rounded-lg bg-primary hover:bg-[#16649b] text-white text-base font-bold transition-all shadow-md hover:shadow-lg active:scale-[0.99] disabled:opacity-50 disabled:cursor-not-allowed"
                  type="submit"
                  disabled={loading}
                >
                  {loading ? 'Wird erstellt...' : 'Konto erstellen'}
                </button>
              </div>
            </form>

            {/* Footer Login Link */}
            <div className="text-center pt-2">
              <p className="text-[#657886] text-sm">
                Bereits registriert?{' '}
                <Link className="text-primary hover:text-[#16649b] font-bold hover:underline" to="/login">
                  Anmelden
                </Link>
              </p>
            </div>
          </div>

          {/* Trust Badge */}
          <div className="mt-8 flex justify-center gap-6 opacity-60 transition-all duration-300">
            <div className="flex items-center gap-1.5 text-xs text-[#657886] font-medium">
              <span className="material-symbols-outlined text-sm">lock</span>
              Sichere SSL-Verschlüsselung
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default RegisterPage;
