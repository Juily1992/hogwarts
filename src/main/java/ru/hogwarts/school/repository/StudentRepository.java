package ru.hogwarts.school.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.hogwarts.school.model.Student;

import java.util.Collection;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Student findStudentByNameContainsIgnoreCase(String name);
    Collection<Student> findStudentByAge(int age);
    Collection<Student> findByNameContainingIgnoreCase(String part);
    Collection<Student> findByAgeBetween(int minAge, int maxAge);

    @Query(value = "SELECT COUNT(s) FROM Student s WHERE s.faculty IS NOT NULL", nativeQuery = true)
    Long countAllStudents();


@Query(value = "SELECT COUNT(s) FROM Student s WHERE s.faculty.id = :facultyId", nativeQuery = true)
Long countStudentsByFaculty(@Param("facultyId") Long facultyId);

    @Query(value = "SELECT AVG(s.age) FROM Student s", nativeQuery = true)
    Double findAverageAge();

    @Query(value = "SELECT * FROM student ORDER BY id DESC LIMIT 5", nativeQuery = true)
    List<Student> findTop5ByOrderByIdDescNative();
}
