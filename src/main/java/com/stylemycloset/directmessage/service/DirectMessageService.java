package com.stylemycloset.directmessage.service;

import com.stylemycloset.directmessage.dto.DirectMessageResult;
import com.stylemycloset.directmessage.dto.request.DirectMessageCreateRequest;
import com.stylemycloset.directmessage.dto.request.DirectMessageSearchCondition;
import com.stylemycloset.directmessage.dto.response.DirectMessageResponse;

public interface DirectMessageService {

  DirectMessageResult create(DirectMessageCreateRequest request);

  DirectMessageResponse<DirectMessageResult> getDirectMessageBetweenParticipants(
      DirectMessageSearchCondition searchCondition, Long logInUser
  );

}
