package ru.hogwarts.school.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.exceptions.StudentNotFoundException;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final AvatarRepository avatarRepository;
    @Value("${avatars.dir.path}")
    private String avatarsDir;
    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);


    public StudentService(StudentRepository studentRepository, AvatarRepository avatarRepository) {
        this.studentRepository = studentRepository;
        this.avatarRepository = avatarRepository;
    }

    public Student createStudent(Student student) {
        logger.info("Was invoked method for create student");
        student.setId(null);
        return studentRepository.save(student);
    }

    public Student findStudent(long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Студент с ID " + id + " не найден"));
    }

    public Student editStudent(Student student) {
        logger.debug("Editing student with ID {}", student.getId());
        if (!studentRepository.existsById(student.getId())) {
            logger.error("Cannot edit student - no student with ID {}", student.getId());
            throw new StudentNotFoundException("Student not found");
        }
        logger.info("Was invoked method to update student");
        return studentRepository.save(student);
    }

    public Collection<Student> getAllStudents() {
        logger.warn("Someone is getting all students");
        return studentRepository.findAll();
    }


    public void deleteStudent(long id) {
        logger.info("Was invoked method to delete student with ID {}", id);
        if (!studentRepository.existsById(id)) {
            logger.error("Cannot delete student - no student with ID {}", id);
            throw new StudentNotFoundException("Student not found");
        }
        studentRepository.deleteById(id);
    }

    public Student findStudentByName(String name) {
        return studentRepository.findStudentByNameContainsIgnoreCase(name);
    }

    public Collection<Student> findStudentByAge(int age) {
        logger.info("Was invoked method to filter students by age {}", age);
        return studentRepository.findStudentByAge(age);
    }

    public Collection<Student> findByNameContaining(String part) {
        return studentRepository.findByNameContainingIgnoreCase(part);
    }

    public Collection<Student> findStudentByAgeBetween(int min, int max) {
        return studentRepository.findByAgeBetween(min, max);
    }

    public Student getStudentById(Long id) {
        logger.debug("Looking for student with id {}", id);
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) {
            logger.error("There is no student with id = {}", id);
            throw new StudentNotFoundException("Student not found");
        }
        logger.info("Was invoked method to get student by id {}", id);
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

    public long getTotalStudents() {
        return studentRepository.countAllStudents();
    }

    public long getStudentCountByFaculty(Long facultyId) {
        return studentRepository.countStudentsByFaculty(facultyId);
    }

    public Double getAverageStudentAge() {
        return studentRepository.findAverageAge();
    }

    public List<Student> getLatestFiveStudents() {
        return studentRepository.findTop5ByOrderByIdDescNative();
    }

    public Page<Avatar> getAvatarsByStudentId(Long studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return avatarRepository.findByStudentId(studentId, pageable);
    }
}
