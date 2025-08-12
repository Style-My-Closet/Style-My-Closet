package com.stylemycloset.directmessage.entity.repository;

import com.stylemycloset.directmessage.entity.Message;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {

  @Query("SELECT m FROM Message m JOIN FETCH m.receiver WHERE m.id = :id")
  Optional<Message> findWithReceiverById(@Param("id") Long id);
}
