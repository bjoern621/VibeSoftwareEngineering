/**
 * Utility-Funktionen für Bild-URLs
 */

/**
 * Gibt die passende Bild-URL für ein Gericht basierend auf dem Namen zurück
 * 
 * @param {string} mealName - Name des Gerichts
 * @returns {string} - Unsplash Bild-URL
 */
export const getMealImage = (mealName) => {
  const imageMap = {
    'Spaghetti Bolognese': 'https://images.unsplash.com/photo-1612874742237-6526221588e3?w=400&h=300&fit=crop',
    'Veganer Burger': 'https://images.unsplash.com/photo-1520072959219-c595dc870360?w=400&h=300&fit=crop',
    'Caesar Salad': 'https://images.unsplash.com/photo-1546793665-c74683f339c1?w=400&h=300&fit=crop',
    'Currywurst mit Pommes': 'https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=400&h=300&fit=crop',
    'Gemüse-Lasagne': 'https://images.unsplash.com/photo-1574894709920-11b28e7367e3?w=400&h=300&fit=crop',
    'Gegrillter Lachs mit Reis': 'https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=400&h=300&fit=crop',
    'Hähnchen Shawarma': 'https://images.unsplash.com/photo-1529006557810-274b9b2fc783?w=400&h=300&fit=crop',
    'Quinoa Bowl': 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&h=300&fit=crop'
  };
  
  // Fallback zu einem generischen Essen-Bild
  return imageMap[mealName] || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&h=300&fit=crop';
};
