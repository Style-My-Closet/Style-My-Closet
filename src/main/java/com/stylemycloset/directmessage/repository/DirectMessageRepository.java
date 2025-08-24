package com.stylemycloset.directmessage.repository;

import com.stylemycloset.directmessage.entity.DirectMessage;
import com.stylemycloset.directmessage.repository.impl.DirectMessageRepositoryCustom;
import java.util.Optional;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectMessageRepository
    extends JpaRepository<DirectMessage, Long>, DirectMessageRepositoryCustom {

  @Query("SELECT m FROM DirectMessage m JOIN FETCH m.receiver WHERE m.id = :id")
  Optional<DirectMessage> findWithReceiverById(@Param("id") Long id);

}
