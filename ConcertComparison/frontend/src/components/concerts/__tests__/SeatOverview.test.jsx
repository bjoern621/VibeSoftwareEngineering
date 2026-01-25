import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import SeatOverview from "../SeatOverview";
import { SEAT_STATUS } from "../../../constants/seatConstants";

// Mock price formatter
jest.mock("../../../utils/priceFormatter", () => ({
    formatPrice: (price) => `${price.toFixed(2)}€`,
}));

const mockSeats = [
    {
        id: "1",
        block: "A",
        category: "Premium",
        row: "1",
        number: "1",
        price: 150.0,
        status: SEAT_STATUS.AVAILABLE,
    },
    {
        id: "2",
        block: "A",
        category: "Premium",
        row: "1",
        number: "2",
        price: 150.0,
        status: SEAT_STATUS.HELD,
    },
    {
        id: "3",
        block: "A",
        category: "Premium",
        row: "1",
        number: "3",
        price: 150.0,
        status: SEAT_STATUS.SOLD,
    },
    {
        id: "4",
        block: "B",
        category: "Standard",
        row: "1",
        number: "1",
        price: 75.0,
        status: SEAT_STATUS.AVAILABLE,
    },
];

const mockAvailability = [
    {
        category: "Premium",
        available: 1,
        held: 1,
        sold: 1,
    },
    {
        category: "Standard",
        available: 1,
        held: 0,
        sold: 0,
    },
];

describe("SeatOverview Component", () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    describe("Rendering", () => {
        it("renders seat overview without crashing", () => {
            render(
                <SeatOverview
                    seats={mockSeats}
                    availabilityByCategory={mockAvailability}
                />,
            );
            expect(
                screen.getByText("Verfügbarkeit nach Kategorie"),
            ).toBeInTheDocument();
        });

        it("displays seats grouped by category", () => {
            render(
                <SeatOverview
                    seats={mockSeats}
                    availabilityByCategory={mockAvailability}
                />,
            );
            // Category names appear twice (in availability card and seat section)
            expect(screen.getAllByText("Premium").length).toBe(2);
            expect(screen.getAllByText("Standard").length).toBe(2);
        });

        it("displays seats grouped by block within category", () => {
            render(
                <SeatOverview
                    seats={mockSeats}
                    availabilityByCategory={mockAvailability}
                />,
            );
            expect(screen.getByText("Block A")).toBeInTheDocument();
            expect(screen.getByText("Block B")).toBeInTheDocument();
        });

        it("displays seats grouped by row within block", () => {
            render(
                <SeatOverview
                    seats={mockSeats}
                    availabilityByCategory={mockAvailability}
                />,
            );
            // "Reihe 1" appears twice (once in Premium Block A, once in Standard Block B)
            expect(screen.getAllByText("Reihe 1")).toHaveLength(2);
        });

        it("displays individual seats", () => {
            render(
                <SeatOverview
                    seats={mockSeats}
                    availabilityByCategory={mockAvailability}
                />,
            );
            // Seat numbers appear multiple times (in availability stats and on buttons)
            const ones = screen.getAllByText("1");
            expect(ones.length).toBeGreaterThan(1); // Should have seat buttons with number 1
            expect(screen.getAllByText("2").length).toBe(1); // Only appears as seat button (held seat)
            expect(screen.getAllByText("3").length).toBeGreaterThan(1); // Seat button + total count in availability
        });

        it("displays availability statistics per category", () => {
            render(
                <SeatOverview
                    seats={mockSeats}
                    availabilityByCategory={mockAvailability}
                />,
            );
            // Check for availability counts (multiple "1" values exist in the UI)
            const ones = screen.getAllByText("1");
            expect(ones.length).toBeGreaterThan(2); // Should have multiple "1" values for availability stats
        });

        it("displays legend with seat status colors", () => {
            render(
                <SeatOverview
                    seats={mockSeats}
                    availabilityByCategory={mockAvailability}
                />,
            );
            // These labels appear multiple times in the UI (in cards and legend)
            expect(
                screen.getAllByText("Verfügbar").length,
            ).toBeGreaterThanOrEqual(1);
            expect(
                screen.getAllByText("Reserviert").length,
            ).toBeGreaterThanOrEqual(1);
            expect(
                screen.getAllByText("Verkauft").length,
            ).toBeGreaterThanOrEqual(1);
            expect(
                screen.getAllByText("Ausgewählt").length,
            ).toBeGreaterThanOrEqual(1);
        });

        it("displays stage indicator", () => {
            render(
                <SeatOverview
                    seats={mockSeats}
                    availabilityByCategory={mockAvailability}
                />,
            );
            expect(screen.getByText("🎤 BÜHNE")).toBeInTheDocument();
        });

        it("displays percentage of available seats", () => {
            render(
                <SeatOverview
                    seats={mockSeats}
                    availabilityByCategory={mockAvailability}
                />,
            );
            expect(screen.getByText("33% verfügbar")).toBeInTheDocument();
        });
    });

    describe("Seat Interaction", () => {
        it("calls onSeatSelect when an available seat is clicked", () => {
            const onSeatSelect = jest.fn();
            render(
                <SeatOverview
                    seats={mockSeats}
                    availabilityByCategory={mockAvailability}
                    onSeatSelect={onSeatSelect}
                />,
            );

            // Get all seats with that row/number pattern (there are 2: Premium and Standard)
            const seatButtons = screen.getAllByRole("button", {
                name: /sitzplatz reihe 1, nummer 1/i,
            });
            fireEvent.click(seatButtons[0]); // Click the first one (Premium)

            expect(onSeatSelect).toHaveBeenCalledTimes(1);
            expect(onSeatSelect).toHaveBeenCalledWith(mockSeats[0]);
        });

        it("does not call onSeatSelect when a held seat is clicked", () => {
            const onSeatSelect = jest.fn();
            render(
                <SeatOverview
                    seats={mockSeats}
                    availabilityByCategory={mockAvailability}
                    onSeatSelect={onSeatSelect}
                />,
            );

            const seatButton = screen.getByRole("button", {
                name: /sitzplatz reihe 1, nummer 2/i,
            });
            fireEvent.click(seatButton);

            expect(onSeatSelect).not.toHaveBeenCalled();
        });

        it("does not call onSeatSelect when a sold seat is clicked", () => {
            const onSeatSelect = jest.fn();
            render(
                <SeatOverview
                    seats={mockSeats}
                    availabilityByCategory={mockAvailability}
                    onSeatSelect={onSeatSelect}
                />,
            );

            const seatButton = screen.getByRole("button", {
                name: /sitzplatz reihe 1, nummer 3/i,
            });
            fireEvent.click(seatButton);

            expect(onSeatSelect).not.toHaveBeenCalled();
        });

        it("highlights selected seat", () => {
            const onSeatSelect = jest.fn();
            render(
                <SeatOverview
                    seats={mockSeats}
                    availabilityByCategory={mockAvailability}
                    selectedSeat={mockSeats[0]}
                    onSeatSelect={onSeatSelect}
                />,
            );

            // Get all available seats (2 with same row/number pattern)
            const seatButtons = screen.getAllByRole("button", {
                name: /sitzplatz reihe 1, nummer 1/i,
            });
            // The first one (Premium) should be highlighted as selected
            expect(seatButtons[0]).toHaveClass("bg-primary", "ring-2");
        });

        it("handles keyboard navigation (Enter key)", () => {
            const onSeatSelect = jest.fn();
            render(
                <SeatOverview
                    seats={mockSeats}
                    availabilityByCategory={mockAvailability}
                    onSeatSelect={onSeatSelect}
                />,
            );

            const seatButtons = screen.getAllByRole("button", {
                name: /sitzplatz reihe 1, nummer 1/i,
            });
            fireEvent.keyPress(seatButtons[0], {
                key: "Enter",
                code: "Enter",
                charCode: 13,
            });

            expect(onSeatSelect).toHaveBeenCalledTimes(1);
        });

        it("handles keyboard navigation (Space key)", () => {
            const onSeatSelect = jest.fn();
            render(
                <SeatOverview
                    seats={mockSeats}
                    availabilityByCategory={mockAvailability}
                    onSeatSelect={onSeatSelect}
                />,
            );

            const seatButtons = screen.getAllByRole("button", {
                name: /sitzplatz reihe 1, nummer 1/i,
            });
            fireEvent.keyPress(seatButtons[0], {
                key: " ",
                code: "Space",
                charCode: 32,
            });

            expect(onSeatSelect).toHaveBeenCalledTimes(1);
        });
    });

    describe("Loading State", () => {
        it("renders loading skeleton when loading", () => {
            const { container } = render(<SeatOverview loading={true} />);
            const skeletons = container.querySelectorAll(".animate-pulse");
            expect(skeletons.length).toBeGreaterThan(0);
        });
    });

    describe("Error State", () => {
        it("renders error state when error is provided", () => {
            const onRetry = jest.fn();
            render(
                <SeatOverview error="Fehler beim Laden" onRetry={onRetry} />,
            );

            expect(
                screen.getByText("Fehler beim Laden der Sitzplätze"),
            ).toBeInTheDocument();
            expect(screen.getByText("Fehler beim Laden")).toBeInTheDocument();
        });

        it("calls onRetry when retry button is clicked", () => {
            const onRetry = jest.fn();
            render(
                <SeatOverview error="Fehler beim Laden" onRetry={onRetry} />,
            );

            const retryButton = screen.getByText("Erneut versuchen");
            fireEvent.click(retryButton);

            expect(onRetry).toHaveBeenCalledTimes(1);
        });
    });

    describe("Empty State", () => {
        it("renders empty state when no seats are available", () => {
            render(<SeatOverview seats={[]} availabilityByCategory={[]} />);

            expect(
                screen.getByText("Keine Sitzplätze verfügbar"),
            ).toBeInTheDocument();
            expect(
                screen.getByText(
                    "Für dieses Konzert sind keine Sitzplätze vorhanden.",
                ),
            ).toBeInTheDocument();
        });
    });

    describe("Accessibility", () => {
        it("has proper ARIA labels for seats", () => {
            render(
                <SeatOverview
                    seats={mockSeats}
                    availabilityByCategory={mockAvailability}
                />,
            );

            // Get all buttons with that name pattern since there are multiple categories
            const seatButtons = screen.getAllByRole("button", {
                name: /sitzplatz reihe 1, nummer 1/i,
            });
            expect(seatButtons.length).toBeGreaterThan(0);
            expect(seatButtons[0]).toHaveAttribute(
                "aria-label",
                "Sitzplatz Reihe 1, Nummer 1, Verfügbar",
            );
        });

        it("disables held and sold seats", () => {
            render(
                <SeatOverview
                    seats={mockSeats}
                    availabilityByCategory={mockAvailability}
                />,
            );

            // Only one held seat (id: 2)
            const heldSeat = screen.getByRole("button", {
                name: /sitzplatz reihe 1, nummer 2/i,
            });
            expect(heldSeat).toBeDisabled();

            // Only one sold seat (id: 3)
            const soldSeat = screen.getByRole("button", {
                name: /sitzplatz reihe 1, nummer 3/i,
            });
            expect(soldSeat).toBeDisabled();
        });

        it("enables available seats", () => {
            render(
                <SeatOverview
                    seats={mockSeats}
                    availabilityByCategory={mockAvailability}
                />,
            );

            // Get all available seats (both id: 1 and id: 4 are available)
            const availableSeats = screen.getAllByRole("button", {
                name: /sitzplatz reihe 1, nummer 1/i,
            });
            availableSeats.forEach((seat) => {
                expect(seat).not.toBeDisabled();
            });
        });
    });

    describe("Responsive Design", () => {
        it("renders correctly with different screen sizes", () => {
            const { container } = render(
                <SeatOverview
                    seats={mockSeats}
                    availabilityByCategory={mockAvailability}
                />,
            );

            // Check for responsive grid classes
            const availabilityCards =
                container.querySelectorAll(".grid-cols-1");
            expect(availabilityCards.length).toBeGreaterThan(0);
        });
    });

    describe("Data Handling", () => {
        it("handles seats without category (defaults to Standard)", () => {
            const seatsWithoutCategory = [
                {
                    id: "1",
                    block: "A",
                    row: "1",
                    number: "1",
                    price: 75.0,
                    status: SEAT_STATUS.AVAILABLE,
                },
            ];

            render(
                <SeatOverview
                    seats={seatsWithoutCategory}
                    availabilityByCategory={[]}
                />,
            );
            expect(screen.getByText("Standard")).toBeInTheDocument();
        });

        it("handles seats without block (defaults to Allgemein)", () => {
            const seatsWithoutBlock = [
                {
                    id: "1",
                    category: "Premium",
                    row: "1",
                    number: "1",
                    price: 150.0,
                    status: SEAT_STATUS.AVAILABLE,
                },
            ];

            render(
                <SeatOverview
                    seats={seatsWithoutBlock}
                    availabilityByCategory={[]}
                />,
            );
            expect(screen.getByText("Block Allgemein")).toBeInTheDocument();
        });

        it("handles seats with missing availability data", () => {
            const incompleteAvailability = [
                {
                    category: "Premium",
                    available: 1,
                    held: 0,
                    sold: 0,
                },
            ];

            render(
                <SeatOverview
                    seats={mockSeats}
                    availabilityByCategory={incompleteAvailability}
                />,
            );
            // Check that "0" values are present (there should be at least 2: held and sold)
            const zeros = screen.getAllByText("0");
            expect(zeros.length).toBeGreaterThanOrEqual(2);
        });
    });

    describe("Sorting", () => {
        it("sorts rows numerically", () => {
            const seatsWithNumericRows = [
                {
                    id: "1",
                    block: "A",
                    category: "Premium",
                    row: "10",
                    number: "1",
                    price: 150.0,
                    status: SEAT_STATUS.AVAILABLE,
                },
                {
                    id: "2",
                    block: "A",
                    category: "Premium",
                    row: "1",
                    number: "1",
                    price: 150.0,
                    status: SEAT_STATUS.AVAILABLE,
                },
                {
                    id: "3",
                    block: "A",
                    category: "Premium",
                    row: "5",
                    number: "1",
                    price: 150.0,
                    status: SEAT_STATUS.AVAILABLE,
                },
            ];

            render(
                <SeatOverview
                    seats={seatsWithNumericRows}
                    availabilityByCategory={[]}
                />,
            );
            const rows = screen.getAllByText(/Reihe \d+/);
            expect(rows[0]).toHaveTextContent("Reihe 1");
            expect(rows[1]).toHaveTextContent("Reihe 5");
            expect(rows[2]).toHaveTextContent("Reihe 10");
        });

        it("sorts seat numbers numerically", () => {
            const seatsWithNumericNumbers = [
                {
                    id: "1",
                    block: "A",
                    category: "Premium",
                    row: "1",
                    number: "10",
                    price: 150.0,
                    status: SEAT_STATUS.AVAILABLE,
                },
                {
                    id: "2",
                    block: "A",
                    category: "Premium",
                    row: "1",
                    number: "1",
                    price: 150.0,
                    status: SEAT_STATUS.AVAILABLE,
                },
                {
                    id: "3",
                    block: "A",
                    category: "Premium",
                    row: "1",
                    number: "5",
                    price: 150.0,
                    status: SEAT_STATUS.AVAILABLE,
                },
            ];

            render(
                <SeatOverview
                    seats={seatsWithNumericNumbers}
                    availabilityByCategory={[]}
                />,
            );
            const rowSection = screen.getByText("Reihe 1").closest("div");
            const seats = rowSection.querySelectorAll("button");
            expect(seats[0]).toHaveTextContent("1");
            expect(seats[1]).toHaveTextContent("5");
            expect(seats[2]).toHaveTextContent("10");
        });
    });
});
