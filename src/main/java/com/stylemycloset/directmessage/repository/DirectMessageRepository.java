package com.stylemycloset.directmessage.repository;

import com.stylemycloset.directmessage.entity.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectMessageRepository
    extends JpaRepository<DirectMessage, Long>, DirectMessageRepositoryCustom {

}
