package com.stylemycloset.user.repository;

import static com.stylemycloset.user.entity.QUser.user;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stylemycloset.user.dto.request.UserPageRequest;
import com.stylemycloset.user.entity.Role;
import com.stylemycloset.user.entity.User;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<User> findUsersByCursor(UserPageRequest request) {

    return queryFactory
        .selectFrom(user)
        .where(
            cursorCondition(request),
            filterByEmail(request.emailLike()),
            filterByRole(request.roleEqual()),
            filterByLocked(request.locked())
        )
        .orderBy(createOrderSpecifiers(request))
        .limit(request.limit() + 1)
        .fetch();
  }

  @Override
  public Integer countByFilter(UserPageRequest request) {
    Integer count = queryFactory
        .select(user.count().intValue())
        .from(user)
        .where(
            filterByEmail(request.emailLike()),
            filterByRole(request.roleEqual()),
            filterByLocked(request.locked())
        )
        .fetchOne();
    return count != null ? count : 0;
  }

  private BooleanExpression filterByEmail(String email) {
    if (StringUtils.hasText(email)) {
      return user.email.like("%" + email + "%");
    }
    return null;
  }

  private BooleanExpression filterByRole(Role role) {
    if (role != null) {
      return user.role.eq(role);
    }
    return null;
  }

  private BooleanExpression filterByLocked(Boolean locked) {
    if (locked != null) {
      return user.locked.eq(locked);
    }
    return null;
  }

  private OrderSpecifier<?>[] createOrderSpecifiers(UserPageRequest request) {
    // 삼항 연산자로 sortDirection이 ASCENDING일 경우 Order.ASC 아닐경우 Order.DESC DESC가 디폴트
    Order direction =
        "ASCENDING".equalsIgnoreCase(request.sortDirection()) ? Order.ASC : Order.DESC;

    // <?> 와일드 카드로 지정하여 각각 다른 타입을 받을 수 있게 설계
    OrderSpecifier<?> primaryOrder;

    // 동적으로 설계하여 무슨 값을 받아도 구현 가능하게 설계
    switch (request.sortBy()) {
      case "definitionName":
        primaryOrder = new OrderSpecifier<>(direction, user.name);
        break;
      case "email":
        primaryOrder = new OrderSpecifier<>(direction, user.email);
        break;
      case "createdAt":
        primaryOrder = new OrderSpecifier<>(direction, user.createdAt);
        break;
      default:
        // 기본 값
        primaryOrder = new OrderSpecifier<>(Order.DESC, user.email);
    }

    // 2차 정렬 조건(혹시 name이 겹칠 경우 페이지네이션 될때마다 값이 뒤죽박죽 될 수도 있음)
    OrderSpecifier<?> tieBreaker = new OrderSpecifier<>(direction, user.id);

    return new OrderSpecifier[]{primaryOrder, tieBreaker};
  }

  private BooleanExpression cursorCondition(UserPageRequest request) {
    // 첫 페이지
    if (request.cursor() == null || request.idAfter() == null) {
      return null;
    }

    // 첫 페이지 이후
    String direction =
        "ASCENDING".equals(request.sortDirection()) ? "ASCENDING" : "DESCENDING";

    return switch (request.sortBy()) {
      case "definitionName" -> {
        String cursorValue = request.cursor();
        if (direction.equals("ASCENDING")) {
          yield user.name.gt(cursorValue) //커서보다 user.name이 더 높은 값 불러오기 만약 겹치면 id로 비교
              .or(user.name.eq(cursorValue).and(user.id.gt(request.idAfter())));
        } else {
          yield user.name.lt(cursorValue) //커서보다 user.name이 더 낮은 값 불러오기 만약 겹치면 id로 비교
              .or(user.name.eq(cursorValue).and(user.id.lt(request.idAfter())));
        }
      }
      case "createdAt" -> {
        Instant cursorValue = Instant.parse(request.cursor());
        if (direction.equals("ASCENDING")) {
          yield user.createdAt.gt(cursorValue)//커서보다 user.createdAt이 더 높은 값 불러오기 만약 겹치면 id로 비교
              .or(user.createdAt.eq(cursorValue).and(user.id.gt(request.idAfter())));
        } else {
          yield user.createdAt.lt(cursorValue)//커서보다 user.createdAt이 더 낮은 값 불러오기 만약 겹치면 id로 비교
              .or(user.createdAt.eq(cursorValue).and(user.id.lt(request.idAfter())));
        }
      }
      default -> {//디폴트는 email로 정렬
        String cursorValue = request.cursor();
        if (direction.equals("ASCENDING")) {
          yield user.email.gt(cursorValue)//커서보다 user.email이 더 높은 값 불러오기 만약 겹치면 id로 비교
              .or(user.email.eq(cursorValue).and(user.id.gt(request.idAfter())));
        } else {
          yield user.email.lt(cursorValue)//커서보다 user.email이 더 낮은 값 불러오기 만약 겹치면 id로 비교
              .or(user.email.eq(cursorValue).and(user.id.lt(request.idAfter())));
        }
      }

    };
  }
}
