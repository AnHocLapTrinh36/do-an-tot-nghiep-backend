package com.example.Backend.config;

import com.example.Backend.entity.CVType;
import com.example.Backend.entity.Cv;
import com.example.Backend.entity.Status; // Nhớ import Enum Status
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class CvSpecification {

    public static Specification<Cv> filterPublicApprovedCvs(String keyword, String type, List<String> techs) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("status"), Status.APPROVED));

            predicates.add(cb.equal(root.get("isPublic"), true));

            // Lọc theo Title (Keyword)
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"));
            }

            // Lọc theo Type (FORM / UPLOAD)
            if (type != null && !type.trim().isEmpty()) {
                try {
                    predicates.add(cb.equal(root.get("type"), CVType.valueOf(type.toUpperCase())));
                } catch (IllegalArgumentException e) {
                    // Tránh crash nếu type gửi lên không khớp với Enum
                }
            }

            // Lọc theo danh sách công nghệ trong contentJson (JSON String)
            if (techs != null && !techs.isEmpty()) {
                List<Predicate> techPredicates = new ArrayList<>();
                for (String tech : techs) {
                    // Tìm kiếm pattern "SkillName" trong chuỗi JSON
                    String searchPattern = "%\"" + tech + "\"%";
                    techPredicates.add(cb.like(root.get("contentJson"), searchPattern));
                }
                // Nối các điều kiện skill bằng OR (Chỉ cần có 1 trong các skill là thoả mãn)
                if (!techPredicates.isEmpty()) {
                    predicates.add(cb.or(techPredicates.toArray(new Predicate[0])));
                }
            }

            // Nối tất cả các cụm điều kiện lại bằng AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}