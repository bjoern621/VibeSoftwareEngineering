/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
    "./public/index.html",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        'primary': '#1976D2',
        'secondary': '#FF9800',
        'background-light': '#F5F5F5',
        'text-main': '#212121',
        'card-bg': '#FFFFFF',
      },
      fontFamily: {
        'display': ['Manrope', 'sans-serif'],
      },
      borderRadius: {
        'DEFAULT': '0.5rem',
        'lg': '0.75rem',
        'xl': '1rem',
        'full': '9999px',
      },
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}

