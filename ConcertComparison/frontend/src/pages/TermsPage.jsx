import React from 'react';
import { useNavigate } from 'react-router-dom';

function TermsPage() {
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
            Allgemeine Geschäftsbedingungen
          </h1>

          <div className="space-y-6 text-[#657886]">
            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">1. Geltungsbereich</h2>
              <p className="mb-2">
                Diese Allgemeinen Geschäftsbedingungen (AGB) gelten für alle Verträge über den Verkauf von
                Eintrittskarten für Veranstaltungen, die über die Plattform ConcertFinder abgeschlossen werden.
              </p>
              <p>
                Vertragspartner ist ConcertFinder GmbH, Musterstraße 123, 12345 Musterstadt, Deutschland.
              </p>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">2. Vertragsschluss</h2>
              <p className="mb-2">
                Mit der Bestellung von Tickets durch den Kunden gibt dieser ein verbindliches Angebot zum Abschluss
                eines Kaufvertrages ab. Der Vertrag kommt durch unsere Auftragsbestätigung per E-Mail zustande.
              </p>
              <p>
                Wir behalten uns vor, Bestellungen ohne Angabe von Gründen abzulehnen, insbesondere bei
                Nichtverfügbarkeit der gewünschten Tickets.
              </p>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">3. Preise und Zahlungsbedingungen</h2>
              <p className="mb-2">
                Alle Preise verstehen sich inklusive der gesetzlichen Mehrwertsteuer. Zusätzlich können
                Servicegebühren und Versandkosten anfallen, die vor Abschluss der Bestellung angezeigt werden.
              </p>
              <p className="mb-2">
                Die Zahlung erfolgt wahlweise per Kreditkarte, PayPal, SEPA-Lastschrift oder Sofortüberweisung.
              </p>
              <p>
                Bei Zahlung per Kreditkarte oder PayPal erfolgt die Belastung unmittelbar nach Vertragsschluss.
              </p>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">4. Lieferung der Tickets</h2>
              <p className="mb-2">
                Die Tickets werden nach erfolgreicher Zahlung per E-Mail als PDF-Datei (E-Ticket) oder über die
                mobile App bereitgestellt.
              </p>
              <p>
                Der Kunde ist verpflichtet, das Ticket beim Einlass zur Veranstaltung vorzuzeigen. Dies kann
                digital oder in ausgedruckter Form erfolgen.
              </p>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">5. Widerrufsrecht</h2>
              <p className="mb-2">
                Gemäß § 312g Abs. 2 Nr. 9 BGB besteht bei Verträgen über Freizeitdienstleistungen, die zu einem
                bestimmten Zeitpunkt oder innerhalb eines bestimmten Zeitraums zu erbringen sind, kein
                Widerrufsrecht.
              </p>
              <p>
                Tickets für Veranstaltungen können daher nach Vertragsschluss nicht widerrufen oder zurückgegeben
                werden.
              </p>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">6. Stornierung und Erstattung</h2>
              <p className="mb-2">
                Bei Absage oder Verlegung einer Veranstaltung durch den Veranstalter wird der Ticketpreis
                vollständig erstattet. Servicegebühren werden nicht erstattet.
              </p>
              <p>
                Eine Stornierung durch den Kunden ist grundsätzlich ausgeschlossen. In begründeten Ausnahmefällen
                kann eine Umbuchung auf eine andere Person möglich sein.
              </p>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">7. Personalisierung und Weitergabe</h2>
              <p className="mb-2">
                Tickets sind grundsätzlich übertragbar, sofern nicht ausdrücklich anders angegeben.
              </p>
              <p>
                Der gewerbliche Weiterverkauf von Tickets über dem aufgedruckten Nennwert ist untersagt und kann
                zur Ungültigkeit des Tickets führen.
              </p>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">8. Haftung</h2>
              <p className="mb-2">
                Wir haften nur für Vorsatz und grobe Fahrlässigkeit. Bei leichter Fahrlässigkeit haften wir nur
                für die Verletzung wesentlicher Vertragspflichten.
              </p>
              <p>
                Für die Durchführung der Veranstaltung ist ausschließlich der Veranstalter verantwortlich. Wir
                übernehmen keine Haftung für die ordnungsgemäße Durchführung der Veranstaltung.
              </p>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">9. Hausrecht und Einlassbedingungen</h2>
              <p className="mb-2">
                Der Veranstalter behält sich das Recht vor, Personen ohne Angabe von Gründen den Zutritt zur
                Veranstaltung zu verweigern oder diese der Veranstaltung zu verweisen.
              </p>
              <p>
                Es gelten die jeweiligen Hausordnungen und Einlassbedingungen des Veranstaltungsortes sowie
                gesetzliche Bestimmungen (z.B. Jugendschutzgesetz).
              </p>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">10. Datenschutz</h2>
              <p>
                Der Umgang mit personenbezogenen Daten erfolgt gemäß unserer Datenschutzerklärung, die Sie unter{' '}
                <a href="/privacy" className="text-primary hover:underline font-medium">
                  Datenschutzrichtlinie
                </a>{' '}
                einsehen können.
              </p>
            </section>

            <section>
              <h2 className="text-[#121517] text-xl font-bold mb-3">11. Schlussbestimmungen</h2>
              <p className="mb-2">
                Es gilt das Recht der Bundesrepublik Deutschland unter Ausschluss des UN-Kaufrechts.
              </p>
              <p className="mb-2">
                Sollten einzelne Bestimmungen dieser AGB unwirksam sein oder werden, berührt dies die Wirksamkeit
                der übrigen Bestimmungen nicht.
              </p>
              <p>Stand: Januar 2026</p>
            </section>

            <section className="mt-8 pt-6 border-t border-[#e5e7eb]">
              <h2 className="text-[#121517] text-xl font-bold mb-3">Kontakt</h2>
              <p className="mb-1">
                <strong>ConcertFinder GmbH</strong>
              </p>
              <p className="mb-1">Musterstraße 123</p>
              <p className="mb-1">12345 Musterstadt</p>
              <p className="mb-1">Deutschland</p>
              <p className="mb-1">
                E-Mail:{' '}
                <a href="mailto:support@concertfinder.de" className="text-primary hover:underline">
                  support@concertfinder.de
                </a>
              </p>
              <p>
                Telefon:{' '}
                <a href="tel:+4912345678900" className="text-primary hover:underline">
                  +49 (0) 123 456 789-00
                </a>
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

export default TermsPage;
