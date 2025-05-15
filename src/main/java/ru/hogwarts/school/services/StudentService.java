package ru.hogwarts.school.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final AvatarRepository avatarRepository;
    @Value("${avatars.dir.path}")
    private String avatarsDir;


    public StudentService(StudentRepository studentRepository, AvatarRepository avatarRepository) {
        this.studentRepository = studentRepository;
        this.avatarRepository = avatarRepository;
    }

    public Student createStudent(Student student) {
        student.setId(null);
        return studentRepository.save(student);
    }

    public Student findStudent(long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Студент с ID " + id + " не найден"));
    }

    public Student editStudent(Student student) {
        return studentRepository.save(student);
    }

    public Collection<Student> getAllStudents() {
        return studentRepository.findAll();
    }


    public void deleteStudent(long id) {
        studentRepository.deleteById(id);
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

    public void uploadAvatar(Long studentId, MultipartFile file) throws IOException {
        Student student = findStudent(studentId);

        Path filePath = Path.of(avatarsDir, studentId + "." + getExtention(file.getOriginalFilename()));
        Files.createDirectories(filePath.getParent());
        Files.deleteIfExists(filePath);

        try (InputStream is = file.getInputStream();
             OutputStream os = Files.newOutputStream(filePath, CREATE_NEW);
             BufferedInputStream bis = new BufferedInputStream(is, 1024);
             BufferedOutputStream bos = new BufferedOutputStream(os, 1024);
        ) {
            bis.transferTo(bos);
        }

        Avatar avatar = avatarRepository.findByStudentId(studentId).orElseGet(Avatar::new);
        avatar.setStudent(student);
        avatar.setFilePath(filePath.toString());
        avatar.setFileSize(file.getSize());
        avatar.setMediaType(file.getContentType());
        avatar.setPreview(file.getBytes());

        avatarRepository.save(avatar);
    }

    public Avatar findStudentAvatar(Long id) {
        return avatarRepository.findByStudentId(id).orElseThrow();
    }

    public String getExtention(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

}
