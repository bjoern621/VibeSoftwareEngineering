import {
    fetchConcertSeats,
    fetchSeatAvailability,
    createSeatHold,
} from "../seatService";
import api from "../api";
import { SEAT_STATUS } from "../../constants/seatConstants";

// Mock the api module
jest.mock("../api");

describe("SeatService Integration Tests", () => {
    beforeEach(() => {
        jest.clearAllMocks();
        localStorage.clear();
    });

    describe("fetchConcertSeats", () => {
        it("fetches seats for a concert successfully", async () => {
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
                    block: "B",
                    category: "Standard",
                    row: "1",
                    number: "1",
                    price: 75.0,
                    status: SEAT_STATUS.AVAILABLE,
                },
            ];

            api.get.mockResolvedValue({
                data: {
                    concertId: "1",
                    seats: mockSeats,
                    availabilityByCategory: [],
                },
            });

            const result = await fetchConcertSeats("1");

            expect(api.get).toHaveBeenCalledWith("/events/1/seats");
            expect(result).toEqual(mockSeats);
        });

        it("handles seats in response.data.seats format", async () => {
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
            ];

            api.get.mockResolvedValue({
                data: {
                    concertId: "1",
                    seats: mockSeats,
                    availabilityByCategory: [],
                },
            });

            const result = await fetchConcertSeats("1");

            expect(result).toEqual(mockSeats);
        });

        it("handles seats in response.data format (backward compatibility)", async () => {
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
            ];

            api.get.mockResolvedValue({
                data: mockSeats,
            });

            const result = await fetchConcertSeats("1");

            expect(result).toEqual(mockSeats);
        });

        it("handles empty seat list", async () => {
            api.get.mockResolvedValue({
                data: {
                    concertId: "1",
                    seats: [],
                    availabilityByCategory: [],
                },
            });

            const result = await fetchConcertSeats("1");

            expect(result).toEqual([]);
        });

        it("handles API error", async () => {
            const mockError = new Error("Network error");
            mockError.response = {
                status: 500,
                data: { message: "Internal Server Error" },
            };
            api.get.mockRejectedValue(mockError);

            await expect(fetchConcertSeats("1")).rejects.toThrow();
            expect(api.get).toHaveBeenCalledWith("/events/1/seats");
        });

        it("handles 404 error (concert not found)", async () => {
            const mockError = new Error("Not Found");
            mockError.response = {
                status: 404,
                data: { message: "Concert not found" },
            };
            api.get.mockRejectedValue(mockError);

            await expect(fetchConcertSeats("1")).rejects.toThrow();
        });

        it("handles timeout error", async () => {
            const mockError = new Error("Request timeout");
            mockError.code = "ECONNABORTED";
            api.get.mockRejectedValue(mockError);

            await expect(fetchConcertSeats("1")).rejects.toThrow();
        });
    });

    describe("fetchSeatAvailability", () => {
        it("fetches availability for a concert successfully", async () => {
            const mockAvailability = [
                {
                    category: "Premium",
                    available: 10,
                    held: 2,
                    sold: 5,
                },
                {
                    category: "Standard",
                    available: 50,
                    held: 5,
                    sold: 20,
                },
            ];

            api.get.mockResolvedValue({
                data: {
                    concertId: "1",
                    seats: [],
                    availabilityByCategory: mockAvailability,
                },
            });

            const result = await fetchSeatAvailability("1");

            expect(api.get).toHaveBeenCalledWith("/events/1/seats");
            expect(result).toEqual(mockAvailability);
        });

        it("handles availability in response.data.availabilityByCategory format", async () => {
            const mockAvailability = [
                {
                    category: "Premium",
                    available: 10,
                    held: 2,
                    sold: 5,
                },
            ];

            api.get.mockResolvedValue({
                data: {
                    concertId: "1",
                    seats: [],
                    availabilityByCategory: mockAvailability,
                },
            });

            const result = await fetchSeatAvailability("1");

            expect(result).toEqual(mockAvailability);
        });

        it("handles empty availability list", async () => {
            api.get.mockResolvedValue({
                data: {
                    concertId: "1",
                    seats: [],
                    availabilityByCategory: [],
                },
            });

            const result = await fetchSeatAvailability("1");

            expect(result).toEqual([]);
        });

        it("handles API error", async () => {
            const mockError = new Error("Network error");
            mockError.response = {
                status: 500,
                data: { message: "Internal Server Error" },
            };
            api.get.mockRejectedValue(mockError);

            await expect(fetchSeatAvailability("1")).rejects.toThrow();
        });
    });

    describe("createSeatHold", () => {
        const mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";

        beforeEach(() => {
            localStorage.setItem("token", mockToken);
        });

        it("handles seat already held", async () => {
            const mockError = new Error("Seat already held");
            mockError.response = {
                status: 409,
                data: { message: "Seat is already held by another user" },
            };
            api.post.mockRejectedValue(mockError);

            await expect(createSeatHold("1")).rejects.toThrow();
        });

        it("handles seat already sold", async () => {
            const mockError = new Error("Seat already sold");
            mockError.response = {
                status: 400,
                data: { message: "Seat is already sold" },
            };
            api.post.mockRejectedValue(mockError);

            await expect(createSeatHold("1")).rejects.toThrow();
        });

        it("handles unauthorized error (no token)", async () => {
            localStorage.clear();

            const mockError = new Error("Unauthorized");
            mockError.response = { status: 401 };
            api.post.mockRejectedValue(mockError);

            await expect(createSeatHold("1")).rejects.toThrow();
        });

        it("handles network error", async () => {
            const mockError = new Error("Network error");
            mockError.response = {
                status: 500,
                data: { message: "Internal Server Error" },
            };
            api.post.mockRejectedValue(mockError);

            await expect(createSeatHold("1")).rejects.toThrow();
        });
    });

    describe("API Integration Scenarios", () => {
        it("handles full flow: fetch seats, hold seat, fetch updated availability", async () => {
            // Initial seat data
            const initialSeats = [
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
                    status: SEAT_STATUS.AVAILABLE,
                },
            ];

            // Create hold response
            const mockHold = {
                id: "hold-1",
                seatId: "1",
                expirationTime: "2024-01-10T12:00:00Z",
                status: "ACTIVE",
            };

            // Updated seat data after hold
            const updatedSeats = [
                {
                    id: "1",
                    block: "A",
                    category: "Premium",
                    row: "1",
                    number: "1",
                    price: 150.0,
                    status: SEAT_STATUS.HELD,
                },
                {
                    id: "2",
                    block: "A",
                    category: "Premium",
                    row: "1",
                    number: "2",
                    price: 150.0,
                    status: SEAT_STATUS.AVAILABLE,
                },
            ];

            // Updated availability
            const updatedAvailability = [
                {
                    category: "Premium",
                    available: 1,
                    held: 1,
                    sold: 0,
                },
            ];

            // Mock API calls
            api.get
                .mockResolvedValueOnce({
                    data: {
                        concertId: "1",
                        seats: initialSeats,
                        availabilityByCategory: [],
                    },
                })
                .mockResolvedValueOnce({
                    data: {
                        concertId: "1",
                        seats: updatedSeats,
                        availabilityByCategory: updatedAvailability,
                    },
                })
                .mockResolvedValueOnce({
                    data: {
                        concertId: "1",
                        availabilityByCategory: updatedAvailability,
                    },
                });

            api.post.mockResolvedValue({
                data: mockHold,
            });

            // Execute flow
            const fetchedSeats = await fetchConcertSeats("1");
            expect(fetchedSeats).toEqual(initialSeats);
            expect(fetchedSeats[0].status).toBe(SEAT_STATUS.AVAILABLE);

            const hold = await createSeatHold("1");
            expect(hold).toEqual(mockHold);

            const updatedSeatsResult = await fetchConcertSeats("1");
            expect(updatedSeatsResult[0].status).toBe(SEAT_STATUS.HELD);

            const updatedAvailabilityResult = await fetchSeatAvailability("1");
            expect(updatedAvailabilityResult).toEqual(updatedAvailability);
        });

        it("handles concurrent seat hold attempts", async () => {
            const mockHold1 = {
                id: "hold-1",
                seatId: "1",
                expirationTime: "2024-01-10T12:00:00Z",
                status: "ACTIVE",
            };

            const mockError = new Error("Seat already held");
            mockError.response = {
                status: 409,
                data: { message: "Seat is already held by another user" },
            };

            api.post
                .mockResolvedValueOnce({ data: mockHold1 })
                .mockRejectedValueOnce(mockError);

            // First hold succeeds
            const hold1 = await createSeatHold("1");
            expect(hold1).toEqual(mockHold1);

            // Second hold fails (concurrent attempt)
            await expect(createSeatHold("1")).rejects.toThrow();
        });

        it("handles seat hold expiration", async () => {
            const mockSeats = [
                {
                    id: "1",
                    block: "A",
                    category: "Premium",
                    row: "1",
                    number: "1",
                    price: 150.0,
                    status: SEAT_STATUS.HELD,
                },
            ];

            api.get
                .mockResolvedValueOnce({
                    data: {
                        concertId: "1",
                        seats: mockSeats,
                        availabilityByCategory: [],
                    },
                })
                .mockResolvedValueOnce({
                    data: {
                        concertId: "1",
                        seats: [
                            {
                                ...mockSeats[0],
                                status: SEAT_STATUS.AVAILABLE,
                            },
                        ],
                        availabilityByCategory: [],
                    },
                });

            // Fetch seats with held status
            const seats1 = await fetchConcertSeats("1");
            expect(seats1[0].status).toBe(SEAT_STATUS.HELD);

            // Simulate hold expiration and fetch again
            const seats2 = await fetchConcertSeats("1");
            expect(seats2[0].status).toBe(SEAT_STATUS.AVAILABLE);
        });
    });
});
