package com.tvxargtec.online.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Modelo genérico para respuestas paginadas del API
 */
public class PaginatedResponse<T> {
    
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private List<T> data;
    
    @SerializedName("pagination")
    private Pagination pagination;

    public PaginatedResponse() {
    }

    public PaginatedResponse(boolean success, String message, List<T> data, Pagination pagination) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.pagination = pagination;
    }

    // Getters y Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    /**
     * Clase interna para información de paginación
     */
    public static class Pagination {
        @SerializedName("current_page")
        private int currentPage;
        
        @SerializedName("total_pages")
        private int totalPages;
        
        @SerializedName("per_page")
        private int perPage;
        
        @SerializedName("total_items")
        private int totalItems;
        
        @SerializedName("has_next")
        private boolean hasNext;
        
        @SerializedName("has_previous")
        private boolean hasPrevious;

        public Pagination() {
        }

        public Pagination(int currentPage, int totalPages, int perPage, int totalItems) {
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.perPage = perPage;
            this.totalItems = totalItems;
            this.hasNext = currentPage < totalPages;
            this.hasPrevious = currentPage > 1;
        }

        // Getters
        public int getCurrentPage() {
            return currentPage;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public int getPerPage() {
            return perPage;
        }

        public int getTotalItems() {
            return totalItems;
        }

        public boolean hasNext() {
            return hasNext;
        }

        public boolean hasPrevious() {
            return hasPrevious;
        }

        public int getNextPage() {
            return hasNext ? currentPage + 1 : currentPage;
        }

        public int getPreviousPage() {
            return hasPrevious ? currentPage - 1 : currentPage;
        }
    }
}
