package com.example.Backend.mapper;

import com.example.Backend.dto.request.CompanyUpdateRequest;
import com.example.Backend.dto.request.RegisterRequest;
import com.example.Backend.dto.response.CompanyResponse;
import com.example.Backend.entity.Company;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "taxCode", ignore = true)
    @Mapping(target = "website", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "verifiedAt", ignore = true)
    @Mapping(target = "verifiedBy", ignore = true)
    @Mapping(target = "isVerified", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(source = "fullName", target = "companyName")
    Company toEntity(RegisterRequest request);

    @Mapping(source = "user.email", target = "email")
    CompanyResponse toResponse(Company company);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "verifiedBy", ignore = true)
    @Mapping(target = "verifiedAt", ignore = true)
    void updateCompanyFromRequest(CompanyUpdateRequest request, @MappingTarget Company company);

    List<CompanyResponse> toResponseList(List<Company> companies);
}
