package io.github.rihyri.til.week02.day02.example.mapper;

import io.github.rihyri.til.week02.day02.example.dto.UserResponse;
import io.github.rihyri.til.week02.day02.example.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "Spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
