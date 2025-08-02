package com.stylemycloset.notification.repository;

import com.stylemycloset.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  long countByReceiverId(long receiverId);
}
