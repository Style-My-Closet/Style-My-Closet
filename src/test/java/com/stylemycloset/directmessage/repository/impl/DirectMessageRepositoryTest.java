package com.stylemycloset.directmessage.repository.impl;


import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.directmessage.entity.DirectMessage;
import com.stylemycloset.directmessage.entity.QDirectMessage;
import com.stylemycloset.directmessage.repository.DirectMessageRepository;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

class DirectMessageRepositoryTest extends IntegrationTestSupport {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private DirectMessageRepository directMessageRepository;

  @AfterEach
  void tearDown() {
    directMessageRepository.deleteAllInBatch();
    userRepository.deleteAllInBatch();
  }

  private DirectMessage save(User sender, User receiver, String content) {
    DirectMessage dm = new DirectMessage(sender, receiver, content);
    return directMessageRepository.save(dm);
  }

  private User save(String name, String email) {
    return userRepository.save(new User(name, email, "p"));
  }

  @DisplayName("대화 상대간 대화 내역을 조회한다[기본 정렬은 createdAt DESC, id DESC]")
  @Test
  void defaultSorting_desc_latestFirst() {
    // given
    User userA = save("alice", "a@ex.com");
    User userB = save("bob", "b@ex.com");
    User userC = save("charlie", "c@ex.com");

    DirectMessage directMessageAtoB = save(userA, userB, "m1");
    DirectMessage directMessageBtoC = save(userB, userA, "m2");
    DirectMessage directMessageAtoBLast = save(userA, userB, "m3");
    DirectMessage directMessageAtoC = save(userA, userC, "excludeMessage");

    // when
    Slice<DirectMessage> slice = directMessageRepository.findMessagesBetweenParticipants(
        userA.getId(), userB.getId(),
        null, null,
        2,
        null,
        "DESC"
    );

    // then
    Sort.Order order = slice.getPageable().getSort().iterator().next();
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(order.getProperty())
          .isEqualTo(QDirectMessage.directMessage.createdAt.getMetadata().getName());
      softly.assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
      softly.assertThat(slice.getContent())
          .extracting(DirectMessage::getId)
          .containsExactly(directMessageAtoBLast.getId(), directMessageBtoC.getId());
      softly.assertThat(slice.hasNext()).isTrue();
    });
  }

  @DisplayName("참여자 순서에 상관없이 대화 내역을 가져옵니다.(sender, receiver 순서를 바꿔도 동일 결과)")
  @Test
  void orderIndependent_participants() {
    // given
    User userA = save("alice", "a@ex.com");
    User userB = save("bob", "b@ex.com");

    DirectMessage directMessageAtoB = save(userA, userB, "hello");
    DirectMessage directMessageBtoA = save(userB, userA, "hi");

    Slice<DirectMessage> firstPage = directMessageRepository.findMessagesBetweenParticipants(
        userA.getId(), userB.getId(), null, null, 10,
        QDirectMessage.directMessage.createdAt.getMetadata().getName(), "DESC"
    );
    Slice<DirectMessage> secondPage = directMessageRepository.findMessagesBetweenParticipants(
        userB.getId(), userA.getId(), null, null, 10,
        QDirectMessage.directMessage.createdAt.getMetadata().getName(), "DESC"
    );

    Assertions.assertThat(firstPage.getContent()).extracting(DirectMessage::getId)
        .containsExactlyElementsOf(secondPage.getContent().stream().map(DirectMessage::getId).toList());
  }

  @DisplayName("생성 날짜 기준 ASC 정렬도 동작한다 (과거→최신)")
  @Test
  void ascSorting_works() {
    User userA = save("alice", "a@ex.com");
    User userB = save("bob", "b@ex.com");

    DirectMessage firstMessage = save(userA, userB, "1");
    DirectMessage secondMessage = save(userB, userA, "2");
    DirectMessage thirdMessage = save(userA, userB, "3");

    Slice<DirectMessage> messages = directMessageRepository.findMessagesBetweenParticipants(
        userA.getId(), userB.getId(),
        null, null, 10,
        QDirectMessage.directMessage.createdAt.getMetadata().getName(),
        "ASC"
    );

    Assertions.assertThat(messages.getContent()).extracting(DirectMessage::getId)
        .containsExactly(firstMessage.getId(), secondMessage.getId(), thirdMessage.getId());
  }

  @DisplayName("soft delete된 메시지는 조회시 제외된다")
  @Test
  void softDelete_messageFilteredOut() {
    User userA = save("alice", "a@ex.com");
    User userB = save("bob", "b@ex.com");

    DirectMessage activeMessage = save(userA, userB, "alive");
    DirectMessage deletedMessage = save(userA, userB, "gone");
    deletedMessage.softDelete();
    directMessageRepository.save(deletedMessage);

    Slice<DirectMessage> messages = directMessageRepository.findMessagesBetweenParticipants(
        userA.getId(), userB.getId(), null, null, 10,
        QDirectMessage.directMessage.createdAt.getMetadata().getName(), "DESC"
    );

    Assertions.assertThat(messages.getContent()).extracting(DirectMessage::getId)
        .containsExactly(activeMessage.getId());
  }

}