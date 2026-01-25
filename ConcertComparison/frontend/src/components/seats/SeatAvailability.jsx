import useSeatUpdatesSSE from './SeatUpdatesSSE';

const SeatAvailability = ({ concertId }) => {
    const { seatStatusById, error } = useSeatUpdatesSSE(concertId);

    const entries = Object.entries(seatStatusById);

    return (
        <div className="seat-availability">
            {error && <p className="error">{error}</p>}

            {entries.length === 0 ? (
                <p>No updates yet.</p>
            ) : (
                <ul>
                    {entries.map(([seatId, status]) => (
                        <li key={seatId}>
                            Seat {seatId}: {status}
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default SeatAvailability;
