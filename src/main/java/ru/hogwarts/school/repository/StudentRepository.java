package ru.hogwarts.school.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hogwarts.school.model.Student;

import java.util.Collection;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Student findStudentByNameContainsIgnoreCase(String name);
    Collection<Student> findStudentByAge(int age);
    Collection<Student> findByNameContainingIgnoreCase(String part);
    Collection<Student> findByAgeBetween(int minAge, int maxAge);
}
