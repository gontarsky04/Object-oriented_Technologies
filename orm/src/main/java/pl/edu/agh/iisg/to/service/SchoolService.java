package pl.edu.agh.iisg.to.service;

import org.hibernate.Session;
import org.hibernate.cache.spi.support.CollectionNonStrictReadWriteAccess;
import pl.edu.agh.iisg.to.dao.CourseDao;
import pl.edu.agh.iisg.to.dao.GradeDao;
import pl.edu.agh.iisg.to.dao.StudentDao;
import pl.edu.agh.iisg.to.repository.StudentRepository;
import pl.edu.agh.iisg.to.model.Course;
import pl.edu.agh.iisg.to.model.Grade;
import pl.edu.agh.iisg.to.model.Student;
import pl.edu.agh.iisg.to.session.TransactionService;

import java.util.*;
import java.util.stream.Collectors;

public class SchoolService {

    private final TransactionService transactionService;

    private final StudentRepository studentRepository;

    private final CourseDao courseDao;

    private final GradeDao gradeDao;

    public SchoolService(TransactionService transactionService, StudentRepository studentRepository, CourseDao courseDao, GradeDao gradeDao) {
        this.transactionService = transactionService;
        this.studentRepository = studentRepository;
        this.courseDao = courseDao;
        this.gradeDao = gradeDao;
    }

    // Backwards-compatible constructor used by tests that still create StudentDao directly
    public SchoolService(TransactionService transactionService, StudentDao studentDao, CourseDao courseDao, GradeDao gradeDao) {
        this.transactionService = transactionService;
        this.studentRepository = new StudentRepository(studentDao, courseDao);
        this.courseDao = courseDao;
        this.gradeDao = gradeDao;
    }

    public boolean enrollStudent(final Course course, final Student student) {
        return transactionService.doAsTransaction(() -> {
            if (!course.studentSet().contains(student)) {
                course.studentSet().add(student);
                student.courseSet().add(course);
                return true;
            }
            return false;
        }).orElse(false);
    }

    public boolean removeStudent(int indexNumber) {
        return transactionService.doAsTransaction(() -> {
            Optional<Student> studentOpt = studentRepository.findByIndexNumber(indexNumber);
            if (studentOpt.isEmpty()) {
                return false;
            }
            Student student = studentOpt.get();
            // delegate removal and association cleanup to repository
            studentRepository.remove(student);
            return true;
        }).orElse(false);
    }

    public boolean gradeStudent(final Student student, final Course course, final float gradeValue) {
        return transactionService.doAsTransaction(() -> {
            gradeDao.save(new Grade(student, course, gradeValue));
            return true;
        }).orElse(false);
    }

    public Map<String, List<Float>> getStudentGrades(String courseName) {
        return transactionService.doAsTransaction(() -> {
            Optional<Course> optCourse = courseDao.findByName(courseName);
            if (optCourse.isEmpty()) {
                return Collections.<String, List<Float>>emptyMap();
            }
            Course course = optCourse.get();
            Set<Grade> grades = course.gradeSet();

            Map<String, List<Float>> report = grades.stream()
                    .collect(Collectors.groupingBy(
                            g -> g.student().fullName(),
                            Collectors.mapping(Grade::grade, Collectors.toList())
                    ));
            report.values().forEach(Collections::sort);
            return report;
    }).orElse(Collections.<String, List<Float>>emptyMap());
    }
}
