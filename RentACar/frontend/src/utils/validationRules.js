/**
 * Zentrale Validierungsregeln mit Yup
 * 
 * Deutsche Fehlermeldungen für alle gängigen Validierungsszenarien
 */

import * as yup from 'yup';

// Yup Locale auf Deutsch setzen
yup.setLocale({
  mixed: {
    default: 'Ungültiger Wert',
    required: 'Dieses Feld ist erforderlich',
    notType: 'Ungültiger Typ',
  },
  string: {
    email: 'Bitte geben Sie eine gültige E-Mail-Adresse ein',
    min: ({ min }) => `Mindestens ${min} Zeichen erforderlich`,
    max: ({ max }) => `Maximal ${max} Zeichen erlaubt`,
    length: ({ length }) => `Genau ${length} Zeichen erforderlich`,
  },
  number: {
    min: ({ min }) => `Wert muss mindestens ${min} sein`,
    max: ({ max }) => `Wert darf maximal ${max} sein`,
    positive: 'Wert muss positiv sein',
  },
  date: {
    min: ({ min }) => `Datum muss nach dem ${new Date(min).toLocaleDateString('de-DE')} liegen`,
    max: ({ max }) => `Datum muss vor dem ${new Date(max).toLocaleDateString('de-DE')} liegen`,
  },
});

/**
 * Validierungsregeln für einzelne Felder
 */

// E-Mail-Validierung
export const emailValidation = yup
  .string()
  .required('E-Mail ist erforderlich')
  .email('Bitte geben Sie eine gültige E-Mail-Adresse ein')
  .matches(
    /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
    'E-Mail-Format ist ungültig'
  );

// Passwort-Validierung (min. 8 Zeichen)
export const passwordValidation = yup
  .string()
  .required('Passwort ist erforderlich')
  .min(8, 'Passwort muss mindestens 8 Zeichen lang sein');

// Passwort-Validierung mit Stärke-Anforderungen (optional, für striktere Validierung)
export const strongPasswordValidation = yup
  .string()
  .required('Passwort ist erforderlich')
  .min(8, 'Passwort muss mindestens 8 Zeichen lang sein')
  .matches(
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/,
    'Passwort muss mindestens einen Großbuchstaben, einen Kleinbuchstaben, eine Ziffer und ein Sonderzeichen enthalten'
  );

// Passwort-Bestätigung
export const passwordConfirmValidation = (passwordFieldName = 'password') =>
  yup
    .string()
    .required('Passwort-Bestätigung ist erforderlich')
    .oneOf([yup.ref(passwordFieldName)], 'Passwörter stimmen nicht überein');

// Name-Validierung (Vor- und Nachname)
export const nameValidation = yup
  .string()
  .required('Name ist erforderlich')
  .min(2, 'Name muss mindestens 2 Zeichen lang sein')
  .max(50, 'Name darf maximal 50 Zeichen lang sein')
  .matches(/^[a-zA-ZäöüÄÖÜß\s-]+$/, 'Name darf nur Buchstaben, Leerzeichen und Bindestriche enthalten');

// Telefonnummer-Validierung (deutsches Format)
export const phoneValidation = yup
  .string()
  .required('Telefonnummer ist erforderlich')
  .matches(
    /^(\+49|0)[1-9]\d{1,14}$/,
    'Bitte geben Sie eine gültige deutsche Telefonnummer ein (z.B. 0171234567 oder +491712345678)'
  );

// PLZ-Validierung (5 Ziffern, deutsches Format)
export const postalCodeValidation = yup
  .string()
  .required('Postleitzahl ist erforderlich')
  .matches(/^\d{5}$/, 'Postleitzahl muss aus genau 5 Ziffern bestehen');

// Straße-Validierung
export const streetValidation = yup
  .string()
  .required('Straße ist erforderlich')
  .min(3, 'Straße muss mindestens 3 Zeichen lang sein')
  .max(100, 'Straße darf maximal 100 Zeichen lang sein');

// Stadt-Validierung
export const cityValidation = yup
  .string()
  .required('Stadt ist erforderlich')
  .min(2, 'Stadt muss mindestens 2 Zeichen lang sein')
  .max(50, 'Stadt darf maximal 50 Zeichen lang sein')
  .matches(/^[a-zA-ZäöüÄÖÜß\s-]+$/, 'Stadt darf nur Buchstaben, Leerzeichen und Bindestriche enthalten');

// Führerscheinnummer-Validierung (11 alphanumerische Zeichen)
export const driverLicenseValidation = yup
  .string()
  .required('Führerscheinnummer ist erforderlich')
  .matches(
    /^[A-Z0-9]{11}$/,
    'Führerscheinnummer muss aus genau 11 Großbuchstaben und Ziffern bestehen'
  );

// Datum-Validierung (muss in der Zukunft liegen)
export const futureDateValidation = yup
  .date()
  .required('Datum ist erforderlich')
  .min(new Date(), 'Datum muss in der Zukunft liegen')
  .typeError('Ungültiges Datum');

// Datum-Validierung (muss in der Vergangenheit liegen)
export const pastDateValidation = yup
  .date()
  .required('Datum ist erforderlich')
  .max(new Date(), 'Datum muss in der Vergangenheit liegen')
  .typeError('Ungültiges Datum');

// Datum-Bereich-Validierung (von < bis)
export const dateRangeValidation = {
  pickupDateTime: yup
    .date()
    .required('Abholzeitpunkt ist erforderlich')
    .min(new Date(), 'Abholzeitpunkt muss in der Zukunft liegen')
    .typeError('Ungültiges Datum'),
  
  returnDateTime: yup
    .date()
    .required('Rückgabezeitpunkt ist erforderlich')
    .min(
      yup.ref('pickupDateTime'),
      'Rückgabezeitpunkt muss nach dem Abholzeitpunkt liegen'
    )
    .test(
      'min-duration',
      'Mindestmietdauer beträgt 24 Stunden',
      function (value) {
        const { pickupDateTime } = this.parent;
        if (!pickupDateTime || !value) return true;
        const diffInHours = (new Date(value) - new Date(pickupDateTime)) / (1000 * 60 * 60);
        return diffInHours >= 24;
      }
    )
    .typeError('Ungültiges Datum'),
};

// Fahrzeugtyp-Validierung
export const vehicleTypeValidation = yup
  .string()
  .required('Fahrzeugtyp ist erforderlich')
  .oneOf(
    ['COMPACT', 'SEDAN', 'SUV', 'VAN', 'LUXURY', 'SPORTS'],
    'Ungültiger Fahrzeugtyp'
  );

// Kennzeichen-Validierung (deutsches Format)
export const licensePlateValidation = yup
  .string()
  .required('Kennzeichen ist erforderlich')
  .matches(
    /^[A-ZÄÖÜ]{1,3}-[A-Z]{1,2}\s?\d{1,4}$/,
    'Ungültiges Kennzeichen-Format (z.B. B-AB 1234)'
  );

// Kilometerstand-Validierung
export const mileageValidation = yup
  .number()
  .required('Kilometerstand ist erforderlich')
  .positive('Kilometerstand muss eine positive Zahl sein')
  .integer('Kilometerstand muss eine ganze Zahl sein')
  .min(0, 'Kilometerstand darf nicht negativ sein')
  .max(999999, 'Kilometerstand ist zu hoch')
  .typeError('Kilometerstand muss eine Zahl sein');

// Jahr-Validierung (Baujahr)
export const yearValidation = yup
  .number()
  .required('Baujahr ist erforderlich')
  .positive('Baujahr muss eine positive Zahl sein')
  .integer('Baujahr muss eine ganze Zahl sein')
  .min(1900, 'Baujahr muss nach 1900 liegen')
  .max(new Date().getFullYear() + 1, 'Baujahr darf nicht in der Zukunft liegen')
  .typeError('Baujahr muss eine Zahl sein');

// Preis-Validierung
export const priceValidation = yup
  .number()
  .required('Preis ist erforderlich')
  .positive('Preis muss positiv sein')
  .min(0.01, 'Preis muss mindestens 0,01 € betragen')
  .typeError('Preis muss eine Zahl sein');

/**
 * Vollständige Schema-Definitionen für Forms
 */

// Registrierungs-Schema
export const registrationSchema = yup.object({
  firstName: nameValidation,
  lastName: nameValidation,
  email: emailValidation,
  password: passwordValidation,
  phoneNumber: phoneValidation,
  driverLicenseNumber: driverLicenseValidation,
  street: streetValidation,
  postalCode: postalCodeValidation,
  city: cityValidation,
});

// Login-Schema
export const loginSchema = yup.object({
  email: emailValidation,
  password: yup.string().required('Passwort ist erforderlich'),
});

// Profil-Schema
export const profileSchema = yup.object({
  street: streetValidation,
  postalCode: postalCodeValidation,
  city: cityValidation,
  email: emailValidation,
  phoneNumber: phoneValidation,
});

// Passwort-Ändern-Schema
export const changePasswordSchema = yup.object({
  currentPassword: yup.string().required('Aktuelles Passwort ist erforderlich'),
  newPassword: passwordValidation,
  confirmPassword: passwordConfirmValidation('newPassword'),
});

// Buchungs-Schema
export const bookingSchema = yup.object({
  vehicleType: vehicleTypeValidation,
  ...dateRangeValidation,
});

// Fahrzeug-Schema (für Mitarbeiter)
export const vehicleSchema = yup.object({
  licensePlate: licensePlateValidation,
  brand: yup.string().required('Marke ist erforderlich').min(2, 'Marke muss mindestens 2 Zeichen lang sein'),
  model: yup.string().required('Modell ist erforderlich').min(1, 'Modell muss mindestens 1 Zeichen lang sein'),
  year: yearValidation,
  mileage: mileageValidation,
  vehicleType: vehicleTypeValidation,
  dailyRate: priceValidation,
});

/**
 * Utility-Funktionen
 */

// Passwort-Stärke berechnen (0-4)
export const calculatePasswordStrength = (password) => {
  if (!password) return 0;

  let strength = 0;

  // Mindestlänge 8 Zeichen
  if (password.length >= 8) strength++;

  // Enthält Kleinbuchstaben
  if (/[a-z]/.test(password)) strength++;

  // Enthält Großbuchstaben
  if (/[A-Z]/.test(password)) strength++;

  // Enthält Ziffern
  if (/\d/.test(password)) strength++;

  // Enthält Sonderzeichen
  if (/[@$!%*?&]/.test(password)) strength++;

  // Sehr lang (>12 Zeichen)
  if (password.length >= 12) strength++;

  // Normalisiere auf 0-4
  return Math.min(Math.floor(strength / 1.5), 4);
};

// Passwort-Stärke als Text
export const getPasswordStrengthText = (strength) => {
  const texts = ['Sehr schwach', 'Schwach', 'Mittel', 'Stark', 'Sehr stark'];
  return texts[strength] || 'Ungültig';
};

// Passwort-Stärke als Farbe
export const getPasswordStrengthColor = (strength) => {
  const colors = ['red', 'orange', 'yellow', 'green', 'green'];
  return colors[strength] || 'gray';
};

export default yup;
