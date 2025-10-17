package pl.edu.agh.iisg.to.repository;

import pl.edu.agh.iisg.to.dao.CourseDao;
import pl.edu.agh.iisg.to.dao.StudentDao;
import pl.edu.agh.iisg.to.model.Course;
import pl.edu.agh.iisg.to.model.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentRepository implements Repository<Student> {

    private final StudentDao studentDao;
    private final CourseDao courseDao;

    public StudentRepository(final StudentDao studentDao, final CourseDao courseDao) {
        this.studentDao = studentDao;
        this.courseDao = courseDao;
    }

    @Override
    public Optional<Student> add(Student student) {
        return studentDao.save(student);
    }

    @Override
    public Optional<Student> getById(int id) {
        return studentDao.findById(id);
    }

    @Override
    public List<Student> findAll() {
        return studentDao.findAll();
    }

    @Override
    public void remove(Student student) {
        // Find managed instance and remove associations with courses before deleting
        studentDao.findById(student.id()).ifPresent(managed -> {
            for (Course course : managed.courseSet()) {
                course.studentSet().remove(managed);
            }
            studentDao.remove(managed);
        });
    }

    public List<Student> findAllByCourseName(String courseName) {
        Optional<Course> optCourse = courseDao.findByName(courseName);
        if (optCourse.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(optCourse.get().studentSet());
    }

    public Optional<Student> findByIndexNumber(final int indexNumber) {
        return studentDao.findByIndexNumber(indexNumber);
    }

}
