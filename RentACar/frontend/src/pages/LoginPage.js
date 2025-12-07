/**
 * LoginPage - Login und Registrierung mit Tabs
 * Basiert auf: stitch_rentacar/login/registrierung_1/code.html
 * Verwendet React Hook Form mit Yup Validation
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm, FormProvider } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import toast from 'react-hot-toast';
import { useAuth } from '../context/AuthContext';
import { loginSchema, registrationSchema } from '../utils/validationRules';
import FormInput from '../components/forms/FormInput';
import PasswordStrengthIndicator from '../components/forms/PasswordStrengthIndicator';
import AddressAutocomplete from '../components/AddressAutocomplete';

const LoginPage = () => {
  const [activeTab, setActiveTab] = useState('login');
  const [loading, setLoading] = useState(false);
  const { login, register } = useAuth();
  const navigate = useNavigate();

  // React Hook Form für Login
  const loginMethods = useForm({
    resolver: yupResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });

  // React Hook Form für Registrierung
  const registerMethods = useForm({
    resolver: yupResolver(registrationSchema),
    defaultValues: {
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      phoneNumber: '',
      driverLicenseNumber: '',
      street: '',
      postalCode: '',
      city: '',
    },
  });

  const handleLogin = async (data) => {
    setLoading(true);
    try {
      await login(data.email, data.password);
      toast.success('Erfolgreich angemeldet!');
      navigate('/');
    } catch (err) {
      toast.error(err.message || 'Anmeldung fehlgeschlagen');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickLogin = async (email) => {
    setLoading(true);
    try {
      // Use the same test password as seeded in backend DataInitializer
      await login(email, 'Test1234!');
      toast.success('Erfolgreich angemeldet!');
      navigate('/');
    } catch (err) {
      toast.error(err.message || 'Anmeldung fehlgeschlagen');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (data) => {
    setLoading(true);
    try {
      await register(data);
      toast.success(
        'Registrierung erfolgreich! Bitte bestätigen Sie Ihre E-Mail-Adresse und melden Sie sich dann an.'
      );
      setActiveTab('login');
      registerMethods.reset();
    } catch (err) {
      toast.error(err.message || 'Registrierung fehlgeschlagen');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Handler für Adress-Änderungen (für AddressAutocomplete)
   */
  const handleAddressChange = (addressFields) => {
    Object.entries(addressFields).forEach(([key, value]) => {
      registerMethods.setValue(key, value, { shouldValidate: true });
    });
  };

  // Watch password for strength indicator
  const watchedPassword = registerMethods.watch('password');

  return (
    <div className="min-h-screen grid grid-cols-1 md:grid-cols-2">
      {/* Left Section - Image */}
      <div
        className="relative hidden md:flex flex-col gap-6 p-10 bg-center bg-no-repeat bg-cover text-white justify-between"
        style={{
          backgroundImage: `url('https://images.unsplash.com/photo-1485291571150-772bcfc10da5?w=800')`,
        }}
      >
        <div className="absolute inset-0 bg-black/40"></div>
        <div className="relative z-10">
          <h2 className="text-2xl font-bold">RENTACAR</h2>
        </div>
        <div className="relative z-10 flex flex-col gap-2 text-left">
          <h1 className="text-5xl font-black leading-tight">
            Ihr nächstes Abenteuer beginnt hier.
          </h1>
          <h2 className="text-lg font-normal">Einfach. Sicher. Mieten.</h2>
        </div>
      </div>

      {/* Right Section - Form */}
      <div className="flex flex-col items-center justify-center p-6 sm:p-12 bg-background-light">
        <div className="flex flex-col max-w-[480px] w-full gap-8">
          {/* Tabs */}
          <div className="flex px-4 py-3 w-full">
            <div className="flex h-12 flex-1 items-center justify-center rounded-lg bg-gray-200 p-1">
              <button
                onClick={() => setActiveTab('login')}
                className={`flex h-full grow items-center justify-center rounded-lg px-2 text-sm font-medium ${activeTab === 'login' ? 'bg-white shadow-sm text-primary' : 'text-gray-500'}`}
              >
                Anmelden
              </button>
              <button
                onClick={() => setActiveTab('register')}
                className={`flex h-full grow items-center justify-center rounded-lg px-2 text-sm font-medium ${activeTab === 'register' ? 'bg-white shadow-sm text-primary' : 'text-gray-500'}`}
              >
                Registrieren
              </button>
            </div>
          </div>

          {/* Login Form */}
          {activeTab === 'login' && (
            <FormProvider {...loginMethods}>
              <form onSubmit={loginMethods.handleSubmit(handleLogin)} className="flex flex-col gap-6 px-4">
                <h1 className="text-4xl font-black">Willkommen zurück!</h1>
                
                <FormInput
                  name="email"
                  label="E-Mail"
                  type="email"
                  placeholder="ihre-email@beispiel.de"
                  icon="mail"
                  required
                />

                <FormInput
                  name="password"
                  label="Passwort"
                  type="password"
                  placeholder="Ihr Passwort"
                  icon="lock"
                  required
                />

                <button
                  type="submit"
                  disabled={loading}
                  className="h-12 rounded-lg bg-primary text-white font-bold hover:opacity-90 disabled:opacity-50"
                >
                  {loading ? 'Wird geladen...' : 'Anmelden'}
                </button>

                {/* Schnelllogin Test-Accounts */}
                <div className="mt-4 pt-4 border-t border-gray-200">
                  <p className="text-sm text-gray-500 mb-3 text-center">
                    Schnelllogin (Test-Accounts)
                  </p>
                  <div className="flex flex-col gap-2">
                    <button
                      type="button"
                      onClick={() => handleQuickLogin('test.customer@example.com')}
                      disabled={loading}
                      className="h-10 rounded-lg bg-green-600 text-white text-sm font-medium hover:bg-green-700 disabled:opacity-50"
                    >
                      Kunde
                    </button>
                    <button
                      type="button"
                      onClick={() => handleQuickLogin('test.employee@example.com')}
                      disabled={loading}
                      className="h-10 rounded-lg bg-blue-600 text-white text-sm font-medium hover:bg-blue-700 disabled:opacity-50"
                    >
                      Mitarbeiter
                    </button>
                    <button
                      type="button"
                      onClick={() => handleQuickLogin('test.admin@example.com')}
                      disabled={loading}
                      className="h-10 rounded-lg bg-purple-600 text-white text-sm font-medium hover:bg-purple-700 disabled:opacity-50"
                    >
                      Admin
                    </button>
                  </div>
                </div>
              </form>
            </FormProvider>
          )}

          {/* Register Form */}
          {activeTab === 'register' && (
            <FormProvider {...registerMethods}>
              <form onSubmit={registerMethods.handleSubmit(handleRegister)} className="flex flex-col gap-4 px-4">
                <h1 className="text-4xl font-black">Konto erstellen</h1>
                
                <div className="grid grid-cols-2 gap-4">
                  <FormInput
                    name="firstName"
                    label="Vorname"
                    type="text"
                    required
                    className="mb-0"
                  />
                  <FormInput
                    name="lastName"
                    label="Nachname"
                    type="text"
                    required
                    className="mb-0"
                  />
                </div>

                <FormInput
                  name="email"
                  label="E-Mail"
                  type="email"
                  required
                  className="mb-0"
                />

                <div>
                  <FormInput
                    name="password"
                    label="Passwort"
                    type="password"
                    required
                    className="mb-0"
                  />
                  <PasswordStrengthIndicator password={watchedPassword} />
                </div>

                <FormInput
                  name="phoneNumber"
                  label="Telefon"
                  type="tel"
                  required
                  className="mb-0"
                />

                <FormInput
                  name="driverLicenseNumber"
                  label="Führerscheinnummer"
                  type="text"
                  required
                  className="mb-0"
                />

                <AddressAutocomplete
                  street={registerMethods.watch('street')}
                  postalCode={registerMethods.watch('postalCode')}
                  city={registerMethods.watch('city')}
                  onAddressChange={handleAddressChange}
                  validationErrors={registerMethods.formState.errors}
                />

                <button
                  type="submit"
                  disabled={loading}
                  className="h-12 rounded-lg bg-primary text-white font-bold hover:opacity-90 disabled:opacity-50 mt-2"
                >
                  {loading ? 'Wird geladen...' : 'Registrieren'}
                </button>
              </form>
            </FormProvider>
          )}
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
