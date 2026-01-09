import React, { createContext, useContext, useReducer } from 'react';

const BookingContext = createContext();

// Initial State
const initialState = {
  currentStep: 1,
  totalSteps: 5,
  bookingData: {
    vehicleId: null,
    vehicle: null, // Fahrzeugdaten für Anzeige
    pickupBranchId: null,
    pickupBranch: null,
    returnBranchId: null,
    returnBranch: null,
    pickupDateTime: null,
    returnDateTime: null,
    extras: [], // Array von {id, name, price, quantity}
    basePrice: 0,
    extrasPrice: 0,
    totalPrice: 0,
  },
};

// Action Types
const ACTIONS = {
  SET_STEP: 'SET_STEP',
  NEXT_STEP: 'NEXT_STEP',
  PREV_STEP: 'PREV_STEP',
  SET_VEHICLE: 'SET_VEHICLE',
  SET_BRANCHES: 'SET_BRANCHES',
  SET_DATES: 'SET_DATES',
  SET_EXTRAS: 'SET_EXTRAS',
  CALCULATE_PRICE: 'CALCULATE_PRICE',
  RESET_BOOKING: 'RESET_BOOKING',
};

// Reducer
function bookingReducer(state, action) {
  switch (action.type) {
    case ACTIONS.SET_STEP:
      return {
        ...state,
        currentStep: Math.max(1, Math.min(action.payload, state.totalSteps)),
      };

    case ACTIONS.NEXT_STEP:
      return {
        ...state,
        currentStep: Math.min(state.currentStep + 1, state.totalSteps),
      };

    case ACTIONS.PREV_STEP:
      return {
        ...state,
        currentStep: Math.max(state.currentStep - 1, 1),
      };

    case ACTIONS.SET_VEHICLE: {
      const newState = {
        ...state,
        bookingData: {
          ...state.bookingData,
          vehicleId: action.payload.id,
          vehicle: action.payload,
          basePrice: action.payload.pricePerDay || 0,
        },
      };
      // Auto-berechne Preis wenn Datum schon gesetzt ist
      if (newState.bookingData.pickupDateTime && newState.bookingData.returnDateTime) {
        const pickup = new Date(newState.bookingData.pickupDateTime);
        const returnDate = new Date(newState.bookingData.returnDateTime);
        const diffTime = Math.abs(returnDate - pickup);
        const rentalDays = Math.max(1, Math.ceil(diffTime / (1000 * 60 * 60 * 24)));
        
        const extrasPrice = newState.bookingData.extras.reduce((sum, extra) => {
          return sum + (extra.pricePerDay * rentalDays * (extra.quantity || 1));
        }, 0);
        
        newState.bookingData.extrasPrice = extrasPrice;
        newState.bookingData.totalPrice = (newState.bookingData.basePrice * rentalDays) + extrasPrice;
      }
      return newState;
    }

    case ACTIONS.SET_BRANCHES:
      return {
        ...state,
        bookingData: {
          ...state.bookingData,
          pickupBranchId: action.payload.pickupBranchId,
          pickupBranch: action.payload.pickupBranch,
          returnBranchId: action.payload.returnBranchId,
          returnBranch: action.payload.returnBranch,
        },
      };

    case ACTIONS.SET_DATES: {
      const newState = {
        ...state,
        bookingData: {
          ...state.bookingData,
          pickupDateTime: action.payload.pickupDateTime,
          returnDateTime: action.payload.returnDateTime,
        },
      };
      // Auto-berechne Preis wenn Fahrzeug schon gesetzt ist
      if (newState.bookingData.basePrice > 0 && newState.bookingData.pickupDateTime && newState.bookingData.returnDateTime) {
        const pickup = new Date(newState.bookingData.pickupDateTime);
        const returnDate = new Date(newState.bookingData.returnDateTime);
        const diffTime = Math.abs(returnDate - pickup);
        const rentalDays = Math.max(1, Math.ceil(diffTime / (1000 * 60 * 60 * 24)));
        
        const extrasPrice = newState.bookingData.extras.reduce((sum, extra) => {
          return sum + (extra.pricePerDay * rentalDays * (extra.quantity || 1));
        }, 0);
        
        newState.bookingData.extrasPrice = extrasPrice;
        newState.bookingData.totalPrice = (newState.bookingData.basePrice * rentalDays) + extrasPrice;
      }
      return newState;
    }

    case ACTIONS.SET_EXTRAS:
      return {
        ...state,
        bookingData: {
          ...state.bookingData,
          extras: action.payload,
        },
      };

    case ACTIONS.CALCULATE_PRICE: {
      const { basePrice, pickupDateTime, returnDateTime, extras } = state.bookingData;

      // Berechne Anzahl Tage
      let rentalDays = 1;
      if (pickupDateTime && returnDateTime) {
        const pickup = new Date(pickupDateTime);
        const returnDate = new Date(returnDateTime);
        const diffTime = Math.abs(returnDate - pickup);
        rentalDays = Math.max(1, Math.ceil(diffTime / (1000 * 60 * 60 * 24)));
      }

      // Berechne Extras-Preis
      const extrasPrice = extras.reduce((sum, extra) => {
        return sum + (extra.pricePerDay * rentalDays * (extra.quantity || 1));
      }, 0);

      // Gesamtpreis
      const totalPrice = (basePrice * rentalDays) + extrasPrice;

      return {
        ...state,
        bookingData: {
          ...state.bookingData,
          extrasPrice,
          totalPrice,
        },
      };
    }

    case ACTIONS.RESET_BOOKING:
      return initialState;

    default:
      return state;
  }
}

// Provider Component
export function BookingProvider({ children }) {
  const [state, dispatch] = useReducer(bookingReducer, initialState);

  const value = {
    ...state,
    setStep: (step) => dispatch({ type: ACTIONS.SET_STEP, payload: step }),
    nextStep: () => dispatch({ type: ACTIONS.NEXT_STEP }),
    prevStep: () => dispatch({ type: ACTIONS.PREV_STEP }),
    setVehicle: (vehicle) => dispatch({ type: ACTIONS.SET_VEHICLE, payload: vehicle }),
    setBranches: (branches) => dispatch({ type: ACTIONS.SET_BRANCHES, payload: branches }),
    setDates: (dates) => dispatch({ type: ACTIONS.SET_DATES, payload: dates }),
    setExtras: (extras) => dispatch({ type: ACTIONS.SET_EXTRAS, payload: extras }),
    calculatePrice: () => dispatch({ type: ACTIONS.CALCULATE_PRICE }),
    resetBooking: () => dispatch({ type: ACTIONS.RESET_BOOKING }),
  };

  return (
    <BookingContext.Provider value={value}>
      {children}
    </BookingContext.Provider>
  );
}

// Custom Hook
export function useBooking() {
  const context = useContext(BookingContext);
  if (!context) {
    throw new Error('useBooking muss innerhalb eines BookingProvider verwendet werden');
  }
  return context;
}

export default BookingContext;
