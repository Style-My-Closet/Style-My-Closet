package com.stylemycloset.notification.event;

public record NewClothAttributeEvent(
    Long clothAttributeId,
    String attributeName
) {

}
