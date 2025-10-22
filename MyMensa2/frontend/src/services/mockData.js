/**
 * Mock-Daten für Frontend-Testing
 * Diese Daten können später einfach durch echte API-Calls ersetzt werden
 */

// Gerichte (Meals)
export const mockMeals = [
  {
    id: 1,
    name: "Spaghetti Carbonara",
    description: "Klassische italienische Pasta mit Sahnesauce, Speck und Parmesan",
    price: 6.50,
    cost: 2.80,
    category: "standard",
    calories: 680,
    protein: 25,
    carbohydrates: 75,
    fat: 28,
    deleted: false,
    deletedAt: null,
    allergens: ["Gluten", "Eier", "Milch"]
  },
  {
    id: 2,
    name: "Gemüse-Curry",
    description: "Aromatisches Curry mit saisonalem Gemüse, Kokosmilch und Basmatireis",
    price: 5.90,
    cost: 2.20,
    category: "vegan",
    calories: 520,
    protein: 12,
    carbohydrates: 68,
    fat: 18,
    deleted: false,
    deletedAt: null,
    allergens: []
  },
  {
    id: 3,
    name: "Hähnchen-Schnitzel",
    description: "Paniertes Hähnchenschnitzel mit Pommes Frites und Salat",
    price: 7.20,
    cost: 3.40,
    category: "halal",
    calories: 780,
    protein: 42,
    carbohydrates: 62,
    fat: 35,
    deleted: false,
    deletedAt: null,
    allergens: ["Gluten", "Eier"]
  },
  {
    id: 4,
    name: "Linsen-Dal",
    description: "Indisches Linsengericht mit Reis und frischem Koriander",
    price: 5.50,
    cost: 1.90,
    category: "vegan",
    calories: 450,
    protein: 18,
    carbohydrates: 72,
    fat: 8,
    deleted: false,
    deletedAt: null,
    allergens: []
  },
  {
    id: 5,
    name: "Quinoa-Salat",
    description: "Frischer Quinoa-Salat mit Feta, Tomaten, Gurken und Oliven",
    price: 6.80,
    cost: 2.60,
    category: "vegetarisch",
    calories: 420,
    protein: 15,
    carbohydrates: 48,
    fat: 18,
    deleted: false,
    deletedAt: null,
    allergens: ["Milch"]
  },
  {
    id: 6,
    name: "Rindergulasch",
    description: "Herzhaftes Rindergulasch mit Spätzle und Rotkohl",
    price: 7.90,
    cost: 3.80,
    category: "standard",
    calories: 720,
    protein: 38,
    carbohydrates: 58,
    fat: 32,
    deleted: false,
    deletedAt: null,
    allergens: ["Gluten", "Sellerie"]
  },
  {
    id: 7,
    name: "Falafel-Wrap",
    description: "Knusprige Falafel im Vollkorn-Wrap mit Hummus und Salat",
    price: 5.20,
    cost: 2.10,
    category: "vegan",
    calories: 490,
    protein: 16,
    carbohydrates: 64,
    fat: 18,
    deleted: false,
    deletedAt: null,
    allergens: ["Gluten", "Sesam"]
  },
  {
    id: 8,
    name: "Lachsfilet",
    description: "Gebratenes Lachsfilet mit Dill-Rahmsauce und Kartoffeln",
    price: 8.50,
    cost: 4.20,
    category: "glutenfrei",
    calories: 620,
    protein: 42,
    carbohydrates: 38,
    fat: 28,
    deleted: false,
    deletedAt: null,
    allergens: ["Fisch", "Milch"]
  }
];

// Speisepläne für komplette Woche (Montag-Freitag, 20.-24. Oktober 2025)
// KW 43: Mo 20.10., Di 21.10., Mi 22.10. (heute), Do 23.10., Fr 24.10.
export const mockMealPlans = [
  // Montag, 20. Oktober 2025 - 5 Gerichte
  { mealId: 1, date: "2025-10-20", stock: 50, meal: mockMeals[0] }, // Carbonara
  { mealId: 2, date: "2025-10-20", stock: 40, meal: mockMeals[1] }, // Gemüse-Curry
  { mealId: 3, date: "2025-10-20", stock: 35, meal: mockMeals[2] }, // Hähnchen-Schnitzel
  { mealId: 4, date: "2025-10-20", stock: 30, meal: mockMeals[3] }, // Linsen-Dal
  { mealId: 7, date: "2025-10-20", stock: 25, meal: mockMeals[6] }, // Falafel-Wrap
  
  // Dienstag, 21. Oktober 2025 - 4 Gerichte
  { mealId: 5, date: "2025-10-21", stock: 45, meal: mockMeals[4] }, // Quinoa-Salat
  { mealId: 6, date: "2025-10-21", stock: 32, meal: mockMeals[5] }, // Rindergulasch
  { mealId: 8, date: "2025-10-21", stock: 28, meal: mockMeals[7] }, // Lachsfilet
  { mealId: 2, date: "2025-10-21", stock: 38, meal: mockMeals[1] }, // Gemüse-Curry
  
  // Mittwoch, 22. Oktober 2025 (HEUTE) - 5 Gerichte
  { mealId: 1, date: "2025-10-22", stock: 45, meal: mockMeals[0] }, // Carbonara
  { mealId: 2, date: "2025-10-22", stock: 30, meal: mockMeals[1] }, // Gemüse-Curry
  { mealId: 3, date: "2025-10-22", stock: 38, meal: mockMeals[2] }, // Hähnchen-Schnitzel
  { mealId: 7, date: "2025-10-22", stock: 25, meal: mockMeals[6] }, // Falafel-Wrap
  { mealId: 8, date: "2025-10-22", stock: 20, meal: mockMeals[7] }, // Lachsfilet
  
  // Donnerstag, 23. Oktober 2025 - 4 Gerichte
  { mealId: 4, date: "2025-10-23", stock: 40, meal: mockMeals[3] }, // Linsen-Dal
  { mealId: 5, date: "2025-10-23", stock: 35, meal: mockMeals[4] }, // Quinoa-Salat
  { mealId: 6, date: "2025-10-23", stock: 28, meal: mockMeals[5] }, // Rindergulasch
  { mealId: 1, date: "2025-10-23", stock: 42, meal: mockMeals[0] }, // Carbonara
  
  // Freitag, 24. Oktober 2025 - 5 Gerichte
  { mealId: 8, date: "2025-10-24", stock: 35, meal: mockMeals[7] }, // Lachsfilet
  { mealId: 2, date: "2025-10-24", stock: 40, meal: mockMeals[1] }, // Gemüse-Curry
  { mealId: 3, date: "2025-10-24", stock: 30, meal: mockMeals[2] }, // Hähnchen-Schnitzel
  { mealId: 4, date: "2025-10-24", stock: 32, meal: mockMeals[3] }, // Linsen-Dal
  { mealId: 7, date: "2025-10-24", stock: 28, meal: mockMeals[6] }  // Falafel-Wrap
];

// Bestellungen
export const mockOrders = [
  {
    id: 1,
    mealId: 1,
    meal: mockMeals[0],
    quantity: 2,
    orderDate: "2025-10-22",
    qrCode: "QR-2025-001-A7B3",
    paid: true,
    collected: false,
    orderTime: "2025-10-22T09:15:00"
  },
  {
    id: 2,
    mealId: 2,
    meal: mockMeals[1],
    quantity: 1,
    orderDate: "2025-10-22",
    qrCode: "QR-2025-002-C4D9",
    paid: true,
    collected: true,
    orderTime: "2025-10-22T09:22:00",
    collectedTime: "2025-10-22T12:05:00"
  },
  {
    id: 3,
    mealId: 3,
    meal: mockMeals[2],
    quantity: 1,
    orderDate: "2025-10-22",
    qrCode: "QR-2025-003-E8F2",
    paid: false,
    collected: false,
    orderTime: "2025-10-22T10:05:00"
  },
  {
    id: 4,
    mealId: 1,
    meal: mockMeals[0],
    quantity: 3,
    orderDate: "2025-10-22",
    qrCode: "QR-2025-004-G1H6",
    paid: true,
    collected: false,
    orderTime: "2025-10-22T10:30:00"
  },
  {
    id: 5,
    mealId: 7,
    meal: mockMeals[6],
    quantity: 2,
    orderDate: "2025-10-22",
    qrCode: "QR-2025-005-J5K8",
    paid: true,
    collected: true,
    orderTime: "2025-10-22T11:00:00",
    collectedTime: "2025-10-22T12:30:00"
  },
  {
    id: 6,
    mealId: 4,
    meal: mockMeals[3],
    quantity: 1,
    orderDate: "2025-10-23",
    qrCode: "QR-2025-006-L2M4",
    paid: true,
    collected: false,
    orderTime: "2025-10-22T14:20:00"
  }
];

// Dashboard-Daten
export const mockDashboardData = {
  totalRevenue: 487.50,
  totalCost: 198.20,
  totalOrders: 28,
  popularMeals: [
    { mealId: 1, mealName: "Spaghetti Carbonara", orderCount: 12 },
    { mealId: 2, mealName: "Gemüse-Curry", orderCount: 8 },
    { mealId: 7, mealName: "Falafel-Wrap", orderCount: 6 },
    { mealId: 3, mealName: "Hähnchen-Schnitzel", orderCount: 5 },
    { mealId: 4, mealName: "Linsen-Dal", orderCount: 4 }
  ]
};

// Lagerbestände
export const mockInventory = [
  {
    id: 1,
    ingredientName: "Tomaten",
    quantity: 50,
    unit: "kg",
    minStock: 20,
    supplier: "FOODSUPPLY",
    lastOrdered: "2025-10-20",
    category: "Gemüse"
  },
  {
    id: 2,
    ingredientName: "Kartoffeln",
    quantity: 15,
    unit: "kg",
    minStock: 30,
    supplier: "FOODSUPPLY",
    lastOrdered: "2025-10-18",
    category: "Gemüse"
  },
  {
    id: 3,
    ingredientName: "Reis",
    quantity: 85,
    unit: "kg",
    minStock: 50,
    supplier: "FOODSUPPLY",
    lastOrdered: "2025-10-15",
    category: "Getreide"
  },
  {
    id: 4,
    ingredientName: "Hähnchenbrust",
    quantity: 8,
    unit: "kg",
    minStock: 15,
    supplier: "FOODSUPPLY",
    lastOrdered: "2025-10-21",
    category: "Fleisch"
  },
  {
    id: 5,
    ingredientName: "Olivenöl",
    quantity: 12,
    unit: "L",
    minStock: 10,
    supplier: "FOODSUPPLY",
    lastOrdered: "2025-10-19",
    category: "Öle & Fette"
  },
  {
    id: 6,
    ingredientName: "Milch",
    quantity: 25,
    unit: "L",
    minStock: 20,
    supplier: "FOODSUPPLY",
    lastOrdered: "2025-10-22",
    category: "Milchprodukte"
  },
  {
    id: 7,
    ingredientName: "Linsen (rot)",
    quantity: 18,
    unit: "kg",
    minStock: 10,
    supplier: "FOODSUPPLY",
    lastOrdered: "2025-10-16",
    category: "Hülsenfrüchte"
  },
  {
    id: 8,
    ingredientName: "Zwiebeln",
    quantity: 22,
    unit: "kg",
    minStock: 15,
    supplier: "FOODSUPPLY",
    lastOrdered: "2025-10-20",
    category: "Gemüse"
  }
];

// Personal
export const mockStaff = [
  {
    id: 1,
    name: "Maria Schmidt",
    role: "Koch",
    hoursWorked: 40,
    availability: "Verfügbar",
    lastSync: "2025-10-22 09:00",
    email: "m.schmidt@mymensa.de",
    phone: "+49 123 456 7890"
  },
  {
    id: 2,
    name: "Thomas Müller",
    role: "Servicekraft",
    hoursWorked: 35,
    availability: "Verfügbar",
    lastSync: "2025-10-22 09:00",
    email: "t.mueller@mymensa.de",
    phone: "+49 123 456 7891"
  },
  {
    id: 3,
    name: "Anna Weber",
    role: "Koch",
    hoursWorked: 42,
    availability: "Urlaub",
    lastSync: "2025-10-22 09:00",
    email: "a.weber@mymensa.de",
    phone: "+49 123 456 7892"
  },
  {
    id: 4,
    name: "Klaus Fischer",
    role: "Servicekraft",
    hoursWorked: 30,
    availability: "Verfügbar",
    lastSync: "2025-10-22 09:00",
    email: "k.fischer@mymensa.de",
    phone: "+49 123 456 7893"
  },
  {
    id: 5,
    name: "Sarah Becker",
    role: "Koch",
    hoursWorked: 38,
    availability: "Verfügbar",
    lastSync: "2025-10-22 09:00",
    email: "s.becker@mymensa.de",
    phone: "+49 123 456 7894"
  },
  {
    id: 6,
    name: "Michael Hoffmann",
    role: "Servicekraft",
    hoursWorked: 32,
    availability: "Krank",
    lastSync: "2025-10-22 09:00",
    email: "m.hoffmann@mymensa.de",
    phone: "+49 123 456 7895"
  }
];

// Hilfsfunktion: Datum formatieren
export const formatDate = (date) => {
  const d = new Date(date);
  return d.toISOString().split('T')[0];
};

// Hilfsfunktion: Zufälligen QR-Code generieren
export const generateQRCode = () => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
  const segments = [
    'QR',
    new Date().getFullYear().toString(),
    Math.floor(Math.random() * 1000).toString().padStart(3, '0'),
    Array(4).fill(0).map(() => chars[Math.floor(Math.random() * chars.length)]).join('')
  ];
  return segments.join('-');
};

// Hilfsfunktion: Bestellung ID generieren
let orderIdCounter = mockOrders.length + 1;
export const generateOrderId = () => {
  return orderIdCounter++;
};
