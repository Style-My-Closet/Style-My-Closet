package com.stylemycloset.notification.event.domain;

public record WeatherAlertEvent(
    Long receiverId,
    Long weatherId,
    String message
) {

}
