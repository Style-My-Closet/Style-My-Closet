package com.stylemycloset.common.repository;

import com.stylemycloset.clothes.entity.attribute.ClothesAttributeDefinition;
import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.clothes.repository.attribute.cursor.ClothesAttributeDefinitionField;
import com.stylemycloset.clothes.repository.clothes.cursor.ClothesField;
import com.stylemycloset.directmessage.entity.DirectMessage;
import com.stylemycloset.directmessage.repository.cursor.DirectMessageField;
import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.repository.cursor.FollowCursorField;
import java.util.function.Function;
import org.springframework.data.domain.Slice;

public record NextCursorInfo(
    String nextCursor,
    String nextIdAfter
) {

  public static NextCursorInfo clothesCursor(Slice<Clothes> slice, String sortBy) {
    return extractNextCursorInfo(slice, sortBy, ClothesField::resolveStrategy, Clothes::getId);
  }

  public static NextCursorInfo followCursor(Slice<Follow> slice, String sortBy) {
    return extractNextCursorInfo(slice, sortBy, FollowCursorField::resolveStrategy, Follow::getId);
  }

  public static NextCursorInfo attributeDefinitionCursor(
      Slice<ClothesAttributeDefinition> slice,
      String sortBy
  ) {
    return extractNextCursorInfo(slice, sortBy, ClothesAttributeDefinitionField::resolveStrategy,
        ClothesAttributeDefinition::getId);
  }

  public static NextCursorInfo directMessageCursor(
      Slice<DirectMessage> slice,
      String sortBy
  ) {
    return extractNextCursorInfo(slice, sortBy, DirectMessageField::resolveStrategy,
        DirectMessage::getId);
  }

  private static <T> NextCursorInfo extractNextCursorInfo(
      Slice<T> slice,
      String sortBy,
      Function<String, CursorStrategy<?, T>> strategyResolver,
      Function<T, ?> idExtractor
  ) {
    if (sortBy == null || sortBy.isBlank() || !slice.hasNext() || slice.getContent().isEmpty()) {
      return new NextCursorInfo(null, null);
    }
    T last = slice.getContent().get(slice.getContent().size() - 1);
    CursorStrategy<?, T> strategy = strategyResolver.apply(sortBy);
    return new NextCursorInfo(
        String.valueOf(strategy.extract(last)),
        String.valueOf(idExtractor.apply(last))
    );
  }

}