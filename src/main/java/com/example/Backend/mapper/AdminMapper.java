package com.example.Backend.mapper;


import com.example.Backend.dto.request.RegisterRequest;
import com.example.Backend.dto.response.AdminResponse;
import com.example.Backend.entity.Admin;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AdminMapper {

    // MapStruct sẽ tự động map các trường cùng tên (id, adminCode, position, phone).
    // Riêng email và fullName nằm trong bảng User, nên ta phải chỉ định rõ nguồn (source).
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.fullName", target = "fullName")
    AdminResponse toResponse(Admin admin);

    /// Map từ RegisterRequest sang Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "adminCode", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "phone", ignore = true)
    Admin toEntity(RegisterRequest request);
}
