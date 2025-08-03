package com.stylemycloset.ootd.entity;

import com.stylemycloset.common.entity.SoftDeletableEntity;
import com.stylemycloset.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "feed_comments")
@Getter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE feed_comments SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class FeedComment extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "feed_comments_seq_gen")
    @SequenceGenerator(name = "feed_comments_seq_gen", sequenceName = "feed_comments_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

}