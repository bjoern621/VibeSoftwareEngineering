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
    generateOrderId,
} from "./mockData";

import { USE_MOCK_DATA, MOCK_DELAY } from "../utils/constants";

// Simulierte Netzwerk-Verzögerung für realistisches Verhalten
const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

// ============================================================================
// MEALS API
// ============================================================================

export const mealsAPI = {
    // Alle Gerichte abrufen
    getAll: async () => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            return mockMeals.filter((m) => !m.deleted);
        }
        const response = await fetch("/api/meals");
        if (!response.ok) throw new Error("Fehler beim Laden der Gerichte");
        return response.json();
    },

    // Alle aktiven Gerichte abrufen (ohne gelöschte)
    getAllActive: async () => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            return mockMeals.filter((m) => !m.deleted);
        }
        const response = await fetch("/api/meals?active=true");
        if (!response.ok) throw new Error("Fehler beim Laden der Gerichte");
        return response.json();
    },

    // Einzelnes Gericht abrufen (inkl. gelöschte für historische Daten)
    getById: async (id) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            const meal = mockMeals.find((m) => m.id === parseInt(id));
            if (!meal) throw new Error("Gericht nicht gefunden");
            return meal;
        }
        const response = await fetch(`/api/meals/${id}`);
        if (!response.ok) throw new Error("Gericht nicht gefunden");
        return response.json();
    },

    // Nur aktives Gericht abrufen (ohne gelöschte)
    getByIdActive: async (id) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            const meal = mockMeals.find(
                (m) => m.id === parseInt(id) && !m.deleted
            );
            if (!meal)
                throw new Error("Gericht nicht gefunden oder wurde gelöscht");
            return meal;
        }
        const response = await fetch(`/api/meals/${id}?active=true`);
        if (!response.ok) throw new Error("Gericht nicht gefunden");
        return response.json();
    },

    // Neues Gericht erstellen
    create: async (mealData) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            const newMeal = {
                ...mealData,
                id: Math.max(...mockMeals.map((m) => m.id)) + 1,
                deleted: false,
                deletedAt: null,
                allergens: mealData.allergens || [],
            };
            mockMeals.push(newMeal);
            return newMeal;
        }
        const response = await fetch("/api/meals", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(mealData),
        });
        if (!response.ok) throw new Error("Fehler beim Erstellen des Gerichts");
        return response.json();
    },

    // Gericht aktualisieren
    update: async (id, mealData) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            const index = mockMeals.findIndex((m) => m.id === parseInt(id));
            if (index === -1) throw new Error("Gericht nicht gefunden");
            mockMeals[index] = { ...mockMeals[index], ...mealData };
            return mockMeals[index];
        }
        const response = await fetch(`/api/meals/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(mealData),
        });
        if (!response.ok)
            throw new Error("Fehler beim Aktualisieren des Gerichts");
        return response.json();
    },

    // Gericht löschen (Soft Delete)
    delete: async (id) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            const meal = mockMeals.find((m) => m.id === parseInt(id));
            if (!meal) throw new Error("Gericht nicht gefunden");
            meal.deleted = true;
            meal.deletedAt = new Date().toISOString();
            return { success: true };
        }
        const response = await fetch(`/api/meals/${id}`, {
            method: "DELETE",
        });
        if (!response.ok) throw new Error("Fehler beim Löschen des Gerichts");
        return response.json();
    },
};

// ============================================================================
// MEAL PLANS API
// ============================================================================

export const mealPlansAPI = {
    // Speiseplan für bestimmtes Datum abrufen
    getByDate: async (date) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            return mockMealPlans.filter((mp) => mp.date === date);
        }
        const response = await fetch(`/api/mealplans?date=${date}`);
        if (!response.ok) throw new Error("Fehler beim Laden des Speiseplans");
        return response.json();
    },

    // Gericht zum Speiseplan hinzufügen
    create: async (planData) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            const meal = mockMeals.find((m) => m.id === planData.mealId);
            if (!meal) throw new Error("Gericht nicht gefunden");

            const newPlan = {
                mealId: planData.mealId,
                date: planData.date,
                stock: planData.stock,
                meal: meal,
            };
            mockMealPlans.push(newPlan);
            return newPlan;
        }
        const response = await fetch("/api/mealplans", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(planData),
        });
        if (!response.ok)
            throw new Error("Fehler beim Hinzufügen zum Speiseplan");
        return response.json();
    },

    // Bestand aktualisieren
    update: async (mealId, planData) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            const plan = mockMealPlans.find(
                (mp) =>
                    mp.mealId === parseInt(mealId) && mp.date === planData.date
            );
            if (!plan) throw new Error("Speiseplan-Eintrag nicht gefunden");
            plan.stock = planData.stock;
            return plan;
        }
        const response = await fetch(`/api/mealplans/${mealId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(planData),
        });
        if (!response.ok)
            throw new Error("Fehler beim Aktualisieren des Bestands");
        return response.json();
    },

    // Vom Speiseplan entfernen
    delete: async (mealId, date) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            const index = mockMealPlans.findIndex(
                (mp) => mp.mealId === parseInt(mealId) && mp.date === date
            );
            if (index === -1)
                throw new Error("Speiseplan-Eintrag nicht gefunden");
            mockMealPlans.splice(index, 1);
            return { success: true };
        }
        const response = await fetch(`/api/mealplans/${mealId}?date=${date}`, {
            method: "DELETE",
        });
        if (!response.ok)
            throw new Error("Fehler beim Entfernen vom Speiseplan");
        return response.json();
    },
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
                filtered = filtered.filter((o) => o.orderDate === filters.date);
            }
            if (filters.status === "pending") {
                filtered = filtered.filter((o) => !o.paid);
            } else if (filters.status === "paid") {
                filtered = filtered.filter((o) => o.paid && !o.collected);
            } else if (filters.status === "collected") {
                filtered = filtered.filter((o) => o.collected);
            }

            return filtered;
        }
        const params = new URLSearchParams(filters);
        const response = await fetch(`/api/orders?${params}`);
        if (!response.ok) throw new Error("Fehler beim Laden der Bestellungen");
        return response.json();
    },

    // Neue Bestellung erstellen
    create: async (orderData) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            const meal = mockMeals.find((m) => m.id === orderData.mealId);
            if (!meal) throw new Error("Gericht nicht gefunden");

            // Bestand prüfen
            const plan = mockMealPlans.find(
                (mp) =>
                    mp.mealId === orderData.mealId &&
                    mp.date === orderData.orderDate
            );
            if (!plan || plan.stock < orderData.quantity) {
                throw new Error("Nicht genügend Bestand verfügbar");
            }

            // Bestand reduzieren
            plan.stock -= orderData.quantity;

            const newOrder = {
                id: generateOrderId(),
                mealId: orderData.mealId,
                meal: meal,
                quantity: orderData.quantity,
                orderDate: orderData.orderDate,
                qrCode: generateQRCode(),
                paid: false,
                collected: false,
                orderTime: new Date().toISOString(),
            };
            mockOrders.push(newOrder);
            return newOrder;
        }
        const response = await fetch("/api/orders", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(orderData),
        });
        if (!response.ok)
            throw new Error("Fehler beim Erstellen der Bestellung");
        return response.json();
    },

    // Zahlung markieren
    markAsPaid: async (id) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            const order = mockOrders.find((o) => o.id === parseInt(id));
            if (!order) throw new Error("Bestellung nicht gefunden");
            order.paid = true;
            order.paidTime = new Date().toISOString();
            return order;
        }
        const response = await fetch(`/api/orders/${id}/pay`, {
            method: "PUT",
        });
        if (!response.ok) throw new Error("Fehler beim Markieren als bezahlt");
        return response.json();
    },

    // Abholung markieren
    markAsCollected: async (id) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            const order = mockOrders.find((o) => o.id === parseInt(id));
            if (!order) throw new Error("Bestellung nicht gefunden");
            if (!order.paid)
                throw new Error("Bestellung muss erst bezahlt werden");
            order.collected = true;
            order.collectedTime = new Date().toISOString();
            return order;
        }
        const response = await fetch(`/api/orders/${id}/collect`, {
            method: "PUT",
        });
        if (!response.ok) throw new Error("Fehler beim Markieren als abgeholt");
        return response.json();
    },

    // Bestellung löschen
    delete: async (id) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            const index = mockOrders.findIndex((o) => o.id === parseInt(id));
            if (index === -1) throw new Error("Bestellung nicht gefunden");
            mockOrders.splice(index, 1);
            return { success: true };
        }
        const response = await fetch(`/api/orders/${id}`, {
            method: "DELETE",
        });
        if (!response.ok) throw new Error("Fehler beim Löschen der Bestellung");
        return response.json();
    },
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
        const response = await fetch(
            `/api/dashboard?startDate=${startDate}&endDate=${endDate}`
        );
        if (!response.ok)
            throw new Error("Fehler beim Laden der Dashboard-Daten");
        return response.json();
    },
};

// ============================================================================
// INVENTORY API
// ============================================================================

export const inventoryAPI = {
    // Alle Zutaten abrufen (mit optionalem lowStock Filter)
    getAll: async (lowStock = false) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            if (lowStock) {
                return mockInventory.filter(
                    (i) => i.stockQuantity < i.minStockLevel
                );
            }
            return mockInventory;
        }
        const url = lowStock
            ? "/api/inventory?lowStock=true"
            : "/api/inventory";
        const response = await fetch(url);
        if (!response.ok)
            throw new Error("Fehler beim Laden des Lagerbestands");
        return response.json();
    },

    // Neue Zutat erstellen
    create: async (ingredientData) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            const newIngredient = {
                ...ingredientData,
                id: Math.max(...mockInventory.map((i) => i.id)) + 1,
                needsReorder:
                    ingredientData.stockQuantity < ingredientData.minStockLevel,
            };
            mockInventory.push(newIngredient);
            return newIngredient;
        }
        const response = await fetch("/api/inventory", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(ingredientData),
        });
        if (!response.ok) throw new Error("Fehler beim Erstellen der Zutat");
        return response.json();
    },

    // Zutat aktualisieren (z.B. Bestand)
    update: async (id, updateData) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            const item = mockInventory.find((i) => i.id === parseInt(id));
            if (!item) throw new Error("Zutat nicht gefunden");
            Object.assign(item, updateData);
            item.needsReorder = item.stockQuantity < item.minStockLevel;
            return item;
        }
        const response = await fetch(`/api/inventory/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(updateData),
        });
        if (!response.ok)
            throw new Error("Fehler beim Aktualisieren der Zutat");
        return response.json();
    },

    // Automatische Nachbestellung aller Zutaten unter Mindestbestand
    reorderAll: async () => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            const lowStockItems = mockInventory.filter(
                (i) => i.stockQuantity < i.minStockLevel
            );
            let totalOrderValue = 0;
            const reorderedItems = lowStockItems.map((item) => {
                const reorderQuantity = item.minStockLevel * 5;
                item.stockQuantity += reorderQuantity;
                item.needsReorder = false;
                const orderValue = reorderQuantity * item.pricePerUnit;
                totalOrderValue += orderValue;
                return {
                    ingredientId: item.id,
                    ingredientName: item.name,
                    currentStock: item.stockQuantity - reorderQuantity,
                    minStockLevel: item.minStockLevel,
                    reorderQuantity: reorderQuantity,
                    supplierId: item.supplierId,
                    foodsupplyOrderId: `FOODSUPPLY-ORD-${Date.now()}-${
                        item.id
                    }`,
                };
            });
            return { reorderedItems, totalOrderValue };
        }
        const response = await fetch("/api/inventory/reorder", {
            method: "POST",
        });
        if (!response.ok)
            throw new Error("Fehler bei der automatischen Nachbestellung");
        return response.json();
    },

    // Einzelne Zutat nachbestellen (Legacy-Support)
    reorder: async (itemId) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            const item = mockInventory.find((i) => i.id === parseInt(itemId));
            if (!item) throw new Error("Artikel nicht gefunden");
            item.stockQuantity += item.minStockLevel * 2;
            item.needsReorder = false;
            return item;
        }
        const response = await fetch(`/api/inventory/${itemId}/reorder`, {
            method: "POST",
        });
        if (!response.ok) throw new Error("Fehler bei der Nachbestellung");
        return response.json();
    },
};

// ============================================================================
// STAFF API
// ============================================================================

export const staffAPI = {
    // Alle Mitarbeiter abrufen (mit optionalen Filtern)
    getAll: async (filters = {}) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            let filtered = [...mockStaff];
            if (filters.role) {
                filtered = filtered.filter((s) => s.role === filters.role);
            }
            if (filters.available !== undefined) {
                filtered = filtered.filter(
                    (s) => s.isAvailable === filters.available
                );
            }
            return filtered;
        }
        const params = new URLSearchParams(filters);
        const response = await fetch(`/api/staff?${params}`);
        if (!response.ok)
            throw new Error("Fehler beim Laden der Personal-Daten");
        return response.json();
    },

    // Neuen Mitarbeiter erstellen
    create: async (staffData) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            const newStaff = {
                ...staffData,
                id: Math.max(...mockStaff.map((s) => s.id)) + 1,
                staffmanId: `STAFFMAN-EMP-${Date.now()}`,
                isAvailable: true,
            };
            mockStaff.push(newStaff);
            return newStaff;
        }
        const response = await fetch("/api/staff", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(staffData),
        });
        if (!response.ok)
            throw new Error("Fehler beim Erstellen des Mitarbeiters");
        return response.json();
    },

    // Arbeitszeiten erfassen
    recordWorkingHours: async (staffId, hoursData) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            const staff = mockStaff.find((s) => s.id === parseInt(staffId));
            if (!staff) throw new Error("Mitarbeiter nicht gefunden");

            const startTime = new Date(`2000-01-01T${hoursData.startTime}`);
            const endTime = new Date(`2000-01-01T${hoursData.endTime}`);
            const hoursWorked = (endTime - startTime) / (1000 * 60 * 60);

            return {
                id: Date.now(),
                staffId: staffId,
                staff: staff,
                date: hoursData.date,
                startTime: hoursData.startTime,
                endTime: hoursData.endTime,
                hoursWorked: hoursWorked,
                syncedToStaffman: true,
            };
        }
        const response = await fetch(`/api/staff/${staffId}/working-hours`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(hoursData),
        });
        if (!response.ok)
            throw new Error("Fehler beim Erfassen der Arbeitszeiten");
        return response.json();
    },

    // Arbeitszeiten für Zeitraum abrufen
    getWorkingHours: async (startDate, endDate, staffId = null) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            // Simulierte Arbeitszeiten
            return [];
        }
        const params = new URLSearchParams({ startDate, endDate });
        if (staffId) params.append("staffId", staffId);
        const response = await fetch(`/api/staff/working-hours?${params}`);
        if (!response.ok)
            throw new Error("Fehler beim Laden der Arbeitszeiten");
        return response.json();
    },

    // Einsatzplanung-Empfehlung
    getScheduleRecommendation: async (date) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            return {
                date: date,
                expectedVisitors: 350,
                plannedMeals: 5,
                recommendedStaff: {
                    cooks: 3,
                    service: 4,
                    total: 7,
                },
                availableStaff: {
                    cooks: mockStaff.filter(
                        (s) => s.role === "COOK" && s.isAvailable
                    ),
                    service: mockStaff.filter(
                        (s) => s.role === "SERVICE" && s.isAvailable
                    ),
                },
            };
        }
        const response = await fetch(
            `/api/staff/schedule-recommendation?date=${date}`
        );
        if (!response.ok)
            throw new Error("Fehler beim Abrufen der Einsatzplanung");
        return response.json();
    },

    // STAFFMAN-Synchronisation (Legacy)
    sync: async () => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            mockStaff.forEach((s) => {
                s.lastSync = new Date()
                    .toISOString()
                    .replace("T", " ")
                    .substring(0, 16);
            });
            return { success: true, synced: mockStaff.length };
        }
        const response = await fetch("/api/staff/sync", {
            method: "POST",
        });
        if (!response.ok) throw new Error("Fehler bei der Synchronisation");
        return response.json();
    },
};

// ============================================================================
// FORECASTS API (NEU)
// ============================================================================

export const forecastsAPI = {
    // Wareneinsatz-Prognose
    getDemandForecast: async (startDate, endDate) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            return {
                forecastPeriod: { startDate, endDate },
                mealForecasts: [
                    {
                        mealName: "Spaghetti Bolognese",
                        averageDailyDemand: 18.5,
                        recommendedStock: 95,
                        confidenceLevel: 0.85,
                    },
                    {
                        mealName: "Veganer Burger",
                        averageDailyDemand: 15.2,
                        recommendedStock: 80,
                        confidenceLevel: 0.78,
                    },
                ],
                ingredientForecasts: mockInventory.map((ing) => ({
                    ingredientName: ing.name,
                    estimatedConsumption: ing.stockQuantity * 0.3,
                    currentStock: ing.stockQuantity,
                    recommendedPurchase:
                        ing.stockQuantity < ing.minStockLevel
                            ? ing.minStockLevel * 2
                            : 0,
                })),
            };
        }
        const response = await fetch(
            `/api/forecasts/demand?startDate=${startDate}&endDate=${endDate}`
        );
        if (!response.ok) throw new Error("Fehler beim Abrufen der Prognose");
        return response.json();
    },

    // Nachhaltigkeits-Bericht
    getSustainabilityReport: async (month, year) => {
        if (USE_MOCK_DATA) {
            await delay(MOCK_DELAY);
            const monthNames = [
                "Januar",
                "Februar",
                "März",
                "April",
                "Mai",
                "Juni",
                "Juli",
                "August",
                "September",
                "Oktober",
                "November",
                "Dezember",
            ];
            return {
                period: `${monthNames[month - 1]} ${year}`,
                wasteReduction: {
                    totalMealsPrepared: 2450,
                    totalMealsSold: 2380,
                    wastedMeals: 70,
                    wastePercentage: 2.86,
                    previousMonthWastePercentage: 5.2,
                    improvement: 2.34,
                },
                costSavings: {
                    savedCosts: 224.0,
                    potentialSavings: 156.0,
                },
                topWastedMeals: [
                    {
                        mealName: "Caesar Salad",
                        wastedPortions: 25,
                        costOfWaste: 75.0,
                    },
                ],
                recommendations: [
                    "Reduktion der Caesar Salad Portionen um 10%",
                    "Erhöhung der Veganer Burger Produktion um 5%",
                ],
            };
        }
        const response = await fetch(
            `/api/reports/sustainability?month=${month}&year=${year}`
        );
        if (!response.ok)
            throw new Error("Fehler beim Abrufen des Nachhaltigkeitsberichts");
        return response.json();
    },
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
    useMockData: USE_MOCK_DATA,
};

export default api;
