package com.stylemycloset.notification.event.domain;

public record ClothAttributeChangedEvent(
    Long clothAttributeId,
    String changedAttributeName
) {

}
