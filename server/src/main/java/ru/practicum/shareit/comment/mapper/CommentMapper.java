package ru.practicum.shareit.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "authorName", source = "author.name")
    CommentDto toCommentDto(Comment comment);

    @Mapping(target = "text", source = "commentDto.text")
    @Mapping(target = "item", source = "item")
    @Mapping(target = "author", source = "author")
    @Mapping(target = "id", source = "commentDto.id")
    @Mapping(target = "created", expression = "java(commentDto.getCreated() != null ? commentDto.getCreated() : java.time.LocalDateTime.now())")
    Comment toComment(CommentDto commentDto, Item item, User author);

    @Mapping(target = "text", source = "commentDto.text")
    @Mapping(target = "item", source = "item")
    @Mapping(target = "author", source = "author")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", expression = "java(java.time.LocalDateTime.now())")
    Comment toCommentFromCreate(CommentDto commentDto, Item item, User author);
}
