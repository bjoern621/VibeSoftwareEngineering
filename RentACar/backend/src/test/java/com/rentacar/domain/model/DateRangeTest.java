package com.rentacar.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für DateRange Value Object.
 */
class DateRangeTest {
    
    @Test
    void shouldCreateValidDateRange() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);
        
        // Act
        DateRange dateRange = new DateRange(start, end);
        
        // Assert
        assertNotNull(dateRange);
        assertEquals(start, dateRange.getStartDateTime());
        assertEquals(end, dateRange.getEndDateTime());
    }
    
    @Test
    void shouldCalculateDaysCorrectly() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(48); // 2 Tage
        
        // Act
        DateRange dateRange = new DateRange(start, end);
        
        // Assert
        assertEquals(2, dateRange.getDays());
    }
    
    @Test
    void shouldRoundUpPartialDays() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(25); // 1 Tag + 1 Stunde = aufgerundet 2 Tage
        
        // Act
        DateRange dateRange = new DateRange(start, end);
        
        // Assert
        assertEquals(2, dateRange.getDays());
    }
    
    @Test
    void shouldReturnAtLeastOneDay() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(1); // Weniger als 1 Tag
        
        // Act
        DateRange dateRange = new DateRange(start, end);
        
        // Assert
        assertEquals(1, dateRange.getDays());
    }
    
    @Test
    void shouldThrowExceptionWhenStartIsNull() {
        // Arrange
        LocalDateTime end = LocalDateTime.now().plusDays(3);
        
        // Act & Assert
        com.rentacar.domain.exception.InvalidDateRangeException exception = assertThrows(
            com.rentacar.domain.exception.InvalidDateRangeException.class,
            () -> new DateRange(null, end)
        );
        assertTrue(exception.getMessage().contains("Startdatum darf nicht null sein"));
    }
    
    @Test
    void shouldThrowExceptionWhenEndIsNull() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        
        // Act & Assert
        com.rentacar.domain.exception.InvalidDateRangeException exception = assertThrows(
            com.rentacar.domain.exception.InvalidDateRangeException.class,
            () -> new DateRange(start, null)
        );
        assertTrue(exception.getMessage().contains("Enddatum darf nicht null sein"));
    }
    
    @Test
    void shouldThrowExceptionWhenStartIsAfterEnd() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        
        // Act & Assert
        com.rentacar.domain.exception.InvalidDateRangeException exception = assertThrows(
            com.rentacar.domain.exception.InvalidDateRangeException.class,
            () -> new DateRange(start, end)
        );
        assertTrue(exception.getMessage().contains("Startdatum muss vor dem Enddatum liegen"));
    }
    
    @Test
    void shouldThrowExceptionWhenStartIsInPast() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        
        // Act & Assert
        com.rentacar.domain.exception.InvalidDateRangeException exception = assertThrows(
            com.rentacar.domain.exception.InvalidDateRangeException.class,
            () -> new DateRange(start, end)
        );
        assertTrue(exception.getMessage().contains("Startdatum darf nicht in der Vergangenheit liegen"));
    }
    
    @Test
    void shouldDetectOverlappingDateRanges() {
        // Arrange
        LocalDateTime start1 = LocalDateTime.now().plusDays(1);
        LocalDateTime end1 = LocalDateTime.now().plusDays(5);
        DateRange range1 = new DateRange(start1, end1);
        
        LocalDateTime start2 = LocalDateTime.now().plusDays(3);
        LocalDateTime end2 = LocalDateTime.now().plusDays(7);
        DateRange range2 = new DateRange(start2, end2);
        
        // Act & Assert
        assertTrue(range1.overlaps(range2));
        assertTrue(range2.overlaps(range1));
    }
    
    @Test
    void shouldDetectNonOverlappingDateRanges() {
        // Arrange
        LocalDateTime start1 = LocalDateTime.now().plusDays(1);
        LocalDateTime end1 = LocalDateTime.now().plusDays(3);
        DateRange range1 = new DateRange(start1, end1);
        
        LocalDateTime start2 = LocalDateTime.now().plusDays(5);
        LocalDateTime end2 = LocalDateTime.now().plusDays(7);
        DateRange range2 = new DateRange(start2, end2);
        
        // Act & Assert
        assertFalse(range1.overlaps(range2));
        assertFalse(range2.overlaps(range1));
    }
    
    @Test
    void shouldDetectDateTimeWithinRange() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(5);
        DateRange range = new DateRange(start, end);
        
        LocalDateTime dateInRange = LocalDateTime.now().plusDays(3);
        
        // Act & Assert
        assertTrue(range.contains(dateInRange));
    }
    
    @Test
    void shouldDetectDateTimeOutsideRange() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(5);
        DateRange range = new DateRange(start, end);
        
        LocalDateTime dateOutsideRange = LocalDateTime.now().plusDays(7);
        
        // Act & Assert
        assertFalse(range.contains(dateOutsideRange));
    }
    
    @Test
    void shouldBeEqualWhenSameDates() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);
        DateRange range1 = new DateRange(start, end);
        DateRange range2 = new DateRange(start, end);
        
        // Act & Assert
        assertEquals(range1, range2);
        assertEquals(range1.hashCode(), range2.hashCode());
    }
}
