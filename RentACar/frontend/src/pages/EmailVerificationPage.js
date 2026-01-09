/**
 * EmailVerificationPage - E-Mail-Verifizierung
 *
 * Diese Seite wird über einen Link in der Verifizierungs-E-Mail aufgerufen.
 * Format: /verify-email?token=abc123xyz
 *
 * Flow:
 * 1. Token aus URL extrahieren
 * 2. API-Call zur Verifizierung
 * 3. Success → Weiterleitung zu /login nach 3 Sekunden
 * 4. Error → Fehlermeldung anzeigen
 */

import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import authService from '../services/authService';

const EmailVerificationPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState('loading'); // 'loading' | 'success' | 'error'
  const [message, setMessage] = useState('');
  const [countdown, setCountdown] = useState(3);

  useEffect(() => {
    const verifyEmailToken = async () => {
      // Token aus URL extrahieren
      const token = searchParams.get('token');

      if (!token) {
        setStatus('error');
        setMessage(
          'Kein Verifizierungs-Token gefunden. Bitte prüfen Sie den Link aus Ihrer E-Mail.'
        );
        return;
      }

      try {
        // API-Call zur E-Mail-Verifizierung
        const response = await authService.verifyEmail(token);
        setStatus('success');
        setMessage(response || 'E-Mail-Adresse erfolgreich verifiziert!');

        // Countdown für automatische Weiterleitung starten
        let counter = 3;
        const interval = setInterval(() => {
          counter -= 1;
          setCountdown(counter);
          
          if (counter <= 0) {
            clearInterval(interval);
            navigate('/login');
          }
        }, 1000);

        // Cleanup
        return () => clearInterval(interval);
      } catch (error) {
        setStatus('error');
        setMessage(
          error.message || 'Verifizierung fehlgeschlagen. Der Link ist möglicherweise abgelaufen.'
        );
      }
    };

    verifyEmailToken();
  }, [searchParams, navigate]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-8">
        {/* Loading State */}
        {status === 'loading' && (
          <div className="text-center">
            <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-primary mx-auto mb-4"></div>
            <h1 className="text-2xl font-bold text-gray-800 mb-2">E-Mail wird verifiziert...</h1>
            <p className="text-gray-600">Bitte warten Sie einen Moment.</p>
          </div>
        )}

        {/* Success State */}
        {status === 'success' && (
          <div className="text-center">
            <div className="mb-6">
              <svg
                className="w-16 h-16 text-green-500 mx-auto"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
            <h1 className="text-2xl font-bold text-gray-800 mb-3">Erfolgreich verifiziert!</h1>
            <p className="text-gray-600 mb-6">{message}</p>
            <p className="text-sm text-gray-500 mb-4">
              Sie werden in <span className="font-bold text-primary">{countdown}</span> Sekunden zum
              Login weitergeleitet...
            </p>
            <Link
              to="/login"
              className="inline-block px-6 py-3 bg-primary text-white font-medium rounded-lg hover:opacity-90 transition"
            >
              Jetzt anmelden
            </Link>
          </div>
        )}

        {/* Error State */}
        {status === 'error' && (
          <div className="text-center">
            <div className="mb-6">
              <svg
                className="w-16 h-16 text-red-500 mx-auto"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
            <h1 className="text-2xl font-bold text-gray-800 mb-3">Verifizierung fehlgeschlagen</h1>
            <p className="text-gray-600 mb-6">{message}</p>
            <div className="space-y-3">
              <Link
                to="/login"
                className="block w-full px-6 py-3 bg-primary text-white font-medium rounded-lg hover:opacity-90 transition"
              >
                Zum Login
              </Link>
              <Link
                to="/"
                className="block w-full px-6 py-3 bg-gray-200 text-gray-700 font-medium rounded-lg hover:bg-gray-300 transition"
              >
                Zur Startseite
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default EmailVerificationPage;
