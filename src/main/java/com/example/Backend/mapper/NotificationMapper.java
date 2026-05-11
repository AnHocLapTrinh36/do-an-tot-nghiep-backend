package com.example.Backend.mapper;

import com.example.Backend.dto.response.NotificationResponse;
import com.example.Backend.entity.Notification;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {


    NotificationResponse toResponse(Notification notification);

    List<NotificationResponse> toResponseList(List<Notification> notifications);

}