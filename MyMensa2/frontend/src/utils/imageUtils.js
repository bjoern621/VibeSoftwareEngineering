/**
 * Utility-Funktionen für Bild-URLs
 */

/**
 * Gibt die passende Bild-URL für ein Gericht basierend auf dem Namen zurück
 * Verwendet intelligentes Keyword-Matching
 *
 * @param {string} mealName - Name des Gerichts
 * @returns {string} - Unsplash Bild-URL
 */
export const getMealImage = (mealName) => {
  if (!mealName) return getDefaultFoodImage();

  const nameLower = mealName.toLowerCase();

  // Keyword-basierte Bild-Zuordnung (Reihenfolge wichtig - spezifische Keywords zuerst)
  const keywordImages = [
    // Pizza (muss vor Fleisch/Fisch stehen, damit "Salami Pizza" nicht als Fleisch erkannt wird)
    { keywords: ['pizza'], url: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400&h=300&fit=crop' },

    // Pasta & Nudeln
    { keywords: ['spaghetti', 'bolognese'], url: 'https://images.unsplash.com/photo-1612874742237-6526221588e3?w=400&h=300&fit=crop' },
    { keywords: ['pasta', 'nudeln', 'penne', 'fusilli', 'linguine'], url: 'https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?w=400&h=300&fit=crop' },
    { keywords: ['lasagne', 'lasagna'], url: 'https://images.unsplash.com/photo-1574894709920-11b28e7367e3?w=400&h=300&fit=crop' },

    // Burger & Sandwiches
    { keywords: ['burger', 'cheeseburger'], url: 'https://images.unsplash.com/photo-1520072959219-c595dc870360?w=400&h=300&fit=crop' },
    { keywords: ['sandwich', 'panini', 'wrap'], url: 'https://images.unsplash.com/photo-1509722747041-616f39b57569?w=400&h=300&fit=crop' },

    // Salate & Bowls
    { keywords: ['caesar', 'salad', 'salat'], url: 'https://images.unsplash.com/photo-1546793665-c74683f339c1?w=400&h=300&fit=crop' },
    { keywords: ['bowl', 'quinoa', 'buddha'], url: 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&h=300&fit=crop' },
    { keywords: ['poke'], url: 'https://images.unsplash.com/photo-1546069901-d5bfd2cbfb1f?w=400&h=300&fit=crop' },

    // Fleisch & Geflügel
    { keywords: ['currywurst'], url: 'https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=400&h=300&fit=crop' },
    { keywords: ['hähnchen', 'chicken', 'hühnchen'], url: 'https://images.unsplash.com/photo-1598103442097-8b74394b95c6?w=400&h=300&fit=crop' },
    { keywords: ['shawarma', 'kebab', 'döner'], url: 'https://images.unsplash.com/photo-1529006557810-274b9b2fc783?w=400&h=300&fit=crop' },
    { keywords: ['schnitzel'], url: 'https://images.unsplash.com/photo-1432139509613-5c4255815697?w=400&h=300&fit=crop' },
    { keywords: ['steak', 'rind'], url: 'https://images.unsplash.com/photo-1600891964092-4316c288032e?w=400&h=300&fit=crop' },

    // Fisch & Meeresfrüchte
    { keywords: ['lachs', 'salmon'], url: 'https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=400&h=300&fit=crop' },
    { keywords: ['thunfisch', 'tuna'], url: 'https://images.unsplash.com/photo-1580959375944-57ed7960dc31?w=400&h=300&fit=crop' },
    { keywords: ['fisch', 'fish'], url: 'https://images.unsplash.com/photo-1580959375944-57ed7960dc31?w=400&h=300&fit=crop' },
    { keywords: ['garnelen', 'shrimp'], url: 'https://images.unsplash.com/photo-1565680018434-b513d5e5fd47?w=400&h=300&fit=crop' },

    // Vegetarisch & Vegan
    { keywords: ['falafel'], url: 'https://images.unsplash.com/photo-1593001874117-5b0d6c1b6c51?w=400&h=300&fit=crop' },
    { keywords: ['tofu'], url: 'https://images.unsplash.com/photo-1546069901-d5bfd2cbfb1f?w=400&h=300&fit=crop' },
    { keywords: ['gemüse', 'vegetable'], url: 'https://images.unsplash.com/photo-1540420773420-3366772f4999?w=400&h=300&fit=crop' },

    // Asiatisch
    { keywords: ['sushi'], url: 'https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=400&h=300&fit=crop' },
    { keywords: ['ramen', 'nudelsuppe'], url: 'https://images.unsplash.com/photo-1557872943-16a5ac26437e?w=400&h=300&fit=crop' },
    { keywords: ['curry'], url: 'https://images.unsplash.com/photo-1588166524941-3bf61a9c41db?w=400&h=300&fit=crop' },
    { keywords: ['pad thai', 'thai'], url: 'https://images.unsplash.com/photo-1559314809-0d155014e29e?w=400&h=300&fit=crop' },
    { keywords: ['wok', 'gebratene'], url: 'https://images.unsplash.com/photo-1585032226651-759b368d7246?w=400&h=300&fit=crop' },

    // Suppen & Eintöpfe
    { keywords: ['suppe', 'soup'], url: 'https://images.unsplash.com/photo-1547592166-23ac45744acd?w=400&h=300&fit=crop' },
    { keywords: ['eintopf', 'stew'], url: 'https://images.unsplash.com/photo-1547592166-23ac45744acd?w=400&h=300&fit=crop' },

    // Beilagen
    { keywords: ['pommes', 'fries'], url: 'https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=400&h=300&fit=crop' },
    { keywords: ['reis', 'rice'], url: 'https://images.unsplash.com/photo-1516684732162-798a0062be99?w=400&h=300&fit=crop' },
    { keywords: ['kartoffel'], url: 'https://images.unsplash.com/photo-1518013431117-eb1465fa5752?w=400&h=300&fit=crop' },
  ];

  // Suche nach passenden Keywords
  for (const { keywords, url } of keywordImages) {
    if (keywords.some(keyword => nameLower.includes(keyword))) {
      return url;
    }
  }

  // Fallback zu generischem Food-Bild
  return getDefaultFoodImage();
};

/**
 * Gibt ein zufälliges generisches Food-Bild zurück
 * @returns {string} - Unsplash Bild-URL
 */
const getDefaultFoodImage = () => {
  const defaultImages = [
    // Ursprüngliche Bilder
    'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&h=300&fit=crop', // Generisches Essen
    'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=400&h=300&fit=crop', // Burger & Pommes
    'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&h=300&fit=crop', // Bowl
    'https://images.unsplash.com/photo-1606787366850-de6330128bfc?w=400&h=300&fit=crop', // Teller mit Essen

    // Neue Kantinen-typische Bilder
    'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400&h=300&fit=crop', // Gemischtes Gemüse mit Fleisch
    'https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=400&h=300&fit=crop', // Pfannengericht - typisch Kantine
    'https://images.unsplash.com/photo-1563379926898-05f4575a45d8?w=400&h=300&fit=crop', // Reisplatte mit Beilagen
    'https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=400&h=300&fit=crop', // Cafeteria/Kantinen-Setting
    'https://images.unsplash.com/photo-1455619452474-d2be8b1e70cd?w=400&h=300&fit=crop', // Diverses warmes Essen
    'https://images.unsplash.com/photo-1476224203421-9ac39bcb3327?w=400&h=300&fit=crop', // Hauptgericht mit Gemüse
    'https://images.unsplash.com/photo-1482049016688-2d3e1b311543?w=400&h=300&fit=crop', // Verschiedene Gerichte - Buffet-Style
    'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&h=300&fit=crop', // Pizza - beliebtes Kantinen-Essen
    'https://images.unsplash.com/photo-1598514982901-ae62764ae75e?w=400&h=300&fit=crop', // Warmes Mittagessen auf Teller
    'https://images.unsplash.com/photo-1574484284002-952d92456975?w=400&h=300&fit=crop', // Hausmannskost-Style
    'https://images.unsplash.com/photo-1529042410759-befb1204b468?w=400&h=300&fit=crop', // Gemischter Teller mit Proteinen
  ];

  // Gib zufälliges Bild zurück für Abwechslung bei unbekannten Gerichten
  return defaultImages[Math.floor(Math.random() * defaultImages.length)];
};
