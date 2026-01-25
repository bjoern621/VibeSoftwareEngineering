import React from 'react';
import { useNavigate } from 'react-router-dom';

function PrivacyPage() {
  const navigate = useNavigate();

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
          <button
            onClick={() => navigate(-1)}
            className="flex items-center gap-2 text-[#121517] hover:text-primary transition-colors"
          >
            <span className="material-symbols-outlined">arrow_back</span>
            <span className="hidden sm:inline">Zurück</span>
          </button>
        </div>
      </header>

      {/* Content */}
      <main className="flex-grow py-10 px-4">
        <div className="max-w-4xl mx-auto bg-white rounded-xl shadow-sm border border-[#e5e7eb] p-6 sm:p-8">
          <h1 className="text-[#121517] text-3xl font-black leading-tight tracking-[-0.033em] mb-6">
            Datenschutzerklärung
          </h1>

          <div className="space-y-6 text-[#657886]">
            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">1. Datenschutz auf einen Blick</h2>
              <h3 className="text-[#121517] text-lg font-semibold mb-2">Allgemeine Hinweise</h3>
              <p className="mb-2">
                Die folgenden Hinweise geben einen einfachen Überblick darüber, was mit Ihren personenbezogenen
                Daten passiert, wenn Sie unsere Website besuchen und unsere Dienste nutzen.
              </p>
              <p>
                Personenbezogene Daten sind alle Daten, mit denen Sie persönlich identifiziert werden können.
              </p>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">2. Verantwortliche Stelle</h2>
              <p className="mb-2">
                Verantwortlich für die Datenverarbeitung auf dieser Website ist:
              </p>
              <p className="mb-1">
                <strong>ConcertFinder GmbH</strong>
              </p>
              <p className="mb-1">Musterstraße 123</p>
              <p className="mb-1">12345 Musterstadt</p>
              <p className="mb-1">Deutschland</p>
              <p className="mb-1">
                E-Mail:{' '}
                <a href="mailto:datenschutz@concertfinder.de" className="text-primary hover:underline">
                  datenschutz@concertfinder.de
                </a>
              </p>
              <p className="mb-4">
                Telefon:{' '}
                <a href="tel:+4912345678900" className="text-primary hover:underline">
                  +49 (0) 123 456 789-00
                </a>
              </p>
              <p>
                Verantwortliche Stelle ist die natürliche oder juristische Person, die allein oder gemeinsam mit
                anderen über die Zwecke und Mittel der Verarbeitung von personenbezogenen Daten entscheidet.
              </p>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">3. Datenerfassung auf unserer Website</h2>
              <h3 className="text-[#121517] text-lg font-semibold mb-2">Welche Daten erfassen wir?</h3>
              <p className="mb-2">Wir erheben folgende Kategorien personenbezogener Daten:</p>
              <ul className="list-disc list-inside space-y-1 mb-4">
                <li>Kontaktdaten (Name, E-Mail-Adresse, Telefonnummer)</li>
                <li>Zahlungsinformationen (Kreditkartendaten, PayPal-Konto)</li>
                <li>Nutzungsdaten (IP-Adresse, Browsertyp, Zugriffszeiten)</li>
                <li>Bestellhistorie und Ticketinformationen</li>
              </ul>

              <h3 className="text-[#121517] text-lg font-semibold mb-2">Wie erfassen wir Ihre Daten?</h3>
              <p className="mb-2">
                Ihre Daten werden zum einen dadurch erhoben, dass Sie uns diese mitteilen (z.B. bei der
                Registrierung oder Bestellung).
              </p>
              <p>
                Andere Daten werden automatisch beim Besuch der Website durch unsere IT-Systeme erfasst. Das sind
                vor allem technische Daten (z.B. Internetbrowser, Betriebssystem oder Uhrzeit des Seitenaufrufs).
              </p>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">4. Zweck der Datenverarbeitung</h2>
              <p className="mb-2">Wir nutzen Ihre Daten für folgende Zwecke:</p>
              <ul className="list-disc list-inside space-y-1">
                <li>Bereitstellung und Verbesserung unserer Dienste</li>
                <li>Verarbeitung von Ticketbestellungen und Zahlungen</li>
                <li>Kommunikation mit Ihnen (Bestellbestätigungen, Support)</li>
                <li>Erfüllung rechtlicher Verpflichtungen</li>
                <li>Analyse der Website-Nutzung zur Optimierung</li>
                <li>Zusendung von Newslettern (nur mit Ihrer Einwilligung)</li>
              </ul>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">5. Rechtsgrundlage</h2>
              <p className="mb-2">Die Verarbeitung Ihrer Daten erfolgt auf Grundlage von:</p>
              <ul className="list-disc list-inside space-y-1">
                <li>Art. 6 Abs. 1 lit. b DSGVO (Vertragserfüllung)</li>
                <li>Art. 6 Abs. 1 lit. a DSGVO (Einwilligung)</li>
                <li>Art. 6 Abs. 1 lit. f DSGVO (berechtigte Interessen)</li>
                <li>Art. 6 Abs. 1 lit. c DSGVO (rechtliche Verpflichtung)</li>
              </ul>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">6. Speicherdauer</h2>
              <p className="mb-2">
                Wir speichern Ihre personenbezogenen Daten nur so lange, wie dies für die Erfüllung der
                beschriebenen Zwecke erforderlich ist.
              </p>
              <p className="mb-2">
                Für die Vertragsabwicklung werden Daten für die Dauer der Vertragsbeziehung und darüber hinaus für
                die gesetzlichen Aufbewahrungsfristen (i.d.R. 10 Jahre) gespeichert.
              </p>
              <p>
                Daten, die wir aufgrund Ihrer Einwilligung verarbeiten, werden gelöscht, sobald Sie Ihre
                Einwilligung widerrufen.
              </p>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">7. Cookies</h2>
              <p className="mb-2">
                Unsere Website verwendet Cookies. Das sind kleine Textdateien, die auf Ihrem Endgerät gespeichert
                werden.
              </p>
              <p className="mb-2">Wir verwenden folgende Arten von Cookies:</p>
              <ul className="list-disc list-inside space-y-1 mb-4">
                <li>
                  <strong>Technisch notwendige Cookies:</strong> Ermöglichen grundlegende Funktionen (z.B.
                  Warenkorb, Login)
                </li>
                <li>
                  <strong>Analyse-Cookies:</strong> Helfen uns, die Nutzung der Website zu verstehen
                </li>
                <li>
                  <strong>Marketing-Cookies:</strong> Ermöglichen personalisierte Werbung (nur mit Einwilligung)
                </li>
              </ul>
              <p>
                Sie können Ihren Browser so einstellen, dass Sie über das Setzen von Cookies informiert werden und
                einzeln über deren Annahme entscheiden.
              </p>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">8. Weitergabe von Daten</h2>
              <p className="mb-2">
                Wir geben Ihre Daten nur an Dritte weiter, wenn dies zur Vertragserfüllung notwendig ist oder Sie
                ausdrücklich eingewilligt haben.
              </p>
              <p className="mb-2">Empfänger können sein:</p>
              <ul className="list-disc list-inside space-y-1">
                <li>Zahlungsdienstleister (z.B. Stripe, PayPal)</li>
                <li>Versanddienstleister (für physische Tickets)</li>
                <li>IT-Dienstleister (Hosting, Wartung)</li>
                <li>Veranstalter (zur Durchführung der Events)</li>
              </ul>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">9. Ihre Rechte</h2>
              <p className="mb-2">Sie haben folgende Rechte bezüglich Ihrer personenbezogenen Daten:</p>
              <ul className="list-disc list-inside space-y-1 mb-4">
                <li>
                  <strong>Auskunftsrecht (Art. 15 DSGVO):</strong> Sie können Auskunft über Ihre gespeicherten
                  Daten verlangen
                </li>
                <li>
                  <strong>Berichtigungsrecht (Art. 16 DSGVO):</strong> Sie können die Korrektur unrichtiger Daten
                  verlangen
                </li>
                <li>
                  <strong>Löschungsrecht (Art. 17 DSGVO):</strong> Sie können die Löschung Ihrer Daten verlangen
                </li>
                <li>
                  <strong>Einschränkung der Verarbeitung (Art. 18 DSGVO):</strong> Sie können die Einschränkung
                  der Verarbeitung verlangen
                </li>
                <li>
                  <strong>Datenübertragbarkeit (Art. 20 DSGVO):</strong> Sie können Ihre Daten in einem
                  strukturierten Format erhalten
                </li>
                <li>
                  <strong>Widerspruchsrecht (Art. 21 DSGVO):</strong> Sie können der Verarbeitung Ihrer Daten
                  widersprechen
                </li>
                <li>
                  <strong>Beschwerderecht:</strong> Sie können sich bei einer Datenschutz-Aufsichtsbehörde
                  beschweren
                </li>
              </ul>
              <p>
                Zur Ausübung dieser Rechte kontaktieren Sie uns bitte unter{' '}
                <a href="mailto:datenschutz@concertfinder.de" className="text-primary hover:underline">
                  datenschutz@concertfinder.de
                </a>
              </p>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">10. Datensicherheit</h2>
              <p className="mb-2">
                Wir verwenden geeignete technische und organisatorische Sicherheitsmaßnahmen, um Ihre Daten gegen
                zufällige oder vorsätzliche Manipulationen, Verlust, Zerstörung oder den Zugriff unberechtigter
                Personen zu schützen.
              </p>
              <p className="mb-2">Unsere Sicherheitsmaßnahmen umfassen:</p>
              <ul className="list-disc list-inside space-y-1">
                <li>SSL/TLS-Verschlüsselung für die Datenübertragung</li>
                <li>Verschlüsselte Speicherung sensibler Daten</li>
                <li>Regelmäßige Sicherheitsupdates und Backups</li>
                <li>Zugriffskontrollen und Authentifizierung</li>
                <li>Schulung unserer Mitarbeiter im Datenschutz</li>
              </ul>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">11. Zahlungsdienstleister</h2>
              <p className="mb-2">
                Wir nutzen externe Zahlungsdienstleister, über deren Plattformen die Zahlungen abgewickelt werden.
              </p>
              <p className="mb-2">
                Bei der Zahlung werden Ihre Zahlungsdaten direkt an den Zahlungsdienstleister übermittelt. Wir
                erhalten keine vollständigen Kreditkartendaten, sondern nur eine Transaktionsbestätigung.
              </p>
              <p>
                Die Datenverarbeitung erfolgt auf Grundlage von Art. 6 Abs. 1 lit. b DSGVO (Vertragserfüllung).
              </p>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">12. Newsletter</h2>
              <p className="mb-2">
                Wenn Sie unseren Newsletter abonnieren, verwenden wir Ihre E-Mail-Adresse ausschließlich für den
                Versand des Newsletters.
              </p>
              <p className="mb-2">
                Die Rechtsgrundlage ist Ihre Einwilligung (Art. 6 Abs. 1 lit. a DSGVO). Sie können Ihre
                Einwilligung jederzeit widerrufen, indem Sie auf den Abmeldelink im Newsletter klicken.
              </p>
              <p>
                Wir nutzen das Double-Opt-In-Verfahren, um sicherzustellen, dass die Anmeldung mit Ihrer
                Zustimmung erfolgt.
              </p>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">13. Änderungen der Datenschutzerklärung</h2>
              <p className="mb-2">
                Wir behalten uns vor, diese Datenschutzerklärung anzupassen, um sie an geänderte Rechtslagen oder
                Änderungen unserer Dienste anzupassen.
              </p>
              <p>
                Für erneute Besuche gilt dann die neue Datenschutzerklärung. Wir empfehlen Ihnen, diese
                Datenschutzerklärung regelmäßig zu lesen.
              </p>
            </section>

            <section className="mt-6 pt-6 border-t border-[#e5e7eb]">
              <p className="text-sm">
                <strong>Stand:</strong> Januar 2026
              </p>
            </section>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="w-full bg-white border-t border-[#f0f3f4] py-6">
        <div className="max-w-[1440px] mx-auto px-4 sm:px-10 text-center text-[#657886] text-sm">
          <p>&copy; 2026 ConcertFinder. Alle Rechte vorbehalten.</p>
        </div>
      </footer>
    </div>
  );
}

export default PrivacyPage;
