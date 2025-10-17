package pl.edu.agh.iisg.to.dao;

import jakarta.persistence.PersistenceException;
import org.hibernate.Session;
import pl.edu.agh.iisg.to.model.Student;
import pl.edu.agh.iisg.to.session.SessionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StudentDao extends GenericDao<Student> {

    public StudentDao(SessionService sessionService) {
        super(sessionService, Student.class);
    }

    public Optional<Student> create(final String firstName, final String lastName, final int indexNumber) {
        Student student = new Student(firstName, lastName, indexNumber);
        return save(student);
    }

    public List<Student> findAll() {
        Session session = currentSession();
        try {
            List<Student> students = session.createQuery("SELECT s FROM Student s ORDER BY s.lastName", Student.class)
                    .getResultList();
            return students == null ? Collections.emptyList() : students;
        } catch (PersistenceException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Optional<Student> findByIndexNumber(final int indexNumber) {
        Session session = currentSession();
        try {
            Student student = (Student) session.createQuery("SELECT s FROM Student s WHERE s.indexNumber = :indexNumber")
                    .setParameter("indexNumber", indexNumber)
                    .uniqueResult();
            return Optional.ofNullable(student);
        } catch (PersistenceException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
