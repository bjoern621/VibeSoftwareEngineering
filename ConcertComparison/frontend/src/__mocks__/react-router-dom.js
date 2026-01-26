const React = require('react');

// Minimal mock of react-router-dom for tests
module.exports = {
  BrowserRouter: ({ children }) => React.createElement(React.Fragment, null, children),
  MemoryRouter: ({ children }) => React.createElement(React.Fragment, null, children),
  Routes: ({ children }) => React.createElement(React.Fragment, null, children),
  Route: () => null,
  Navigate: () => null,
  Link: ({ children, to }) => React.createElement('a', { href: to }, children),
  NavLink: ({ children, to }) => React.createElement('a', { href: to }, children),
  useNavigate: () => () => {},
  useLocation: () => ({ pathname: '/', state: null }),
  useParams: () => ({}),
};
