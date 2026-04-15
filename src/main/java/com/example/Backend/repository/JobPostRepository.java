package com.example.Backend.repository;

import com.example.Backend.entity.JobPost;
import com.example.Backend.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostRepository extends JpaRepository<JobPost, Long> {
    // lấy tin cho sinh viên: phải sau ngày bắt đầu, trước ngày kết thúc và trạng approved
    @Query("SELECT j FROM JobPost j WHERE j.status='APPROVED' AND j.endDay >=CURRENT_TIMESTAMP AND j.startDay <= CURRENT_TIMESTAMP")
    List<JobPost> findActiveJobs();

    List<JobPost> findByCompanyUserEmail(String email);

    List<JobPost> findByStatus(JobStatus status);

    @Query("""
                SELECT j FROM JobPost j 
                WHERE j.status = 'APPROVED' 
                AND j.endDay >= CURRENT_TIMESTAMP 
                AND j.startDay <= CURRENT_TIMESTAMP
                AND LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))
            """)
    List<JobPost> findByTitleContainingIgnoreCase(String title);

    @Query("""
                SELECT j FROM JobPost j
                WHERE j.status = 'APPROVED'
                AND j.endDay >= CURRENT_TIMESTAMP
                AND j.startDay <= CURRENT_TIMESTAMP
                AND (:location IS NULL OR j.workLocation = :location)
                AND (:type IS NULL OR j.jobType = :type)
                AND (:minSalary IS NULL OR j.salary >= :minSalary)
                AND (:maxSalary IS NULL OR j.salary <= :maxSalary)
            
            """)
    List<JobPost> filterJobs(
            String location,
            String type,
            Double minSalary,
            Double maxSalary
    );
}
