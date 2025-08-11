package com.stylemycloset.cloth.dto;

public enum SortDirection {
    DESCENDING("desc"),
    ASCENDING("asc");
    
    private final String value;
    
    SortDirection(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static SortDirection fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Unknown sort direction: null");
        }
        for (SortDirection direction : values()) {
            if (direction.value.equalsIgnoreCase(value) || direction.name().equalsIgnoreCase(value)) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Unknown sort direction: " + value);
    }
}
