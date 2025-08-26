package com.stylemycloset.follow.repository.impl.cursorstrategy;

import com.querydsl.core.types.Path;
import com.stylemycloset.common.repository.CursorStrategy;
import com.stylemycloset.follow.entity.QFollow;
import com.stylemycloset.follow.repository.cursor.FollowCursorField;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class FollowCursorFieldTest {

  @DisplayName("사용자가 존재하지 않는 정렬 기준으로 요청하면, 오류가 발생한다")
  @Test
  void invalidSortBy_throwsException() {
    // given
    String nonExistingField = "nonExistingField";

    // then
    Assertions.assertThatThrownBy(() -> FollowCursorField.resolveStrategy(nonExistingField)
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @DisplayName("사용자가 정렬 기준을 지정하지 않으면, 기본 생성일 기준으로 정렬된다")
  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void nullOrBlankSortBy_returnsCreatedAtStrategy(String sortBy) {
    // given & when
    CursorStrategy<?, ?> strategy = FollowCursorField.resolveStrategy(sortBy);

    // then
    Assertions.assertThat(strategy.path().getMetadata().getName())
        .isEqualTo(QFollow.follow.createdAt.getMetadata().getName());
  }

  @DisplayName("팔로우 목록 정렬 기준 필드명이 올바른 커서 전략으로 매핑된다")
  @ParameterizedTest
  @MethodSource("resolveFollowField")
  void strategyMappingTest(String sortBy, Path<?> path) {
    // when
    CursorStrategy<?, ?> cursorStrategy = FollowCursorField.resolveStrategy(sortBy);

    // then
    Assertions.assertThat(cursorStrategy.path()).isEqualTo(path);
  }

  static Stream<Arguments> resolveFollowField() {
    return Stream.of(
        Arguments.of(
            QFollow.follow.id.getMetadata().getName(),
            QFollow.follow.id
        ),
        Arguments.of(
            QFollow.follow.createdAt.getMetadata().getName(),
            QFollow.follow.createdAt
        ),
        Arguments.of(
            QFollow.follow.followedAt.getMetadata().getName(),
            QFollow.follow.followedAt
        )
    );
  }

}