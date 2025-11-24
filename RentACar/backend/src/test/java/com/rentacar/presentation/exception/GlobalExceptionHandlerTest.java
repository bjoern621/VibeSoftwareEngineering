package com.rentacar.presentation.exception;

import com.rentacar.domain.exception.VehicleNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleVehicleNotFoundException() {
        VehicleNotFoundException ex = new VehicleNotFoundException(1L);
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleVehicleNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Fahrzeug nicht gefunden", response.getBody().getError());
    }

    @Test
    void handleGenericException() {
        Exception ex = new RuntimeException("Something went wrong");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Interner Serverfehler", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("Something went wrong"));
    }
}
