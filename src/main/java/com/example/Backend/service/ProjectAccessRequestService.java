package com.example.Backend.service;

import com.example.Backend.dto.request.CreateProjectAccessRequestRequest;
import com.example.Backend.dto.request.admin.AdminUpdateProjectAccessRequestStatusRequest;
import com.example.Backend.dto.response.ProjectAccessRequestResponse;
import org.springframework.data.domain.Page;

public interface ProjectAccessRequestService {

    /// Tạo một yêu cầu xin truy cập / hợp tác vào project từ phía company
    ProjectAccessRequestResponse createRequest(CreateProjectAccessRequestRequest request, String companyEmail);

    /// Lấy danh sách các yêu cầu mà company hiện tại đã gửi, có phân trang
    Page<ProjectAccessRequestResponse> getMyRequests(String companyEmail, int page, int size);

    /// Lấy chi tiết một yêu cầu của chính company hiện tại
    ProjectAccessRequestResponse getMyRequestDetail(Long requestId, String companyEmail);

    /// Admin lấy toàn bộ danh sách yêu cầu, có phân trang
    Page<ProjectAccessRequestResponse> getAllRequests(int page, int size);

    /// Admin xem chi tiết một yêu cầu bất kỳ theo id
    ProjectAccessRequestResponse getRequestDetailForAdmin(Long requestId);

    /// Admin cập nhật trạng thái của một yêu cầu (ví dụ: pending, approved, rejected)
    ProjectAccessRequestResponse updateRequestStatus(Long requestId, AdminUpdateProjectAccessRequestStatusRequest request);
}