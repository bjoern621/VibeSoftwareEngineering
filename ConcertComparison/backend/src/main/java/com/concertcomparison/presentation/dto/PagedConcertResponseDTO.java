package com.concertcomparison.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Paginierte Antwort für Konzertlisten")
public class PagedConcertResponseDTO {

    private List<ConcertListItemDTO> concerts;
    private PageMetadata page;

    public PagedConcertResponseDTO() {}

    public PagedConcertResponseDTO(List<ConcertListItemDTO> concerts, PageMetadata page) {
        this.concerts = concerts;
        this.page = page;
    }

    public List<ConcertListItemDTO> getConcerts() {
        return concerts;
    }

    public PageMetadata getPage() {
        return page;
    }

    @Schema(description = "Metadaten für Pagination")
    public static class PageMetadata {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;

        public PageMetadata() {}

        public PageMetadata(int page, int size, long totalElements, int totalPages) {
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }

        public int getPage() {
            return page;
        }

        public int getSize() {
            return size;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }
    }
}
