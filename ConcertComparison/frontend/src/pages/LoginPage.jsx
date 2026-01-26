import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { validateEmail, validatePassword } from '../utils/validation';

/**
 * LoginPage - User login form
 * Based on Google Stitch template '02 login page'
 */
const LoginPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const [formData, setFormData] = useState({
    email: '',
    password: '',
    rememberMe: false,
  });

  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [apiError, setApiError] = useState('');

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

    const emailValidation = validateEmail(formData.email);
    if (!emailValidation.isValid) {
      newErrors.email = emailValidation.error;
    }

    const passwordValidation = validatePassword(formData.password);
    if (!passwordValidation.isValid) {
      newErrors.password = passwordValidation.error;
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
      await login(formData.email, formData.password);
      
      // Auto-redirect to previous page or home
      const from = location.state?.from?.pathname || '/concerts';
      navigate(from, { replace: true });
    } catch (error) {
      console.error('Login failed:', error);
      const message = error.response?.data?.message || 'Anmeldung fehlgeschlagen. Bitte überprüfen Sie Ihre Zugangsdaten.';
      setApiError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex h-screen w-full">
      {/* Left Section: Login Form */}
      <div className="flex w-full lg:w-1/2 flex-col bg-[#F5F5F5] overflow-y-auto h-full px-6 py-8 sm:px-12 lg:px-20 xl:px-32 relative">
        {/* Logo Header */}
        <div className="flex items-center gap-3 mb-12 lg:absolute lg:top-10 lg:left-20 lg:mb-0">
          <div className="flex items-center justify-center size-10 rounded-lg bg-primary/10 text-primary">
            <span className="material-symbols-outlined text-3xl">confirmation_number</span>
          </div>
          <h2 className="text-[#121517] text-xl font-bold tracking-tight">ConcertFinder</h2>
        </div>

        <div className="flex flex-1 flex-col justify-center max-w-[480px] mx-auto w-full">
          {/* Heading */}
          <div className="flex flex-col gap-2 mb-8">
            <h1 className="text-[#121517] text-4xl font-black leading-tight tracking-[-0.033em]">
              Willkommen zurück
            </h1>
            <p className="text-[#657886] text-base font-normal leading-normal">
              Bitte geben Sie Ihre Zugangsdaten ein.
            </p>
          </div>

          {/* API Error */}
          {apiError && (
            <div className="mb-5 p-4 rounded-lg bg-red-50 border border-red-200">
              <p className="text-sm text-red-600">{apiError}</p>
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="flex flex-col gap-5">
            {/* Email Field */}
            <label className="flex flex-col gap-1.5">
              <span className="text-[#121517] text-sm font-semibold leading-normal">
                E-Mail-Adresse
              </span>
              <div className="relative">
                <input
                  className={`form-input flex w-full rounded-lg border ${
                    errors.email ? 'border-red-500' : 'border-[#dce1e5]'
                  } bg-white h-12 px-4 text-base text-[#121517] placeholder:text-[#657886] focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all`}
                  placeholder="name@example.com"
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                />
                <span className="material-symbols-outlined absolute right-4 top-3 text-[#657886]">
                  mail
                </span>
              </div>
              {errors.email && <span className="text-sm text-red-600">{errors.email}</span>}
            </label>

            {/* Password Field */}
            <label className="flex flex-col gap-1.5">
              <span className="text-[#121517] text-sm font-semibold leading-normal">
                Passwort
              </span>
              <div className="relative">
                <input
                  className={`form-input flex w-full rounded-lg border ${
                    errors.password ? 'border-red-500' : 'border-[#dce1e5]'
                  } bg-white h-12 px-4 text-base text-[#121517] placeholder:text-[#657886] focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all`}
                  placeholder="Passwort eingeben"
                  type={showPassword ? 'text' : 'password'}
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                />
                <span
                  className="material-symbols-outlined absolute right-4 top-3 text-[#657886] cursor-pointer hover:text-primary"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? 'visibility_off' : 'visibility'}
                </span>
              </div>
              {errors.password && <span className="text-sm text-red-600">{errors.password}</span>}
            </label>

            {/* Checkbox & Forgot Password */}
            <div className="flex items-center justify-between">
              <label className="flex items-center gap-3 cursor-pointer group">
                <input
                  className="h-5 w-5 rounded border-[#dce1e5] bg-white text-primary focus:ring-0 focus:ring-offset-0 transition-all"
                  type="checkbox"
                  name="rememberMe"
                  checked={formData.rememberMe}
                  onChange={handleChange}
                />
                <span className="text-[#121517] text-sm font-medium leading-normal group-hover:text-primary transition-colors">
                  Angemeldet bleiben
                </span>
              </label>
            </div>

            {/* Submit Button */}
            <button
              className="mt-2 flex w-full items-center justify-center rounded-lg bg-primary h-12 px-5 text-base font-bold text-white shadow-sm hover:bg-primary/90 focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
              type="submit"
              disabled={loading}
            >
              {loading ? 'Wird angemeldet...' : 'Anmelden'}
            </button>

            {/* DEV Quick Login Buttons - TODO: Remove before production */}
            {process.env.NODE_ENV === 'development' && (
              <div className="mt-4 p-4 rounded-lg bg-yellow-50 border border-yellow-200">
                <p className="text-xs text-yellow-700 font-semibold mb-3 text-center">⚡ DEV Quick Login</p>
                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={() => {
                      setFormData({ email: 'admin@example.com', password: 'adminpassword123', rememberMe: false });
                      setTimeout(() => document.querySelector('form').requestSubmit(), 100);
                    }}
                    className="flex-1 py-2 px-3 text-xs font-bold rounded-lg bg-purple-600 text-white hover:bg-purple-700 transition-colors"
                    disabled={loading}
                  >
                    🔐 Admin Login
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setFormData({ email: 'user@example.com', password: 'userpassword123', rememberMe: false });
                      setTimeout(() => document.querySelector('form').requestSubmit(), 100);
                    }}
                    className="flex-1 py-2 px-3 text-xs font-bold rounded-lg bg-blue-600 text-white hover:bg-blue-700 transition-colors"
                    disabled={loading}
                  >
                    👤 User Login
                  </button>
                </div>
              </div>
            )}

            {/* Footer Link */}
            <p className="text-center text-sm text-[#657886] mt-4">
              Noch kein Konto?{' '}
              <Link className="font-bold text-primary hover:underline" to="/register">
                Jetzt registrieren
              </Link>
            </p>
          </form>

          {/* Help Links Footer */}
          <div className="mt-12 flex justify-center gap-6 text-sm text-[#657886]">
            <a className="hover:text-[#121517] transition-colors" href="/privacy">
              Datenschutz
            </a>
            <a className="hover:text-[#121517] transition-colors" href="/terms">
              AGB
            </a>
          </div>
        </div>
      </div>

      {/* Right Section: Visual & Branding */}
      <div className="hidden lg:flex lg:w-1/2 relative bg-gradient-to-br from-primary to-[#2CA02C] flex-col items-center justify-center p-12 overflow-hidden text-center">
        {/* Background Pattern */}
        <div className="absolute inset-0 opacity-10 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] mix-blend-overlay"></div>

        {/* Decorative circles */}
        <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-white/10 rounded-full blur-3xl"></div>
        <div className="absolute bottom-1/4 right-1/4 w-[500px] h-[500px] bg-white/5 rounded-full blur-3xl"></div>

        <div className="relative z-10 flex flex-col items-center max-w-lg">
          {/* Illustration Container */}
          <div className="mb-10 w-full aspect-[4/3] rounded-2xl bg-white/10 backdrop-blur-sm border border-white/20 shadow-2xl overflow-hidden relative group">
            <img
              alt="Excited crowd at a live concert with vibrant stage lights"
              className="w-full h-full object-cover opacity-90 group-hover:scale-105 transition-transform duration-700 ease-out"
              src="https://lh3.googleusercontent.com/aida-public/AB6AXuCsnntRekYndZ6FwirU11lIb3zBhqRCdL-blMFWvd3uAJjUzqQ5OsLShh67mGeyYi615guUrMxhu7itJFLhlAgls4uXoLqUUlOx5QpJxnYJI_F6hpMIom2RDOvpQiCjJ43Oil_75mPTHPbzLCxdFRT59x6nQNyMwSlqgJQF1_E3o9Y1DxzUgBjbc7rrE_5_BhnIqlI2K0nsA5IVJFu_BunGh2jEC6cMXQQKIsA25XBTOumNv-7VI8WwbX-MA0j9JxZOosfJ3ZKGyoGp"
            />
            <div className="absolute bottom-4 left-4 right-4 p-4 rounded-xl bg-black/40 backdrop-blur-md border border-white/10">
              <div className="flex items-center gap-3 text-white/90">
                <span className="material-symbols-outlined text-yellow-400">star</span>
                <span className="text-sm font-medium">4.9/5 Bewertung von Fans</span>
              </div>
            </div>
          </div>

          <h2 className="text-4xl font-black text-white leading-tight mb-6 drop-shadow-sm">
            "Über 100.000 zufriedene Konzertbesucher"
          </h2>
          <p className="text-lg text-white/90 font-medium max-w-md mx-auto leading-relaxed">
            Sichern Sie sich Ihren Platz für die größten Events des Jahres mit Echtzeit-Verfügbarkeit und sofortiger Buchung.
          </p>

          {/* Social Proof */}
          <div className="flex items-center justify-center mt-10 gap-4">
            <div className="flex -space-x-3">
              <img
                alt="User avatar"
                className="w-10 h-10 border-2 border-white rounded-full object-cover"
                src="https://lh3.googleusercontent.com/aida-public/AB6AXuCYheQNLyIwDASTM8DgcDWhjzB7qVORZWakrruoKwVhpK1Bxa4rPdHLgH1nfR8rucih5Pc8qIevO-NKROCxCXArCMmlUip4nZ1MtB7FSSDyiRd4v5HRXX0mByOMNaMlGQzugnvGqgmiKHiPDUQF5D_Qrqhv_gUlA5_tVTngPpmikuJX-p6zRIM8icH570zHtj9GOPuTsR4tZtwI89lJCRSB65v_-zmveuH5tYPmzDj9VCR4G9EyCgsZQakufwCchRV_O2UgwlRZfSif"
              />
              <img
                alt="User avatar"
                className="w-10 h-10 border-2 border-white rounded-full object-cover"
                src="https://lh3.googleusercontent.com/aida-public/AB6AXuAv_29DL_uCF74neeGTGSG2cpBBZYlbNKnrKRrZ4EFdJPOeaAkbSHF15oKuUqm_wOPlaep24JVdO6RrYDR2na8sRurf2w-bGTFQpixfS38wakwXgQVZr14lfJJAKLWha9Yw9CSO0I8Tt8AEA0G5Zs6qOHyViZmJ6NzNKu--tzEbYGocBrZqx3612XalZEZnBrKkhS8eDHd4tSB4T1nR6iS9qZiy4U8ns-aPdh_cmg15sVQdmuoOZ5R123fGOO8_LzEq3NN73Kku6SXY"
              />
              <img
                alt="User avatar"
                className="w-10 h-10 border-2 border-white rounded-full object-cover"
                src="https://lh3.googleusercontent.com/aida-public/AB6AXuCb590AaXXoQt6-AG8232QHSV21cOTG17Xg9dMeseeMa07E1M5fCmBvbWLWlyCwREKlyfFIMaz7354c-FtES1y1uoYT8N-TmNlBNF9uVznXzX7Q-3I7otyqzmf5rgwu46BITCK8TJr6CwQTx6FIiAl0BiG8PlXaPSui1-HIrS6h9L3pZfFBoJ-VcBnSQSAMYS33cBlXGaw4xomD9e9uO-HWxIU8VbVhDVB5Eb2pjqxnZrsvg4As3-yBBCXiVvcO6x_uj7SJhMRpEutp"
              />
              <div className="flex items-center justify-center w-10 h-10 text-xs font-medium text-primary bg-white border-2 border-white rounded-full hover:bg-gray-50">
                +99k
              </div>
            </div>
            <div className="text-white text-sm font-semibold">ConcertFinder Community</div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
