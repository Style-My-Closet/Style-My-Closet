package com.stylemycloset.cloth.dto;

public enum SortDirection {
    DESCENDING("desc"),
    ASCENDING("asc");
    private String value;
    SortDirection(String value) {
        this.value = value;
    }
}
