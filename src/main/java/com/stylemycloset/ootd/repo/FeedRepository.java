package com.stylemycloset.ootd.repo;

import com.stylemycloset.ootd.entity.Feed;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedRepository extends JpaRepository<Feed, Long>, FeedRepositoryCustom {

  @Query("SELECT f FROM Feed f JOIN FETCH f.author WHERE f.id = :id")
  Optional<Feed> findWithUserById(@Param("id") Long id);
}
