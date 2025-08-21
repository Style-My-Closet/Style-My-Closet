package com.stylemycloset.ootd.mapper;

import com.stylemycloset.ootd.dto.CommentDto;
import com.stylemycloset.ootd.entity.FeedComment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    private final FeedMapper feedMapper;

    public CommentMapper(FeedMapper feedMapper) {
        this.feedMapper = feedMapper;
    }

    public CommentDto toDto(FeedComment comment) {
        if (comment == null) {
            return null;
        }

        return new CommentDto(
            comment.getId(),
            comment.getCreatedAt(),
            comment.getFeed().getId(),
            feedMapper.toAuthorDto(comment.getAuthor()),
            comment.getContent()
        );
    }

    public List<CommentDto> toDtoList(List<FeedComment> comments) {
        if (comments == null) {
            return List.of();
        }

        return comments.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
}
