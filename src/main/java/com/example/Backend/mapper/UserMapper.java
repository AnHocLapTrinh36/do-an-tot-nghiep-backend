package com.example.Backend.mapper;

import com.example.Backend.dto.request.RegisterRequest;
import com.example.Backend.dto.response.RegisterResponse;
import com.example.Backend.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {


    User toEntity(RegisterRequest request);
    RegisterResponse toDto(User user);

//    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
//    void updateEntityFromDto(AdminUserUpdateRequest dto, @MappingTarget User entity);
//    UserReponse toResponse(User user);

    List<RegisterResponse> toResponseList(List<User> users);
}