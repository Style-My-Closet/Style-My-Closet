package com.stylemycloset.notification.event.domain;

public record NewClothAttributeEvent(
    Long clothAttributeId,
    String attributeName
) {

}
