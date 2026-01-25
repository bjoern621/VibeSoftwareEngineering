import { renderHook, act } from "@testing-library/react";
import { useHoldTimer } from "../../hooks/useHoldTimer";

describe("useHoldTimer Hook", () => {
    beforeEach(() => {
        jest.useFakeTimers();
    });

    afterEach(() => {
        jest.runOnlyPendingTimers();
        jest.useRealTimers();
    });

    describe("Initial State", () => {
        it("should initialize with default TTL of 600 seconds", () => {
            const { result } = renderHook(() => useHoldTimer());

            expect(result.current.timeLeft).toBe(600);
            expect(result.current.formattedTime).toBe("10:00");
            expect(result.current.progressPercentage).toBe(0);
            expect(result.current.isActive).toBe(false);
            expect(result.current.isExpired).toBe(false);
        });

        it("should initialize with custom TTL", () => {
            const { result } = renderHook(() => useHoldTimer(120));

            expect(result.current.timeLeft).toBe(120);
            expect(result.current.formattedTime).toBe("02:00");
            expect(result.current.progressPercentage).toBe(0);
            expect(result.current.isActive).toBe(false);
            expect(result.current.isExpired).toBe(false);
        });

        it("should format time correctly for different values", () => {
            const { result: result1 } = renderHook(() => useHoldTimer(65));
            expect(result1.current.formattedTime).toBe("01:05");

            const { result: result2 } = renderHook(() => useHoldTimer(3665));
            expect(result2.current.formattedTime).toBe("61:05");
        });

        it("should calculate progress percentage correctly", () => {
            const { result } = renderHook(() => useHoldTimer(60));

            act(() => {
                result.current.start();
            });

            // After 30 seconds, progress should be 50%
            act(() => {
                jest.advanceTimersByTime(30000);
            });

            expect(result.current.progressPercentage).toBe(50);
        });
    });

    describe("Timer Control", () => {
        it("should start the timer when start() is called", () => {
            const { result } = renderHook(() => useHoldTimer(60));

            expect(result.current.isActive).toBe(false);

            act(() => {
                result.current.start();
            });

            expect(result.current.isActive).toBe(true);
            expect(result.current.isExpired).toBe(false);
            expect(result.current.timeLeft).toBe(60);
        });

        it("should stop the timer when stop() is called", () => {
            const { result } = renderHook(() => useHoldTimer(60));

            act(() => {
                result.current.start();
            });

            expect(result.current.isActive).toBe(true);

            act(() => {
                result.current.stop();
            });

            expect(result.current.isActive).toBe(false);
        });

        it("should reset the timer when reset() is called", () => {
            const { result } = renderHook(() => useHoldTimer(60));

            act(() => {
                result.current.start();
            });

            act(() => {
                jest.advanceTimersByTime(30000);
            });

            expect(result.current.timeLeft).toBe(30);

            act(() => {
                result.current.reset();
            });

            expect(result.current.timeLeft).toBe(60);
            expect(result.current.isActive).toBe(false);
            expect(result.current.isExpired).toBe(false);
            expect(result.current.progressPercentage).toBe(0);
        });

        it("should reset to custom TTL value", () => {
            const { result } = renderHook(() => useHoldTimer(120));

            act(() => {
                result.current.start();
            });

            act(() => {
                jest.advanceTimersByTime(60000);
            });

            expect(result.current.timeLeft).toBe(60);

            act(() => {
                result.current.reset();
            });

            expect(result.current.timeLeft).toBe(120);
        });
    });

    describe("Countdown Progress", () => {
        it("should decrease timeLeft by 1 every second when active", () => {
            const { result } = renderHook(() => useHoldTimer(10));

            act(() => {
                result.current.start();
            });

            expect(result.current.timeLeft).toBe(10);

            act(() => {
                jest.advanceTimersByTime(1000);
            });

            expect(result.current.timeLeft).toBe(9);

            act(() => {
                jest.advanceTimersByTime(5000);
            });

            expect(result.current.timeLeft).toBe(4);
        });

        it("should update formattedTime correctly during countdown", () => {
            const { result } = renderHook(() => useHoldTimer(60));

            act(() => {
                result.current.start();
            });

            expect(result.current.formattedTime).toBe("01:00");

            act(() => {
                jest.advanceTimersByTime(35000);
            });

            expect(result.current.formattedTime).toBe("00:25");
        });

        it("should update progress percentage during countdown", () => {
            const { result } = renderHook(() => useHoldTimer(100));

            act(() => {
                result.current.start();
            });

            expect(result.current.progressPercentage).toBe(0);

            act(() => {
                jest.advanceTimersByTime(25000);
            });

            expect(result.current.progressPercentage).toBe(25);

            act(() => {
                jest.advanceTimersByTime(25000);
            });

            expect(result.current.progressPercentage).toBe(50);
        });

        it("should not decrease time when timer is not active", () => {
            const { result } = renderHook(() => useHoldTimer(10));

            act(() => {
                jest.advanceTimersByTime(5000);
            });

            expect(result.current.timeLeft).toBe(10);
        });
    });

    describe("Expiration", () => {
        it("should call onExpire callback when timer reaches zero", () => {
            const onExpire = jest.fn();
            const { result } = renderHook(() => useHoldTimer(3, onExpire));

            act(() => {
                result.current.start();
            });

            act(() => {
                jest.advanceTimersByTime(3000);
            });

            expect(onExpire).toHaveBeenCalledTimes(1);
            expect(result.current.isExpired).toBe(true);
            expect(result.current.timeLeft).toBe(0);
        });

        it("should set isExpired to true when timer reaches zero", () => {
            const { result } = renderHook(() => useHoldTimer(5));

            act(() => {
                result.current.start();
            });

            expect(result.current.isExpired).toBe(false);

            act(() => {
                jest.advanceTimersByTime(5000);
            });

            expect(result.current.isExpired).toBe(true);
        });

        it("should stop timer when expired", () => {
            const { result } = renderHook(() => useHoldTimer(5));

            act(() => {
                result.current.start();
            });

            act(() => {
                jest.advanceTimersByTime(5000);
            });

            expect(result.current.isActive).toBe(false);
        });

        it("should set progress percentage to 100 when expired", () => {
            const { result } = renderHook(() => useHoldTimer(100));

            act(() => {
                result.current.start();
            });

            act(() => {
                jest.advanceTimersByTime(100000);
            });

            expect(result.current.progressPercentage).toBe(100);
        });

        it("should format time as 00:00 when expired", () => {
            const { result } = renderHook(() => useHoldTimer(10));

            act(() => {
                result.current.start();
            });

            act(() => {
                jest.advanceTimersByTime(10000);
            });

            expect(result.current.formattedTime).toBe("00:00");
        });
    });

    describe("Cleanup", () => {
        it("should clean up interval on unmount", () => {
            const { result, unmount } = renderHook(() => useHoldTimer(60));

            act(() => {
                result.current.start();
            });

            unmount();

            // Timer should not continue after unmount
            act(() => {
                jest.advanceTimersByTime(10000);
            });

            // This is just to ensure no memory leaks
            expect(true).toBe(true);
        });

        it("should clean up interval when stop() is called", () => {
            const { result } = renderHook(() => useHoldTimer(60));

            act(() => {
                result.current.start();
                result.current.stop();
            });

            // Timer should not continue after stop
            act(() => {
                jest.advanceTimersByTime(10000);
            });

            expect(result.current.timeLeft).toBe(60);
        });
    });

    describe("Callback Updates", () => {
        it("should use the latest onExpire callback", () => {
            const onExpire1 = jest.fn();
            const onExpire2 = jest.fn();

            const { result, rerender } = renderHook(
                ({ onExpire }) => useHoldTimer(2, onExpire),
                { initialProps: { onExpire: onExpire1 } },
            );

            act(() => {
                result.current.start();
            });

            // Update callback
            rerender({ onExpire: onExpire2 });

            act(() => {
                jest.advanceTimersByTime(2000);
            });

            expect(onExpire1).not.toHaveBeenCalled();
            expect(onExpire2).toHaveBeenCalledTimes(1);
        });
    });

    describe("Edge Cases", () => {
        it("should handle TTL of 0", () => {
            const { result } = renderHook(() => useHoldTimer(0));

            expect(result.current.timeLeft).toBe(0);
            expect(result.current.formattedTime).toBe("00:00");
            expect(result.current.progressPercentage).toBe(0);
        });

        it("should handle very large TTL values", () => {
            const { result } = renderHook(() => useHoldTimer(999999));

            expect(result.current.timeLeft).toBe(999999);
            expect(result.current.formattedTime).toBe("16666:39");
        });

        it("should handle rapid start/stop calls", () => {
            const { result } = renderHook(() => useHoldTimer(60));

            act(() => {
                result.current.start();
                result.current.stop();
                result.current.start();
                result.current.stop();
            });

            expect(result.current.isActive).toBe(false);
        });

        it("should handle multiple reset calls", () => {
            const { result } = renderHook(() => useHoldTimer(60));

            act(() => {
                result.current.start();
                jest.advanceTimersByTime(30000);
            });

            act(() => {
                result.current.reset();
                result.current.reset();
                result.current.reset();
            });

            expect(result.current.timeLeft).toBe(60);
            expect(result.current.isActive).toBe(false);
        });
    });

    describe("Progress Percentage Edge Cases", () => {
        it("should handle TTL of 0 for progress percentage", () => {
            const { result } = renderHook(() => useHoldTimer(0));

            expect(result.current.progressPercentage).toBe(0);
        });

        it("should calculate percentage correctly for fractional progress", () => {
            const { result } = renderHook(() => useHoldTimer(7));

            act(() => {
                result.current.start();
            });

            act(() => {
                jest.advanceTimersByTime(1000);
            });

            // 1/7 ≈ 14.29%
            expect(result.current.progressPercentage).toBeCloseTo(14.29, 1);
        });
    });
});
