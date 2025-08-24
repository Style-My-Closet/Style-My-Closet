package com.stylemycloset.directmessage.repository.impl;

import com.stylemycloset.directmessage.entity.DirectMessage;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Direction;

public interface DirectMessageRepositoryCustom {

  Slice<DirectMessage> findMessagesBetweenParticipants(
      Long senderId,
      Long receiverId,
      String cursor,
      String idAfter,
      Integer limit,
      String sortBy,
      Direction sortDirection
  );

}
