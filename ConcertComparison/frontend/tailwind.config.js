/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
    "./public/index.html"
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        primary: '#1e75b3',
        'primary-dark': '#155a8a',
        'primary-light': '#3d8fc4',
        price: '#FF7F0E',
        'background-light': '#f6f7f8',
        'background-dark': '#121a20',
        'card-light': '#ffffff',
        'card-dark': '#1a2630',
        'text-primary': '#121517',
        'text-secondary': '#657886',
        'border-light': '#e5e7eb',
        'border-dark': '#374151',
      },
      fontFamily: {
        display: ['Inter', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        'card': '0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06)',
        'card-hover': '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
      },
    },
  },
  plugins: [],
}
