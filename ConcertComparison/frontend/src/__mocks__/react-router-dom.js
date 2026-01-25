const React = require('react');

// Minimal mock of react-router-dom for tests
module.exports = {
  BrowserRouter: ({ children }) => React.createElement(React.Fragment, null, children),
  MemoryRouter: ({ children }) => React.createElement(React.Fragment, null, children),
  Routes: ({ children }) => React.createElement(React.Fragment, null, children),
  Route: () => null,
  Navigate: () => null,
  Link: ({ children }) => React.createElement('a', null, children),
  useNavigate: () => () => {},
};
