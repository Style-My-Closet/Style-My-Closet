package com.stylemycloset.notification.event;

public record ClothAttributeChangedEvent(
    Long clothAttributeId,
    String changedAttributeName
) {

}
