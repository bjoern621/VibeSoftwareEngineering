/**
 * API Service Layer
 * Zentralisiert alle API-Calls und ermöglicht einfaches Umschalten zwischen Mock-Daten und echtem Backend
 */

import {
  mockMeals,
  mockMealPlans,
  mockOrders,
  mockDashboardData,
  mockInventory,
  mockStaff,
  formatDate,
  generateQRCode,
  generateOrderId
} from './mockData';

import { USE_MOCK_DATA, MOCK_DELAY, API_BASE_URL } from '../utils/constants';

// Simulierte Netzwerk-Verzögerung für realistisches Verhalten
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

// ============================================================================
// MEALS API
// ============================================================================

export const mealsAPI = {
  // Alle Gerichte abrufen
  getAll: async () => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      return mockMeals.filter(m => !m.deleted);
    }
    const response = await fetch(`${API_BASE_URL}/meals`);
    if (!response.ok) throw new Error('Fehler beim Laden der Gerichte');
    return response.json();
  },

  // Alle aktiven Gerichte abrufen (ohne gelöschte)
  getAllActive: async () => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      return mockMeals.filter(m => !m.deleted);
    }
    // Backend gibt standardmäßig nur aktive Gerichte zurück
    const response = await fetch(`${API_BASE_URL}/meals`);
    if (!response.ok) throw new Error('Fehler beim Laden der Gerichte');
    return response.json();
  },

  // Einzelnes Gericht abrufen (inkl. gelöschte für historische Daten)
  getById: async (id) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const meal = mockMeals.find(m => m.id === parseInt(id));
      if (!meal) throw new Error('Gericht nicht gefunden');
      return meal;
    }
    const response = await fetch(`${API_BASE_URL}/meals/${id}`);
    if (!response.ok) {
      if (response.status === 404) throw new Error('Gericht nicht gefunden');
      throw new Error('Fehler beim Laden des Gerichts');
    }
    return response.json();
  },

  // Nur aktives Gericht abrufen (ohne gelöschte)
  getByIdActive: async (id) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const meal = mockMeals.find(m => m.id === parseInt(id) && !m.deleted);
      if (!meal) throw new Error('Gericht nicht gefunden oder wurde gelöscht');
      return meal;
    }
    // Backend prüft automatisch auf deleted=false bei GET /api/meals
    const response = await fetch(`${API_BASE_URL}/meals/${id}`);
    if (!response.ok) {
      if (response.status === 404) throw new Error('Gericht nicht gefunden');
      throw new Error('Fehler beim Laden des Gerichts');
    }
    const meal = await response.json();
    if (meal.deleted) throw new Error('Gericht wurde gelöscht');
    return meal;
  },

  // Neues Gericht erstellen
  create: async (mealData) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const newMeal = {
        ...mealData,
        id: Math.max(...mockMeals.map(m => m.id)) + 1,
        deleted: false,
        deletedAt: null,
        allergens: mealData.allergens || []
      };
      mockMeals.push(newMeal);
      return newMeal;
    }
    const response = await fetch(`${API_BASE_URL}/meals`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(mealData)
    });
    if (!response.ok) {
      if (response.status === 400) {
        const error = await response.json();
        throw new Error(error.message || 'Ungültige Eingabedaten');
      }
      throw new Error('Fehler beim Erstellen des Gerichts');
    }
    return response.json();
  },

  // Gericht aktualisieren
  update: async (id, mealData) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const index = mockMeals.findIndex(m => m.id === parseInt(id));
      if (index === -1) throw new Error('Gericht nicht gefunden');
      mockMeals[index] = { ...mockMeals[index], ...mealData };
      return mockMeals[index];
    }
    const response = await fetch(`${API_BASE_URL}/meals/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(mealData)
    });
    if (!response.ok) {
      if (response.status === 404) throw new Error('Gericht nicht gefunden');
      if (response.status === 400) {
        const error = await response.json();
        throw new Error(error.message || 'Ungültige Eingabedaten');
      }
      throw new Error('Fehler beim Aktualisieren des Gerichts');
    }
    return response.json();
  },

  // Gericht löschen (Soft Delete)
  delete: async (id) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const meal = mockMeals.find(m => m.id === parseInt(id));
      if (!meal) throw new Error('Gericht nicht gefunden');
      meal.deleted = true;
      meal.deletedAt = new Date().toISOString();
      return { success: true };
    }
    const response = await fetch(`${API_BASE_URL}/meals/${id}`, {
      method: 'DELETE'
    });
    if (!response.ok) {
      if (response.status === 404) throw new Error('Gericht nicht gefunden');
      throw new Error('Fehler beim Löschen des Gerichts');
    }
    // Backend gibt 204 No Content zurück
    return { success: true };
  }
};

// ============================================================================
// MEAL PLANS API
// ============================================================================

export const mealPlansAPI = {
  // Speiseplan für bestimmtes Datum abrufen
  getByDate: async (date) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      return mockMealPlans.filter(mp => mp.date === date);
    }
    // Backend erwartet startDate und endDate
    const response = await fetch(`${API_BASE_URL}/meal-plans?startDate=${date}&endDate=${date}`);
    if (!response.ok) throw new Error('Fehler beim Laden des Speiseplans');
    const data = await response.json();
    // Backend gibt Array mit {date, meals: [{meal, stock}]} zurück
    // Wir müssen es ins alte Format konvertieren für Kompatibilität
    if (data.length === 0) return [];
    return data[0].meals.map(item => ({
      mealId: item.meal.id,
      date: date,
      stock: item.stock,
      meal: item.meal
    }));
  },

  // Speiseplan für Zeitraum abrufen (neue Methode für Backend-Format)
  getByDateRange: async (startDate, endDate) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const dates = [];
      let current = new Date(startDate);
      const end = new Date(endDate);
      while (current <= end) {
        const dateStr = formatDate(current);
        const mealsForDate = mockMealPlans.filter(mp => mp.date === dateStr);
        if (mealsForDate.length > 0) {
          dates.push({
            date: dateStr,
            meals: mealsForDate
          });
        }
        current.setDate(current.getDate() + 1);
      }
      return dates;
    }
    const response = await fetch(`${API_BASE_URL}/meal-plans?startDate=${startDate}&endDate=${endDate}`);
    if (!response.ok) throw new Error('Fehler beim Laden des Speiseplans');
    return response.json();
  },

  // Gericht zum Speiseplan hinzufügen oder aktualisieren
  addOrUpdate: async (planData) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const meal = mockMeals.find(m => m.id === planData.mealId);
      if (!meal) throw new Error('Gericht nicht gefunden');
      
      const existingIndex = mockMealPlans.findIndex(
        mp => mp.mealId === planData.mealId && mp.date === planData.date
      );
      
      if (existingIndex >= 0) {
        mockMealPlans[existingIndex].stock = planData.stock;
        return mockMealPlans[existingIndex];
      } else {
        const newPlan = {
          mealId: planData.mealId,
          date: planData.date,
          stock: planData.stock,
          meal: meal
        };
        mockMealPlans.push(newPlan);
        return newPlan;
      }
    }
    // Backend nutzt PUT für create/update
    const response = await fetch(`${API_BASE_URL}/meal-plans`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(planData)
    });
    if (!response.ok) {
      if (response.status === 404) throw new Error('Gericht nicht gefunden');
      if (response.status === 400) {
        const error = await response.json();
        throw new Error(error.message || 'Ungültige Eingabedaten');
      }
      throw new Error('Fehler beim Hinzufügen zum Speiseplan');
    }
    return response.json();
  },

  // Legacy: Erstellen (nutzt jetzt addOrUpdate)
  create: async (planData) => {
    return mealPlansAPI.addOrUpdate(planData);
  },

  // Legacy: Aktualisieren (nutzt jetzt addOrUpdate)
  update: async (mealId, planData) => {
    return mealPlansAPI.addOrUpdate({ mealId, ...planData });
  },

  // Vom Speiseplan entfernen
  delete: async (mealId, date) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const index = mockMealPlans.findIndex(
        mp => mp.mealId === parseInt(mealId) && mp.date === date
      );
      if (index === -1) throw new Error('Speiseplan-Eintrag nicht gefunden');
      mockMealPlans.splice(index, 1);
      return { success: true };
    }
    const response = await fetch(`${API_BASE_URL}/meal-plans?mealId=${mealId}&date=${date}`, {
      method: 'DELETE'
    });
    if (!response.ok) {
      if (response.status === 404) throw new Error('Speiseplan-Eintrag nicht gefunden');
      throw new Error('Fehler beim Entfernen vom Speiseplan');
    }
    // Backend gibt 204 No Content zurück
    return { success: true };
  }
};

// ============================================================================
// ORDERS API
// ============================================================================

export const ordersAPI = {
  // Alle Bestellungen abrufen (mit Filtern)
  getAll: async (filters = {}) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      let filtered = [...mockOrders];
      
      if (filters.date) {
        filtered = filtered.filter(o => o.orderDate === filters.date);
      }
      if (filters.status === 'pending') {
        filtered = filtered.filter(o => !o.paid);
      } else if (filters.status === 'paid') {
        filtered = filtered.filter(o => o.paid && !o.collected);
      } else if (filters.status === 'collected') {
        filtered = filtered.filter(o => o.collected);
      }
      
      return filtered;
    }
    // Backend unterstützt: startDate, endDate, paid, collected
    const params = new URLSearchParams();
    if (filters.startDate) params.append('startDate', filters.startDate);
    if (filters.endDate) params.append('endDate', filters.endDate);
    if (filters.paid !== undefined) params.append('paid', filters.paid);
    if (filters.collected !== undefined) params.append('collected', filters.collected);
    
    const response = await fetch(`${API_BASE_URL}/orders?${params}`);
    if (!response.ok) throw new Error('Fehler beim Laden der Bestellungen');
    return response.json();
  },

  // Neue Bestellung erstellen
  create: async (orderData) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const meal = mockMeals.find(m => m.id === orderData.mealId);
      if (!meal) throw new Error('Gericht nicht gefunden');
      
      // Bestand prüfen
      const plan = mockMealPlans.find(
        mp => mp.mealId === orderData.mealId && mp.date === orderData.pickupDate
      );
      if (!plan || plan.stock < 1) {
        throw new Error('Nicht genügend Bestand verfügbar');
      }
      
      // Bestand reduzieren
      plan.stock -= 1;
      
      const newOrder = {
        id: generateOrderId(),
        mealId: orderData.mealId,
        meal: meal,
        orderDate: new Date().toISOString(),
        pickupDate: orderData.pickupDate,
        paid: false,
        paidAt: null,
        paymentMethod: null,
        paymentTransactionId: null,
        qrCode: null, // QR-Code erst nach Bezahlung
        collected: false,
        collectedAt: null
      };
      mockOrders.push(newOrder);
      return newOrder;
    }
    // Backend erwartet: { mealId, pickupDate }
    const response = await fetch(`${API_BASE_URL}/orders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        mealId: orderData.mealId,
        pickupDate: orderData.pickupDate
      })
    });
    if (!response.ok) {
      if (response.status === 400) {
        const error = await response.json();
        throw new Error(error.message || 'Ungültige Eingabedaten oder nicht genügend Bestand');
      }
      if (response.status === 404) throw new Error('Gericht nicht im Speiseplan gefunden');
      throw new Error('Fehler beim Erstellen der Bestellung');
    }
    return response.json();
  },

  // Bestellung bezahlen (EASYPAY-Integration)
  pay: async (orderId, paymentData) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const order = mockOrders.find(o => o.id === parseInt(orderId));
      if (!order) throw new Error('Bestellung nicht gefunden');
      if (order.paid) throw new Error('Bestellung wurde bereits bezahlt');
      
      order.paid = true;
      order.paidAt = new Date().toISOString();
      order.paymentMethod = paymentData.paymentMethod;
      order.paymentTransactionId = paymentData.paymentTransactionId;
      order.qrCode = generateQRCode();
      return order;
    }
    // Backend erwartet: { paymentMethod, paymentTransactionId }
    const response = await fetch(`${API_BASE_URL}/orders/${orderId}/pay`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(paymentData)
    });
    if (!response.ok) {
      if (response.status === 404) throw new Error('Bestellung nicht gefunden');
      if (response.status === 400) {
        const error = await response.json();
        throw new Error(error.message || 'Bestellung wurde bereits bezahlt oder ungültige Zahlungsmethode');
      }
      throw new Error('Fehler beim Bezahlen der Bestellung');
    }
    return response.json();
  },

  // Bestellung per QR-Code validieren (Mensa-Mitarbeiter App)
  validateQRCode: async (qrCode) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const order = mockOrders.find(o => o.qrCode === qrCode);
      if (!order) throw new Error('QR-Code ungültig');
      if (!order.paid) throw new Error('Bestellung wurde noch nicht bezahlt');
      
      const alreadyCollected = order.collected;
      if (!alreadyCollected) {
        order.collected = true;
        order.collectedAt = new Date().toISOString();
      }
      
      return {
        alreadyCollected,
        collectedAt: order.collectedAt,
        orderId: order.id,
        orderDate: order.orderDate,
        pickupDate: order.pickupDate,
        meal: order.meal
      };
    }
    // Backend erwartet: { qrCode }
    const response = await fetch(`${API_BASE_URL}/orders/validate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ qrCode })
    });
    if (!response.ok) {
      if (response.status === 404) throw new Error('QR-Code ungültig oder Bestellung nicht bezahlt');
      if (response.status === 400) throw new Error('QR-Code-Format ungültig');
      throw new Error('Fehler bei der QR-Code-Validierung');
    }
    return response.json();
  },

  // Legacy: Zahlung markieren (nutzt jetzt pay)
  markAsPaid: async (id, paymentData = {}) => {
    return ordersAPI.pay(id, paymentData);
  },

  // Legacy: Abholung markieren (nutzt jetzt validateQRCode)
  markAsCollected: async (id) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const order = mockOrders.find(o => o.id === parseInt(id));
      if (!order) throw new Error('Bestellung nicht gefunden');
      if (!order.paid) throw new Error('Bestellung muss erst bezahlt werden');
      order.collected = true;
      order.collectedAt = new Date().toISOString();
      return order;
    }
    // Für Legacy-Unterstützung - nutze validateQRCode mit dem QR-Code der Bestellung
    const orders = await ordersAPI.getAll();
    const order = orders.find(o => o.id === parseInt(id));
    if (!order || !order.qrCode) throw new Error('Bestellung nicht gefunden');
    return ordersAPI.validateQRCode(order.qrCode);
  },

  // Bestellung löschen
  delete: async (orderId) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const index = mockOrders.findIndex(o => o.id === parseInt(orderId));
      if (index === -1) throw new Error('Bestellung nicht gefunden');
      
      // Bestand zurückgeben falls nicht bezahlt
      const order = mockOrders[index];
      if (!order.paid) {
        const plan = mockMealPlans.find(
          mp => mp.mealId === order.mealId && mp.date === order.pickupDate
        );
        if (plan) {
          plan.stock += 1;
        }
      }
      
      mockOrders.splice(index, 1);
      return;
    }
    // Backend DELETE-Endpoint
    const response = await fetch(`${API_BASE_URL}/orders/${orderId}`, {
      method: 'DELETE'
    });
    if (!response.ok) {
      if (response.status === 404) throw new Error('Bestellung nicht gefunden');
      throw new Error('Fehler beim Löschen der Bestellung');
    }
  }
};

// ============================================================================
// DASHBOARD API
// ============================================================================

export const dashboardAPI = {
  getData: async (startDate, endDate) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      // In einer echten Implementierung würden hier die Bestellungen
      // im Zeitraum gefiltert und aggregiert
      return mockDashboardData;
    }
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    
    const response = await fetch(`${API_BASE_URL}/dashboard?${params}`);
    if (!response.ok) throw new Error('Fehler beim Laden der Dashboard-Daten');
    return response.json();
  }
};

// ============================================================================
// INVENTORY API
// ============================================================================

export const inventoryAPI = {
  // Alle Zutaten abrufen
  getAll: async (lowStockOnly = false) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      if (lowStockOnly) {
        return mockInventory.filter(i => i.stockQuantity < i.minStockLevel);
      }
      return mockInventory;
    }
    const params = new URLSearchParams();
    if (lowStockOnly) params.append('lowStock', 'true');
    
    const response = await fetch(`${API_BASE_URL}/inventory?${params}`);
    if (!response.ok) throw new Error('Fehler beim Laden des Lagerbestands');
    return response.json();
  },

  // Neue Zutat erstellen
  create: async (ingredientData) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const newIngredient = {
        ...ingredientData,
        id: Math.max(...mockInventory.map(i => i.id)) + 1,
        needsReorder: ingredientData.stockQuantity < ingredientData.minStockLevel
      };
      mockInventory.push(newIngredient);
      return newIngredient;
    }
    const response = await fetch(`${API_BASE_URL}/inventory`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(ingredientData)
    });
    if (!response.ok) {
      if (response.status === 400) {
        const error = await response.json();
        throw new Error(error.message || 'Ungültige Eingabedaten');
      }
      throw new Error('Fehler beim Erstellen der Zutat');
    }
    return response.json();
  },

  // Zutat aktualisieren
  update: async (id, ingredientData) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const index = mockInventory.findIndex(i => i.id === parseInt(id));
      if (index === -1) throw new Error('Zutat nicht gefunden');
      mockInventory[index] = { 
        ...mockInventory[index], 
        ...ingredientData,
        needsReorder: (ingredientData.stockQuantity || mockInventory[index].stockQuantity) < mockInventory[index].minStockLevel
      };
      return mockInventory[index];
    }
    const response = await fetch(`${API_BASE_URL}/inventory/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(ingredientData)
    });
    if (!response.ok) {
      if (response.status === 404) throw new Error('Zutat nicht gefunden');
      if (response.status === 400) {
        const error = await response.json();
        throw new Error(error.message || 'Ungültige Eingabedaten');
      }
      throw new Error('Fehler beim Aktualisieren der Zutat');
    }
    return response.json();
  },

  // Automatische Nachbestellung aller Zutaten unter Mindestbestand (FOODSUPPLY)
  reorderAll: async () => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const itemsToReorder = mockInventory.filter(i => i.stockQuantity < i.minStockLevel);
      const reorderedItems = itemsToReorder.map(item => {
        const reorderQuantity = item.minStockLevel * 3; // Dreifacher Mindestbestand
        item.stockQuantity += reorderQuantity;
        item.needsReorder = false;
        return {
          ingredientId: item.id,
          ingredientName: item.name,
          currentStock: item.stockQuantity - reorderQuantity,
          minStockLevel: item.minStockLevel,
          reorderQuantity,
          supplierId: item.supplierId,
          foodsupplyOrderId: `FOODSUPPLY-ORD-${Math.floor(Math.random() * 10000)}`
        };
      });
      const totalOrderValue = reorderedItems.reduce((sum, item) => 
        sum + (item.reorderQuantity * (mockInventory.find(i => i.id === item.ingredientId)?.pricePerUnit || 0)), 0
      );
      return {
        reorderedItems,
        totalOrderValue: parseFloat(totalOrderValue.toFixed(2))
      };
    }
    const response = await fetch(`${API_BASE_URL}/inventory/reorder`, {
      method: 'POST'
    });
    if (!response.ok) {
      if (response.status === 500) throw new Error('Verbindung zu FOODSUPPLY fehlgeschlagen');
      throw new Error('Fehler bei der automatischen Nachbestellung');
    }
    return response.json();
  },

  // Lagerbestand nach Gerichtszubereitung aktualisieren
  consumeForMeal: async (mealId, quantity) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const meal = mockMeals.find(m => m.id === parseInt(mealId));
      if (!meal) throw new Error('Gericht nicht gefunden');
      
      // Simuliere Zutaten-Verbrauch (mockup)
      const ingredientsConsumed = [];
      const randomIngredients = mockInventory.slice(0, 3); // Nehme erste 3 Zutaten als Beispiel
      
      randomIngredients.forEach(ingredient => {
        const quantityUsed = (Math.random() * 0.5 + 0.1) * quantity; // Zufällige Menge
        ingredient.stockQuantity -= quantityUsed;
        ingredient.needsReorder = ingredient.stockQuantity < ingredient.minStockLevel;
        
        ingredientsConsumed.push({
          ingredientName: ingredient.name,
          quantityUsed: parseFloat(quantityUsed.toFixed(2)),
          unit: ingredient.unit,
          remainingStock: parseFloat(ingredient.stockQuantity.toFixed(2)),
          needsReorder: ingredient.needsReorder
        });
      });
      
      return {
        mealName: meal.name,
        quantityPrepared: quantity,
        ingredientsConsumed
      };
    }
    const response = await fetch(`${API_BASE_URL}/inventory/consume`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ mealId, quantity })
    });
    if (!response.ok) {
      if (response.status === 404) throw new Error('Gericht oder Zutat nicht gefunden');
      if (response.status === 400) throw new Error('Nicht genügend Bestand');
      throw new Error('Fehler beim Aktualisieren des Lagerbestands');
    }
    return response.json();
  },

  // Legacy: Einzelne Nachbestellung (nutzt jetzt reorderAll)
  reorder: async (itemId) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const item = mockInventory.find(i => i.id === parseInt(itemId));
      if (!item) throw new Error('Artikel nicht gefunden');
      // Simuliere Nachbestellung
      const reorderQuantity = item.minStockLevel * 2;
      item.stockQuantity += reorderQuantity;
      item.needsReorder = false;
      item.lastOrdered = formatDate(new Date());
      return item;
    }
    // Backend unterstützt nur Sammel-Nachbestellung
    return inventoryAPI.reorderAll();
  }
};

// ============================================================================
// STAFF API
// ============================================================================

export const staffAPI = {
  // Alle Mitarbeiter abrufen
  getAll: async (filters = {}) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      let filtered = [...mockStaff];
      if (filters.role) {
        filtered = filtered.filter(s => s.role === filters.role);
      }
      if (filters.available !== undefined) {
        filtered = filtered.filter(s => s.isAvailable === filters.available);
      }
      return filtered;
    }
    const params = new URLSearchParams();
    if (filters.role) params.append('role', filters.role);
    if (filters.available !== undefined) params.append('available', filters.available);
    
    const response = await fetch(`${API_BASE_URL}/staff?${params}`);
    if (!response.ok) throw new Error('Fehler beim Laden der Personal-Daten');
    return response.json();
  },

  // Neuen Mitarbeiter erstellen
  create: async (staffData) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const newStaff = {
        ...staffData,
        id: Math.max(...mockStaff.map(s => s.id)) + 1,
        staffmanId: `STAFFMAN-EMP-${Math.floor(Math.random() * 10000)}`,
        isAvailable: true
      };
      mockStaff.push(newStaff);
      return newStaff;
    }
    const response = await fetch(`${API_BASE_URL}/staff`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(staffData)
    });
    if (!response.ok) {
      if (response.status === 400) {
        const error = await response.json();
        throw new Error(error.message || 'Ungültige Eingabedaten');
      }
      if (response.status === 500) throw new Error('STAFFMAN-Synchronisation fehlgeschlagen');
      throw new Error('Fehler beim Erstellen des Mitarbeiters');
    }
    return response.json();
  },

  // Arbeitszeiten erfassen
  recordWorkingHours: async (staffId, workingHoursData) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const staff = mockStaff.find(s => s.id === parseInt(staffId));
      if (!staff) throw new Error('Mitarbeiter nicht gefunden');
      
      // Berechne Arbeitsstunden
      const start = new Date(`2000-01-01T${workingHoursData.startTime}`);
      const end = new Date(`2000-01-01T${workingHoursData.endTime}`);
      const hoursWorked = (end - start) / (1000 * 60 * 60);
      
      const newWorkingHours = {
        id: Math.floor(Math.random() * 10000),
        staffId: parseInt(staffId),
        date: workingHoursData.date,
        startTime: workingHoursData.startTime,
        endTime: workingHoursData.endTime,
        hoursWorked: parseFloat(hoursWorked.toFixed(1)),
        syncedToStaffman: true
      };
      
      return newWorkingHours;
    }
    const response = await fetch(`${API_BASE_URL}/staff/${staffId}/working-hours`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(workingHoursData)
    });
    if (!response.ok) {
      if (response.status === 404) throw new Error('Mitarbeiter nicht gefunden');
      if (response.status === 400) {
        const error = await response.json();
        throw new Error(error.message || 'Ungültige Zeitangaben');
      }
      if (response.status === 500) throw new Error('STAFFMAN-Synchronisation fehlgeschlagen');
      throw new Error('Fehler beim Erfassen der Arbeitszeiten');
    }
    return response.json();
  },

  // Arbeitszeiten für Zeitraum abrufen
  getWorkingHours: async (startDate, endDate, staffId = null) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      // Simuliere Arbeitszeiten-Daten
      const mockWorkingHours = mockStaff.map(staff => ({
        id: Math.floor(Math.random() * 10000),
        staff: staff,
        date: startDate,
        startTime: '08:00:00',
        endTime: '16:00:00',
        hoursWorked: 8.0,
        syncedToStaffman: true
      }));
      
      if (staffId) {
        return mockWorkingHours.filter(wh => wh.staff.id === parseInt(staffId));
      }
      return mockWorkingHours;
    }
    const params = new URLSearchParams({ startDate, endDate });
    if (staffId) params.append('staffId', staffId);
    
    const response = await fetch(`${API_BASE_URL}/staff/working-hours?${params}`);
    if (!response.ok) throw new Error('Fehler beim Laden der Arbeitszeiten');
    return response.json();
  },

  // Einsatzplanung basierend auf erwarteter Besucherzahl
  getScheduleRecommendation: async (date) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      const cooks = mockStaff.filter(s => s.role === 'COOK' && s.isAvailable);
      const service = mockStaff.filter(s => s.role === 'SERVICE' && s.isAvailable);
      
      return {
        date,
        expectedVisitors: Math.floor(Math.random() * 200) + 250,
        plannedMeals: Math.floor(Math.random() * 3) + 3,
        recommendedStaff: {
          cooks: Math.min(3, cooks.length),
          service: Math.min(4, service.length),
          total: Math.min(7, cooks.length + service.length)
        },
        availableStaff: {
          cooks,
          service
        }
      };
    }
    const response = await fetch(`${API_BASE_URL}/staff/schedule-recommendation?date=${date}`);
    if (!response.ok) throw new Error('Fehler beim Abrufen der Einsatzplanung');
    return response.json();
  },

  // Legacy: STAFFMAN-Synchronisation
  sync: async () => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      // Simuliere STAFFMAN-Synchronisation
      mockStaff.forEach(s => {
        s.lastSync = new Date().toISOString().replace('T', ' ').substring(0, 16);
      });
      return { success: true, synced: mockStaff.length };
    }
    // Backend synchronisiert automatisch bei create und recordWorkingHours
    // Diese Funktion ist für Legacy-Kompatibilität
    return { success: true, message: 'Synchronisation erfolgt automatisch' };
  }
};

// ============================================================================
// FORECASTS API
// ============================================================================

export const forecastsAPI = {
  // Wareneinsatz-Prognose für Zeitraum
  getDemandForecast: async (startDate, endDate) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      // Mock-Prognose-Daten
      return {
        forecastPeriod: {
          startDate,
          endDate
        },
        mealForecasts: [
          {
            mealName: 'Spaghetti Bolognese',
            averageDailyDemand: 18.5,
            recommendedStock: 95,
            confidenceLevel: 0.85
          },
          {
            mealName: 'Veganer Burger',
            averageDailyDemand: 15.2,
            recommendedStock: 80,
            confidenceLevel: 0.78
          }
        ],
        ingredientForecasts: [
          {
            ingredientName: 'Tomaten',
            estimatedConsumption: 45.5,
            currentStock: 50.0,
            recommendedPurchase: 0.0
          }
        ]
      };
    }
    const params = new URLSearchParams();
    params.append('startDate', startDate);
    params.append('endDate', endDate);
    
    const response = await fetch(`${API_BASE_URL}/forecasts/demand?${params}`);
    if (!response.ok) throw new Error('Fehler beim Laden der Prognose-Daten');
    return response.json();
  },

  // Nachhaltigkeit-Bericht
  getSustainabilityReport: async (month, year) => {
    if (USE_MOCK_DATA) {
      await delay(MOCK_DELAY);
      return {
        period: `${month}/${year}`,
        wasteReduction: {
          totalMealsPrepared: 2450,
          totalMealsSold: 2380,
          wastedMeals: 70,
          wastePercentage: 2.86,
          previousMonthWastePercentage: 5.2,
          improvement: 2.34
        },
        costSavings: {
          savedCosts: 224.0,
          potentialSavings: 156.0
        },
        topWastedMeals: [
          {
            mealName: 'Caesar Salad',
            wastedPortions: 25,
            costOfWaste: 75.0
          }
        ],
        recommendations: [
          'Reduktion der Caesar Salad Portionen um 10%',
          'Erhöhung der Veganer Burger Produktion um 5%'
        ]
      };
    }
    const params = new URLSearchParams();
    params.append('month', month);
    params.append('year', year);
    
    const response = await fetch(`${API_BASE_URL}/reports/sustainability?${params}`);
    if (!response.ok) throw new Error('Fehler beim Laden des Nachhaltigkeitsberichts');
    return response.json();
  }
};

// Export für einfachen Zugriff
const api = {
  meals: mealsAPI,
  mealPlans: mealPlansAPI,
  orders: ordersAPI,
  dashboard: dashboardAPI,
  inventory: inventoryAPI,
  staff: staffAPI,
  forecasts: forecastsAPI,
  // Flag zum Umschalten zwischen Mock und echtem Backend
  useMockData: USE_MOCK_DATA
};

export default api;
