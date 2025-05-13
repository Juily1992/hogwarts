package ru.hogwarts.school.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Collection;

@Service
public class StudentService {
    @Autowired
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public Student findStudent(long id) {
        return studentRepository.getById(id);
    }

    public Student editStudent(Student student) {
        return studentRepository.save(student);
    }

    public Collection<Student> getAllStudents() {
        return studentRepository.findAll();
    }


    public Student deleteStudent (Long id) {
        Student faculty = studentRepository.findById(id).orElse(null);
        if (faculty != null) {
            studentRepository.deleteById(id);
        }
        return faculty; // возвращаем удалённый объект или null
    }



    public Student findStudentByName(String name) {
        return studentRepository.findStudentByNameContainsIgnoreCase(name);
    }

    public Collection<Student> findStudentByAge(int age) {
        return studentRepository.findStudentByAge(age);
    }

    public Collection<Student> findByNameContaining(String part) {
        return studentRepository.findByNameContainingIgnoreCase(part);
    }

    public Collection<Student> findStudentByAgeBetween(int min, int max) {
        return studentRepository.findByAgeBetween(min, max);
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id).orElse(null);
    }
}
