package com.example.Backend.mapper;

import com.example.Backend.dto.User.AdminUserRequest;
import com.example.Backend.dto.User.AdminUserUpdateRequest;
import com.example.Backend.dto.User.UserReponse;
import com.example.Backend.dto.User.UserRequest;
import com.example.Backend.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {


    User toEntity(AdminUserRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(AdminUserUpdateRequest dto, @MappingTarget User entity);

    UserReponse toResponse(User user);

    List<UserReponse> toResponseList(List<User> users);
}