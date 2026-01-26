// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import '@testing-library/jest-dom';

// Increase test timeout for slow tests
jest.setTimeout(15000);

// Global cleanup to prevent hanging tests
afterEach(() => {
  // Clean up any pending timers
  if (typeof jest !== 'undefined' && jest.isMockFunction && jest.isMockFunction(setTimeout)) {
    jest.runOnlyPendingTimers();
    jest.useRealTimers();
  }
});

// Suppress specific console errors during tests
const originalError = console.error;
beforeAll(() => {
  console.error = (...args) => {
    // Suppress act() warnings during tests (these are common and usually not actionable)
    if (typeof args[0] === 'string' && args[0].includes('was not wrapped in act')) {
      return;
    }
    originalError.call(console, ...args);
  };
});

afterAll(() => {
  console.error = originalError;
});
