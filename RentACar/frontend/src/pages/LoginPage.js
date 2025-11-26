/**
 * LoginPage - Login und Registrierung mit Tabs
 * Basiert auf: stitch_rentacar/login/registrierung_1/code.html
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const LoginPage = () => {
  const [activeTab, setActiveTab] = useState('login');
  const [loginData, setLoginData] = useState({ email: '', password: '' });
  const [registerData, setRegisterData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    phoneNumber: '',
    driverLicenseNumber: '',
    address: { street: '', postalCode: '', city: '', country: 'Deutschland' },
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login, register } = useAuth();
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(loginData.email, loginData.password);
      navigate('/');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register(registerData);
      setActiveTab('login');
      setError('Registrierung erfolgreich! Bitte bestätigen Sie Ihre E-Mail.');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

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

          {/* Error Message */}
          {error && (
            <div
              className={`p-4 rounded-lg ${error.includes('erfolgreich') ? 'bg-green-50 text-green-800' : 'bg-red-50 text-red-800'}`}
            >
              {error}
            </div>
          )}

          {/* Login Form */}
          {activeTab === 'login' && (
            <form onSubmit={handleLogin} className="flex flex-col gap-6 px-4">
              <h1 className="text-4xl font-black">Willkommen zurück!</h1>
              <label className="flex flex-col w-full">
                <p className="text-base font-medium pb-2">E-Mail</p>
                <div className="flex items-stretch rounded-lg">
                  <input
                    className="form-input flex-1 rounded-lg border-gray-300 bg-white h-14 px-4"
                    type="email"
                    placeholder="ihre-email@beispiel.de"
                    value={loginData.email}
                    onChange={(e) => setLoginData({ ...loginData, email: e.target.value })}
                    required
                  />
                  <div className="flex items-center justify-center px-4 border border-l-0 border-gray-300 rounded-r-lg bg-white">
                    <span className="material-symbols-outlined text-gray-400">mail</span>
                  </div>
                </div>
              </label>
              <label className="flex flex-col w-full">
                <p className="text-base font-medium pb-2">Passwort</p>
                <div className="flex items-stretch rounded-lg">
                  <input
                    className="form-input flex-1 rounded-lg border-gray-300 bg-white h-14 px-4"
                    type="password"
                    placeholder="Ihr Passwort"
                    value={loginData.password}
                    onChange={(e) => setLoginData({ ...loginData, password: e.target.value })}
                    required
                  />
                  <div className="flex items-center justify-center px-4 border border-l-0 border-gray-300 rounded-r-lg bg-white">
                    <span className="material-symbols-outlined text-gray-400">lock</span>
                  </div>
                </div>
              </label>
              <button
                type="submit"
                disabled={loading}
                className="h-12 rounded-lg bg-primary text-white font-bold hover:opacity-90 disabled:opacity-50"
              >
                {loading ? 'Wird geladen...' : 'Anmelden'}
              </button>
            </form>
          )}

          {/* Register Form */}
          {activeTab === 'register' && (
            <form onSubmit={handleRegister} className="flex flex-col gap-4 px-4">
              <h1 className="text-4xl font-black">Konto erstellen</h1>
              <div className="grid grid-cols-2 gap-4">
                <label className="flex flex-col">
                  <p className="text-sm font-medium pb-2">Vorname</p>
                  <input
                    className="form-input rounded-lg border-gray-300 h-12 px-3"
                    type="text"
                    value={registerData.firstName}
                    onChange={(e) =>
                      setRegisterData({ ...registerData, firstName: e.target.value })
                    }
                    required
                  />
                </label>
                <label className="flex flex-col">
                  <p className="text-sm font-medium pb-2">Nachname</p>
                  <input
                    className="form-input rounded-lg border-gray-300 h-12 px-3"
                    type="text"
                    value={registerData.lastName}
                    onChange={(e) => setRegisterData({ ...registerData, lastName: e.target.value })}
                    required
                  />
                </label>
              </div>
              <label className="flex flex-col">
                <p className="text-sm font-medium pb-2">E-Mail</p>
                <input
                  className="form-input rounded-lg border-gray-300 h-12 px-3"
                  type="email"
                  value={registerData.email}
                  onChange={(e) => setRegisterData({ ...registerData, email: e.target.value })}
                  required
                />
              </label>
              <label className="flex flex-col">
                <p className="text-sm font-medium pb-2">Passwort</p>
                <input
                  className="form-input rounded-lg border-gray-300 h-12 px-3"
                  type="password"
                  value={registerData.password}
                  onChange={(e) => setRegisterData({ ...registerData, password: e.target.value })}
                  required
                />
              </label>
              <label className="flex flex-col">
                <p className="text-sm font-medium pb-2">Telefon</p>
                <input
                  className="form-input rounded-lg border-gray-300 h-12 px-3"
                  type="tel"
                  value={registerData.phoneNumber}
                  onChange={(e) =>
                    setRegisterData({ ...registerData, phoneNumber: e.target.value })
                  }
                  required
                />
              </label>
              <label className="flex flex-col">
                <p className="text-sm font-medium pb-2">Führerscheinnummer</p>
                <input
                  className="form-input rounded-lg border-gray-300 h-12 px-3"
                  type="text"
                  value={registerData.driverLicenseNumber}
                  onChange={(e) =>
                    setRegisterData({ ...registerData, driverLicenseNumber: e.target.value })
                  }
                  required
                />
              </label>
              <div className="grid grid-cols-2 gap-4">
                <label className="flex flex-col col-span-2">
                  <p className="text-sm font-medium pb-2">Straße</p>
                  <input
                    className="form-input rounded-lg border-gray-300 h-12 px-3"
                    type="text"
                    value={registerData.address.street}
                    onChange={(e) =>
                      setRegisterData({
                        ...registerData,
                        address: { ...registerData.address, street: e.target.value },
                      })
                    }
                    required
                  />
                </label>
                <label className="flex flex-col">
                  <p className="text-sm font-medium pb-2">PLZ</p>
                  <input
                    className="form-input rounded-lg border-gray-300 h-12 px-3"
                    type="text"
                    value={registerData.address.postalCode}
                    onChange={(e) =>
                      setRegisterData({
                        ...registerData,
                        address: { ...registerData.address, postalCode: e.target.value },
                      })
                    }
                    required
                  />
                </label>
                <label className="flex flex-col">
                  <p className="text-sm font-medium pb-2">Stadt</p>
                  <input
                    className="form-input rounded-lg border-gray-300 h-12 px-3"
                    type="text"
                    value={registerData.address.city}
                    onChange={(e) =>
                      setRegisterData({
                        ...registerData,
                        address: { ...registerData.address, city: e.target.value },
                      })
                    }
                    required
                  />
                </label>
              </div>
              <button
                type="submit"
                disabled={loading}
                className="h-12 rounded-lg bg-primary text-white font-bold hover:opacity-90 disabled:opacity-50 mt-2"
              >
                {loading ? 'Wird geladen...' : 'Registrieren'}
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
