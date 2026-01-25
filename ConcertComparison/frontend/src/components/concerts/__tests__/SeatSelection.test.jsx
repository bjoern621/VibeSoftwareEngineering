// Mock it useHoldTimer hook
jest.mock("../../../hooks/useHoldTimer");

import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import SeatSelection from "../SeatSelection";
import * as seatService from "../../../services/seatService";
import { useHoldTimer } from "../../../hooks/useHoldTimer";

const mockSeat = {
    id: "seat-123",
    block: "A",
    category: "VIP",
    row: "1",
    number: "5",
    price: 150.0,
    status: "AVAILABLE",
};

describe("SeatSelection Component", () => {
    const mockOnClose = jest.fn();
    const mockOnConfirm = jest.fn();

    beforeEach(() => {
        jest.clearAllMocks();
        // Mock implementation of useHoldTimer
        useHoldTimer.mockImplementation((ttlSeconds = 600, onExpire) => ({
            timeLeft: ttlSeconds,
            formattedTime: "10:00",
            progressPercentage: 0,
            isActive: false,
            isExpired: false,
            start: jest.fn(),
            stop: jest.fn(),
            reset: jest.fn(),
        }));
    });

    describe("Rendering", () => {
        it("should not render when no seat is selected", () => {
            const { container } = render(
                <SeatSelection
                    seat={null}
                    onClose={mockOnClose}
                    onConfirm={mockOnConfirm}
                />,
            );
            expect(container.firstChild).toBeNull();
        });

        it("should render seat selection form when seat is provided", () => {
            render(
                <SeatSelection
                    seat={mockSeat}
                    onClose={mockOnClose}
                    onConfirm={mockOnConfirm}
                />,
            );

            expect(screen.getByText("Sitzplatz auswählen")).toBeInTheDocument();
            expect(screen.getByText("Block")).toBeInTheDocument();
            expect(screen.getByText("A")).toBeInTheDocument();
            expect(screen.getByText("Kategorie")).toBeInTheDocument();
            expect(screen.getByText("VIP")).toBeInTheDocument();
            expect(screen.getByText("Reihe")).toBeInTheDocument();
            expect(screen.getByText("1")).toBeInTheDocument();
            expect(screen.getByText("Sitzplatz")).toBeInTheDocument();
            expect(screen.getByText("5")).toBeInTheDocument();
            expect(screen.getByText("Preis")).toBeInTheDocument();
            expect(screen.getByText("150.00 €")).toBeInTheDocument();
        });

        it("should display default values for missing seat properties", () => {
            const seatWithoutProperties = {
                id: "seat-456",
                row: "2",
                number: "10",
            };
            render(
                <SeatSelection
                    seat={seatWithoutProperties}
                    onClose={mockOnClose}
                    onConfirm={mockOnConfirm}
                />,
            );

            expect(screen.getByText("Allgemein")).toBeInTheDocument();
            expect(screen.getByText("Standard")).toBeInTheDocument();
        });

        it("should display countdown timer in confirmed state", () => {
            const useHoldTimer = require("../../../hooks/useHoldTimer");
            useHoldTimer.useHoldTimer.mockReturnValue({
                timeLeft: 600,
                formattedTime: "10:00",
                progressPercentage: 0,
                isActive: true,
                isExpired: false,
                start: jest.fn(),
                stop: jest.fn(),
                reset: jest.fn(),
            });

            mockOnConfirm.mockResolvedValue({ holdId: "hold-123" });

            const { rerender } = render(
                <SeatSelection
                    seat={mockSeat}
                    onClose={mockOnClose}
                    onConfirm={mockOnConfirm}
                />,
            );

            // Click reserve button
            const reserveButton = screen.getByText("Reservieren");
            fireEvent.click(reserveButton);

            waitFor(() => {
                expect(
                    screen.getByText("Reservierung erfolgreich"),
                ).toBeInTheDocument();
                expect(screen.getByText("10:00")).toBeInTheDocument();
                expect(screen.getByText("hold-123")).toBeInTheDocument();
            });
        });
    });

    describe("User Interactions", () => {
        it("should call onConfirm when reserve button is clicked", () => {
            render(
                <SeatSelection
                    seat={mockSeat}
                    onClose={mockOnClose}
                    onConfirm={mockOnConfirm}
                />,
            );

            const reserveButton = screen.getByText("Reservieren");
            fireEvent.click(reserveButton);

            expect(mockOnConfirm).toHaveBeenCalledWith(mockSeat);
        });

        it("should call onClose when cancel button is clicked", () => {
            render(
                <SeatSelection
                    seat={mockSeat}
                    onClose={mockOnClose}
                    onConfirm={mockOnConfirm}
                />,
            );

            const cancelButton = screen.getByText("Abbrechen");
            fireEvent.click(cancelButton);

            expect(mockOnClose).toHaveBeenCalled();
        });

        it("should disable reserve button while loading", () => {
            render(
                <SeatSelection
                    seat={mockSeat}
                    isLoading={true}
                    onClose={mockOnClose}
                    onConfirm={mockOnConfirm}
                />,
            );

            const reserveButton = screen.getByRole("button", {
                name: /wird reserviert/i,
            });
            expect(reserveButton).toBeDisabled();
            expect(screen.getByText("Wird reserviert...")).toBeInTheDocument();
        });

        it('should call both "Zur Kasse" and onClose when clicking checkout button in success state', async () => {
            const useHoldTimer = require("../../../hooks/useHoldTimer");
            const mockStop = jest.fn();
            useHoldTimer.useHoldTimer.mockReturnValue({
                timeLeft: 600,
                formattedTime: "10:00",
                progressPercentage: 0,
                isActive: true,
                isExpired: false,
                start: jest.fn(),
                stop: mockStop,
                reset: jest.fn(),
            });

            mockOnConfirm.mockResolvedValue({ holdId: "hold-123" });

            const { rerender } = render(
                <SeatSelection
                    seat={mockSeat}
                    onClose={mockOnClose}
                    onConfirm={mockOnConfirm}
                />,
            );

            // Click reserve button
            const reserveButton = screen.getByText("Reservieren");
            fireEvent.click(reserveButton);

            await waitFor(() => {
                expect(
                    screen.getByText("Reservierung erfolgreich"),
                ).toBeInTheDocument();
            });

            // Click checkout button
            const checkoutButton = screen.getByText("Zur Kasse");
            fireEvent.click(checkoutButton);

            expect(mockOnClose).toHaveBeenCalled();
        });
    });
});
