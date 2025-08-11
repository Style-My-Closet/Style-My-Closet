package com.stylemycloset.directmessage.service.impl;

import com.stylemycloset.directmessage.dto.DirectMessageResult;
import com.stylemycloset.directmessage.dto.request.DirectMessageCreateRequest;
import com.stylemycloset.directmessage.dto.request.DirectMessageSearchCondition;
import com.stylemycloset.directmessage.dto.response.DirectMessageResponse;
import com.stylemycloset.directmessage.entity.DirectMessage;
import com.stylemycloset.directmessage.exception.DirectMessageSelfForbiddenException;
import com.stylemycloset.directmessage.mapper.DirectMessageMapper;
import com.stylemycloset.directmessage.repository.DirectMessageRepository;
import com.stylemycloset.directmessage.service.DirectMessageService;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.exception.UserNotFoundException;
import com.stylemycloset.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DirectMessageServiceImpl implements DirectMessageService {

  private final DirectMessageRepository directMessageRepository;
  private final UserRepository userRepository;
  private final DirectMessageMapper directMessageMapper;

  @Transactional
  @Override
  public DirectMessageResult create(DirectMessageCreateRequest request) {
    validateSelfSentMessage(request);

    User sender = getUser(request.senderId());
    User receiver = getUser(request.receiverId());
    DirectMessage directMessage = directMessageRepository.save(new DirectMessage(
        sender, receiver, request.content()
    ));

    return directMessageMapper.toResult(directMessage);
  }

  @Transactional(readOnly = true)
  @Override
  public DirectMessageResponse<DirectMessageResult> getDirectMessageBetweenParticipants(
      DirectMessageSearchCondition searchCondition,
      Long logInUser
  ) {
    Slice<DirectMessage> messages = directMessageRepository.findMessagesBetweenParticipants(
        searchCondition.userId(),
        logInUser,
        searchCondition.cursor(),
        searchCondition.idAfter(),
        searchCondition.limit(),
        searchCondition.sortBy(),
        searchCondition.sortDirection()
    );
    return directMessageMapper.toMessageResponse(messages);
  }

  private void validateSelfSentMessage(DirectMessageCreateRequest request) {
    if (request.senderId().equals(request.receiverId())) {
      throw new DirectMessageSelfForbiddenException();
    }
  }

  private User getUser(Long request) {
    return userRepository.findById(request)
        .orElseThrow(UserNotFoundException::new);
  }

}
