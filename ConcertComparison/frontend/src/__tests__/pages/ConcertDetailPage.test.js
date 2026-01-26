import React from "react";
import {
    render,
    screen,
    fireEvent,
    waitFor,
    within,
} from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import ConcertDetailPage from "../../pages/ConcertDetailPage";
import * as concertService from "../../services/concertService";
import * as seatService from "../../services/seatService";
import { CartProvider } from "../../context/CartContext";
import { AuthProvider } from "../../context/AuthContext";

// Mock EventSource für SSE Tests
class MockEventSource {
    static instances = [];
    static lastCallbacks = null;

    constructor(url) {
        this.url = url;
        this.readyState = 0;
        this.onopen = null;
        this.onmessage = null;
        this.onerror = null;
        this._listeners = {};
        MockEventSource.instances.push(this);
    }

    addEventListener(event, callback) {
        if (!this._listeners[event]) {
            this._listeners[event] = [];
        }
        this._listeners[event].push(callback);
    }

    removeEventListener(event, callback) {
        if (this._listeners[event]) {
            this._listeners[event] = this._listeners[event].filter(cb => cb !== callback);
        }
    }

    close() {
        this.readyState = 2;
    }

    simulateOpen() {
        this.readyState = 1;
        if (this.onopen) {
            this.onopen({ type: 'open' });
        }
    }

    simulateError() {
        if (this.onerror) {
            this.onerror({ type: 'error' });
        }
    }

    simulateSeatUpdate(data) {
        const event = { data: JSON.stringify(data) };
        if (this._listeners['seat_update']) {
            this._listeners['seat_update'].forEach(cb => cb(event));
        }
    }

    static reset() {
        MockEventSource.instances = [];
    }

    static getLastInstance() {
        return MockEventSource.instances[MockEventSource.instances.length - 1];
    }
}

// EventSource global mocken
const originalEventSource = global.EventSource;

// Mock authService to prevent real API calls during tests
jest.mock("../../services/authService", () => ({
    __esModule: true,
    default: {
        isAuthenticated: () => false,
        getProfile: () => Promise.resolve(null),
        login: jest.fn(),
        logout: jest.fn(),
        register: jest.fn(),
    },
}));

// Mock useParams and useNavigate
const mockNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
    ...jest.requireActual("react-router-dom"),
    useParams: () => ({ id: "1" }),
    useNavigate: () => mockNavigate,
}));

// Mock services - preserve SSE functions from seatService
jest.mock("../../services/concertService");
jest.mock("../../services/seatService", () => {
    const actual = jest.requireActual("../../services/seatService");
    return {
        ...actual,
        fetchConcertSeats: jest.fn(),
        fetchSeatAvailability: jest.fn(),
        createSeatHold: jest.fn(),
        cancelSeatHold: jest.fn(),
    };
});

describe("ConcertDetailPage Component", () => {
    const mockConcert = {
        id: "1",
        name: "Metallica Live 2025",
        date: "2025-06-15T20:00:00",
        venue: "Mercedes-Benz Arena Berlin",
        description:
            "The legendary metal band returns to Berlin for an epic show!",
        minPrice: 89.5,
        maxPrice: 299.0,
        totalSeats: 100,
        availableSeats: 75,
        imageUrl: "https://example.com/concert.jpg",
    };

    const mockSeats = [
        {
            id: "s1",
            block: "A",
            category: "VIP",
            row: "1",
            number: "1",
            price: 299.0,
            status: "AVAILABLE",
        },
        {
            id: "s2",
            block: "A",
            category: "VIP",
            row: "1",
            number: "2",
            price: 299.0,
            status: "HELD",
        },
        {
            id: "s3",
            block: "A",
            category: "VIP",
            row: "1",
            number: "3",
            price: 299.0,
            status: "SOLD",
        },
        {
            id: "s4",
            block: "B",
            category: "Standard",
            row: "2",
            number: "1",
            price: 89.5,
            status: "AVAILABLE",
        },
    ];

    const renderComponent = () => {
        return render(
            <BrowserRouter>
                <AuthProvider>
                    <CartProvider>
                        <ConcertDetailPage />
                    </CartProvider>>
                </AuthProvider>
            </BrowserRouter>,
        );
    };

    // Set up MockEventSource for all tests in this suite
    beforeAll(() => {
        global.EventSource = MockEventSource;
    });

    afterAll(() => {
        global.EventSource = originalEventSource;
    });

    beforeEach(() => {
        jest.clearAllMocks();
        MockEventSource.reset();
        concertService.fetchConcertById.mockResolvedValue(mockConcert);
        seatService.fetchConcertSeats.mockResolvedValue(mockSeats);
    });

    afterEach(() => {
        // Cleanup alle SSE Verbindungen
        MockEventSource.instances.forEach(instance => {
            if (instance.readyState !== 2) {
                instance.close();
            }
        });
        MockEventSource.reset();
    });

    describe("Loading State", () => {
        test("shows loading skeleton while fetching data", () => {
            // Delay the response to see loading state
            concertService.fetchConcertById.mockImplementation(
                () =>
                    new Promise((resolve) =>
                        setTimeout(() => resolve(mockConcert), 100),
                    ),
            );

            renderComponent();

            // Loading skeleton should have animated elements
            expect(
                document.querySelector(".animate-pulse"),
            ).toBeInTheDocument();
        });
    });

    describe("Successful Data Loading", () => {
        test("renders concert title after loading", async () => {
            renderComponent();

            await waitFor(() => {
                // Title appears in h1 header
                const titles = screen.getAllByText("Metallica Live 2025");
                expect(titles.length).toBeGreaterThanOrEqual(1);
            });
        });

        test("renders concert venue", async () => {
            renderComponent();

            await waitFor(() => {
                const venues = screen.getAllByText(
                    "Mercedes-Benz Arena Berlin",
                );
                expect(venues.length).toBeGreaterThanOrEqual(1);
            });
        });

        test("renders concert description", async () => {
            renderComponent();

            await waitFor(() => {
                expect(
                    screen.getByText(/The legendary metal band returns/),
                ).toBeInTheDocument();
            });
        });

        test("renders price information", async () => {
            renderComponent();

            await waitFor(() => {
                const priceElements = screen.getAllByText(/89,50/);
                expect(priceElements.length).toBeGreaterThanOrEqual(1);
            });
        });

        test("renders availability statistics", async () => {
            renderComponent();

            await waitFor(() => {
                // Check for availability - 2 AVAILABLE out of 4 total seats
                expect(screen.getByText(/von 4 Plätzen frei/i)).toBeInTheDocument();
                // Check that price range is displayed 
                const priceElements = screen.getAllByText(/89,50 €/);
                expect(priceElements.length).toBeGreaterThan(0);
                const maxPriceElements = screen.getAllByText(/299,00 €/);
                expect(maxPriceElements.length).toBeGreaterThan(0);
            });
        });
    });

    describe("Navigation", () => {
        test("renders breadcrumb with concert name", async () => {
            renderComponent();

            await waitFor(() => {
                expect(screen.getByText("Konzerte")).toBeInTheDocument();
            });

            await waitFor(() => {
                const titles = screen.getAllByText("Metallica Live 2025");
                expect(titles.length).toBeGreaterThanOrEqual(1);
            });
        });

        test("navigates back to concert list when back button is clicked", async () => {
            renderComponent();

            await waitFor(() => {
                const titles = screen.getAllByText("Metallica Live 2025");
                expect(titles.length).toBeGreaterThanOrEqual(1);
            });

            const backButton = screen.getByLabelText("Zurück zur Übersicht");
            fireEvent.click(backButton);

            expect(mockNavigate).toHaveBeenCalledWith("/concerts");
        });

        test("breadcrumb link navigates to concerts page", async () => {
            renderComponent();

            // Wait for content to load first
            await waitFor(() => {
                expect(screen.getByText("Konzertdetails")).toBeInTheDocument();
            });

            // Check that the breadcrumb nav contains a link with "Konzerte" text
            const nav = screen.getByRole("navigation");
            expect(nav).toBeInTheDocument();

            // The Link component should be in the nav with "Konzerte" text
            const konzerteText = screen.getByText("Konzerte");
            expect(konzerteText).toBeInTheDocument();

            // Verify the parent is an anchor element (Link renders as <a>)
            expect(konzerteText.closest("a")).toBeInTheDocument();
        });
    });

    describe("Seat Map", () => {
        test("renders seat map section", async () => {
            renderComponent();

            await waitFor(() => {
                expect(
                    screen.getByText("Sitzplatz wählen"),
                ).toBeInTheDocument();
            });
        });

        test("renders seats grouped by block", async () => {
            renderComponent();

            await waitFor(() => {
                const blockElements = screen.getAllByText(/Block [AB]/);
                expect(blockElements.length).toBeGreaterThanOrEqual(1);
            });
        });

        test("renders seat legend with color explanations", async () => {
            renderComponent();

            await waitFor(() => {
                const verfugbarElements = screen.getAllByText("Verfügbar");
                expect(verfugbarElements.length).toBeGreaterThanOrEqual(1);
            });
        });

        test("clicking available seat opens dialog", async () => {
            renderComponent();

            await waitFor(() => {
                expect(
                    screen.getByText("Sitzplatz wählen"),
                ).toBeInTheDocument();
            });

            // Find and click an available seat using title
            const availableSeat = screen.getByTitle(
                /Reihe 1, Platz 1 - Verfügbar/,
            );
            fireEvent.click(availableSeat);

            await waitFor(() => {
                expect(
                    screen.getByText("Sitzplatz auswählen"),
                ).toBeInTheDocument();
                expect(screen.getByText("Reservieren")).toBeInTheDocument();
            });
        });
    });

    describe("Seat Selection Dialog", () => {
        test("displays selected seat details in dialog", async () => {
            renderComponent();

            await waitFor(() => {
                expect(
                    screen.getByText("Sitzplatz wählen"),
                ).toBeInTheDocument();
            });

            // Click an available seat using title
            const availableSeat = screen.getByTitle(
                /Reihe 1, Platz 1 - Verfügbar/,
            );
            fireEvent.click(availableSeat);

            await waitFor(() => {
                // Check dialog shows seat details - query within dialog context
                const dialogTitle = screen.getByText("Sitzplatz auswählen");
                const dialog = dialogTitle.closest(".relative.bg-card-light");
                within(dialog).getByText("VIP");
            });
        });

        test("closes dialog when cancel button is clicked", async () => {
            renderComponent();

            await waitFor(() => {
                expect(
                    screen.getByText("Sitzplatz wählen"),
                ).toBeInTheDocument();
            });

            // Open dialog using title to be specific
            const availableSeat = screen.getByTitle(
                /Reihe 1, Platz 1 - Verfügbar/,
            );
            fireEvent.click(availableSeat);

            await waitFor(() => {
                expect(
                    screen.getByText("Sitzplatz auswählen"),
                ).toBeInTheDocument();
            });

            // Close dialog
            const cancelButton = screen.getByText("Abbrechen");
            fireEvent.click(cancelButton);

            await waitFor(() => {
                expect(
                    screen.queryByText("Sitzplatz auswählen"),
                ).not.toBeInTheDocument();
            });
        });

        test("shows success notification after successful reservation", async () => {
            seatService.createSeatHold.mockResolvedValue({
                id: "res1",
                expiresAt: "2025-06-15T20:15:00",
            });

            renderComponent();

            await waitFor(() => {
                expect(
                    screen.getByText("Sitzplatz wählen"),
                ).toBeInTheDocument();
            });

            // Open dialog and reserve using title
            const availableSeat = screen.getByTitle(
                /Reihe 1, Platz 1 - Verfügbar/,
            );
            fireEvent.click(availableSeat);

            await waitFor(() => {
                expect(screen.getByText("Reservieren")).toBeInTheDocument();
            });

            fireEvent.click(screen.getByText("Reservieren"));

            await waitFor(() => {
                expect(
                    screen.getByText(/erfolgreich reserviert/),
                ).toBeInTheDocument();
            });
        });
    });

    describe("Error Handling", () => {
        test("shows error message when concert not found", async () => {
            concertService.fetchConcertById.mockRejectedValue({
                response: { status: 404 },
            });

            renderComponent();

            await waitFor(() => {
                expect(
                    screen.getByText("Konzert nicht gefunden"),
                ).toBeInTheDocument();
            });
        });

        test("shows generic error message on API failure", async () => {
            concertService.fetchConcertById.mockRejectedValue({
                response: {
                    status: 500,
                    data: { message: "Interner Serverfehler" },
                },
            });

            renderComponent();

            await waitFor(() => {
                expect(
                    screen.getByText("Interner Serverfehler"),
                ).toBeInTheDocument();
            });
        });

        test("shows retry button on error", async () => {
            concertService.fetchConcertById.mockRejectedValue({
                response: { status: 500 },
            });

            renderComponent();

            await waitFor(() => {
                expect(
                    screen.getByText("Erneut versuchen"),
                ).toBeInTheDocument();
            });
        });

        test("retries loading when retry button is clicked", async () => {
            concertService.fetchConcertById
                .mockRejectedValueOnce({ response: { status: 500 } })
                .mockResolvedValueOnce(mockConcert);

            renderComponent();

            await waitFor(() => {
                expect(
                    screen.getByText("Erneut versuchen"),
                ).toBeInTheDocument();
            });

            fireEvent.click(screen.getByText("Erneut versuchen"));

            await waitFor(() => {
                const titles = screen.getAllByText("Metallica Live 2025");
                expect(titles.length).toBeGreaterThanOrEqual(1);
            });

            expect(concertService.fetchConcertById).toHaveBeenCalledTimes(2);
        });

        test("shows error notification when seat hold fails", async () => {
            seatService.createSeatHold.mockRejectedValue({
                response: {
                    status: 409,
                    data: { message: "Sitzplatz bereits reserviert" },
                },
            });

            renderComponent();

            await waitFor(() => {
                expect(
                    screen.getByText("Sitzplatz wählen"),
                ).toBeInTheDocument();
            });

            // Open dialog and try to reserve using title
            const availableSeat = screen.getByTitle(
                /Reihe 1, Platz 1 - Verfügbar/,
            );
            fireEvent.click(availableSeat);

            await waitFor(() => {
                expect(screen.getByText("Reservieren")).toBeInTheDocument();
            });

            fireEvent.click(screen.getByText("Reservieren"));

            await waitFor(() => {
                expect(
                    screen.getByText(/bereits reserviert/),
                ).toBeInTheDocument();
            });
        });
    });

    describe("SSE Live Updates", () => {
        test("establishes SSE connection after loading", async () => {
            renderComponent();

            await waitFor(() => {
                expect(screen.getByText("Sitzplatz wählen")).toBeInTheDocument();
            });

            // SSE Verbindung wird hergestellt
            await waitFor(() => {
                expect(MockEventSource.instances.length).toBeGreaterThan(0);
            });

            // Simuliere erfolgreiche Verbindung
            const eventSource = MockEventSource.getLastInstance();
            eventSource.simulateOpen();

            // Verbindung sollte zur korrekten URL hergestellt werden
            expect(eventSource.url).toContain("/events/1/seats/stream");
        });

        test("updates seat status when SSE event received", async () => {
            renderComponent();

            await waitFor(() => {
                expect(screen.getByText("Sitzplatz wählen")).toBeInTheDocument();
            });

            // Warte auf SSE Verbindung
            await waitFor(() => {
                expect(MockEventSource.instances.length).toBeGreaterThan(0);
            });

            const eventSource = MockEventSource.getLastInstance();
            eventSource.simulateOpen();

            // Prüfe initialen Status - s1 ist AVAILABLE
            const availableSeat = screen.getByTitle(/Reihe 1, Platz 1 - Verfügbar/);
            expect(availableSeat).toBeInTheDocument();

            // Simuliere seat_update Event: s1 wird zu SOLD
            eventSource.simulateSeatUpdate({
                seatId: "s1",
                status: "SOLD",
                timestamp: "2026-01-26T10:00:00Z",
            });

            // Sitz sollte jetzt als Verkauft angezeigt werden (nicht mehr klickbar)
            await waitFor(() => {
                const soldSeat = screen.getByTitle(/Reihe 1, Platz 1 - Verkauft/);
                expect(soldSeat).toBeInTheDocument();
                expect(soldSeat).toBeDisabled();
            });
        });

        test("handles connection error and attempts reconnect", async () => {
            jest.useFakeTimers();

            renderComponent();

            await waitFor(() => {
                expect(screen.getByText("Sitzplatz wählen")).toBeInTheDocument();
            });

            await waitFor(() => {
                expect(MockEventSource.instances.length).toBeGreaterThan(0);
            });

            const eventSource = MockEventSource.getLastInstance();
            eventSource.simulateOpen();

            // Simuliere Verbindungsfehler
            eventSource.simulateError();

            // Nach Fehler sollte Reconnect versucht werden (nach 1 Sekunde)
            jest.advanceTimersByTime(1000);

            // Es sollte eine neue EventSource-Instanz geben (Reconnect)
            await waitFor(() => {
                expect(MockEventSource.instances.length).toBeGreaterThan(1);
            });

            jest.useRealTimers();
        });

        test("establishes SSE connection to correct endpoint", async () => {
            renderComponent();

            await waitFor(() => {
                expect(screen.getByText("Sitzplatz wählen")).toBeInTheDocument();
            });

            await waitFor(() => {
                expect(MockEventSource.instances.length).toBeGreaterThan(0);
            });

            const eventSource = MockEventSource.getLastInstance();
            
            // Prüfe dass die URL das richtige Format hat
            expect(eventSource.url).toContain("/events/1/seats/stream");
        });

        test("updates availability count when seat status changes via SSE", async () => {
            renderComponent();

            await waitFor(() => {
                expect(screen.getByText("Sitzplatz wählen")).toBeInTheDocument();
            });

            // Initial: 2 von 4 Plätzen verfügbar (s1 und s4)
            await waitFor(() => {
                expect(screen.getByText(/von 4 Plätzen frei/i)).toBeInTheDocument();
            });

            await waitFor(() => {
                expect(MockEventSource.instances.length).toBeGreaterThan(0);
            });

            const eventSource = MockEventSource.getLastInstance();
            eventSource.simulateOpen();

            // Simuliere: s1 wird verkauft
            eventSource.simulateSeatUpdate({
                seatId: "s1",
                status: "SOLD",
                timestamp: "2026-01-26T10:00:00Z",
            });

            // Jetzt sollte nur noch 1 von 4 Plätzen frei sein
            await waitFor(() => {
                expect(screen.getByText(/von 4 Plätzen frei/i)).toBeInTheDocument();
                // Die Zahl "1" sollte irgendwo in der Verfügbarkeitsanzeige erscheinen
                const availabilityElement = screen.getByText(/von 4 Plätzen frei/i).closest('div');
                expect(availabilityElement.textContent).toMatch(/1/);
            });
        });
    });
});
