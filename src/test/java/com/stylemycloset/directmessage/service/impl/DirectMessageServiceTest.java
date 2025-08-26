package com.stylemycloset.directmessage.service.impl;

import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.directmessage.dto.DirectMessageResult;
import com.stylemycloset.directmessage.dto.request.DirectMessageSearchCondition;
import com.stylemycloset.directmessage.dto.response.DirectMessageResponse;
import com.stylemycloset.directmessage.entity.DirectMessage;
import com.stylemycloset.directmessage.entity.QDirectMessage;
import com.stylemycloset.directmessage.repository.DirectMessageRepository;
import com.stylemycloset.directmessage.service.DirectMessageService;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.hibernate.query.SortDirection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;

class DirectMessageServiceTest extends IntegrationTestSupport {

  @Autowired
  private DirectMessageService directMessageService;
  @Autowired
  private DirectMessageRepository directMessageRepository;
  @Autowired
  private UserRepository userRepository;

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

  @DisplayName("DM 목록을 첫 페이지 이후로 조회하면, 이전 페이지와 중복 없이 이어진다")
  @Test
  void cursorPagination_nextPage_noOverlap() {
    // given
    User viewer = save("viewer", "v@ex.com");
    User partner = save("partner", "p@ex.com");

    DirectMessage olderMessage = save(viewer, partner, "older");
    DirectMessage newerMessage = save(partner, viewer, "newer");

    // 첫 페이지: limit=1, 최신 1건
    DirectMessageResponse<DirectMessageResult> firstPage =
        directMessageService.getDirectMessageBetweenParticipants(
            new DirectMessageSearchCondition(
                partner.getId(),
                null,
                null,
                1,
                QDirectMessage.directMessage.createdAt.getMetadata().getName(),
                Direction.DESC
            ),
            viewer.getId()
        );

    // when: 두 번째 페이지 - page1의 nextCursor/nextIdAfter 사용
    DirectMessageResponse<DirectMessageResult> secondPage =
        directMessageService.getDirectMessageBetweenParticipants(
            new DirectMessageSearchCondition(
                partner.getId(),
                firstPage.nextCursor(),
                firstPage.nextIdAfter(),
                1,
                QDirectMessage.directMessage.createdAt.getMetadata().getName(),
                Direction.DESC
            ),
            viewer.getId()
        );

    // then
    SoftAssertions.assertSoftly(softly -> {
      // firstPage
      softly.assertThat(firstPage.hasNext()).isTrue();
      softly.assertThat(firstPage.data())
          .extracting(DirectMessageResult::id)
          .containsExactly(newerMessage.getId());

      // secondPage
      softly.assertThat(secondPage.hasNext()).isFalse();
      softly.assertThat(secondPage.data())
          .extracting(DirectMessageResult::id)
          .containsExactly(olderMessage.getId());

      // 중복체크
      List<Long> page1Ids = firstPage.data().stream().map(DirectMessageResult::id).toList();
      List<Long> page2Ids = secondPage.data().stream().map(DirectMessageResult::id).toList();
      softly.assertThat(page1Ids).doesNotContainAnyElementsOf(page2Ids);

      softly.assertThat(firstPage.sortDirection()).isEqualTo("DESC");
      softly.assertThat(firstPage.sortBy())
          .isEqualTo(QDirectMessage.directMessage.createdAt.getMetadata().getName());
    });
  }

  @DisplayName("양방향 메시지가 섞여 있어도 동일 대화로 묶여 최신순으로 반환된다")
  @Test
  void mixedDirections_stillOneConversation_sortedByCreatedAtDesc() {
    // given
    User viewer = save("viewer", "v@ex.com");
    User partner = save("partner", "p@ex.com");

    DirectMessage messageViewerToPartner = save(viewer, partner, "m1");
    DirectMessage messagePartnerToViewer = save(partner, viewer, "m2");
    DirectMessage messageViewerToPartnerLast = save(viewer, partner, "m3");

    // when
    DirectMessageResponse<DirectMessageResult> result =
        directMessageService.getDirectMessageBetweenParticipants(
            new DirectMessageSearchCondition(
                partner.getId(),
                null,
                null,
                10,
                QDirectMessage.directMessage.createdAt.getMetadata().getName(),
                Direction.DESC
            ),
            viewer.getId()
        );

    // then
    Assertions.assertThat(result.data())
        .extracting(DirectMessageResult::id)
        .containsExactly(
            messageViewerToPartnerLast.getId(),
            messagePartnerToViewer.getId(),
            messageViewerToPartner.getId()
        );
  }

}