package com.example.Backend.service.Implement;

import com.example.Backend.entity.Course;
import com.example.Backend.entity.Teacher;
import com.example.Backend.repository.CourseReponsitory;
import com.example.Backend.repository.TeacherRepository;

import com.example.Backend.service.CourseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseReponsitory courseRepository;
    private final TeacherRepository teacherRepository;

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public Course getCourseById(Integer id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học ID: " + id));
    }

    @Override
    public Course createCourse(Course course) {
        if (courseRepository.existsByName(course.getName()))
            throw new RuntimeException("Tên khóa học đã tồn tại");
        return courseRepository.save(course);
    }

    @Override
    public Course updateCourse(Integer id, Course courseRequest) {
        Course course = getCourseById(id);
        if (courseRepository.existsByNameAndIdNot(courseRequest.getName(), id))
            throw new RuntimeException("Tên khóa học đã tồn tại");
        course.setName(courseRequest.getName());
        return courseRepository.save(course);
    }

    @Override
    public void deleteCourse(Integer id) {
        Course course = getCourseById(id);
        courseRepository.delete(course);
    }

    @Transactional
    public Course addTeacherToCourse(Integer courseId, Long teacherId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên"));
        course.getTeachers().add(teacher);
        // nhow  quan hệ n-n cấu hình mà nó tự thêm data vào course_teacher
        return courseRepository.save(course);
    }

    @Transactional
    public void removeTeacherFromCourse(Integer courseId, Long teacherId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên"));
        course.getTeachers().remove(teacher);
        courseRepository.save(course);
    }
}