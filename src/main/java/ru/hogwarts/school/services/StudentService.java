package ru.hogwarts.school.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
import java.util.stream.IntStream;

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

    public List<String> getNamesStartingWithA() {
        return studentRepository.findAll().stream()
                .map(student -> student.getName())
                .filter(name -> name != null && !name.isEmpty() && (name.charAt(0) == 'A' || name.charAt(0) == 'a'))
                .map(String::toUpperCase)
                .sorted()
                .toList();
    }

    public double getAverageAgeOfStudents() {
        return studentRepository.findAll().stream()
                .mapToInt(Student::getAge)
                .average()
                .orElse(0);
    }

    public int calculateSumUsingParallelStream() {
        return IntStream.rangeClosed(1, 1_000_000)
                .parallel()
                .sum();
    }

    public List<String> getAllStudentNames() {
        return studentRepository.findAll().stream()
                .map(Student::getName)
                .toList();
    }

    public ResponseEntity<String> processStudentsParallel() {
        List<String> names = getAllStudentNames();

        System.out.println("Main thread - " + names.get(0));
        System.out.println("Main thread - " + names.get(1));
        if (names.size() < 6) {
            throw new RuntimeException("Not enough students in database to perform this action");
        }

        new Thread(() -> {
            System.out.println("Thread 1 - " + names.get(2));
            System.out.println("Thread 1 - " + names.get(3));
        }, "Thread-1").start();

        new Thread(() -> {
            System.out.println("Thread 2 - " + names.get(4));
            System.out.println("Thread 2 - " + names.get(5));
        }, "Thread-2").start();

        return ResponseEntity.ok("Names printed in parallel");
    }

    public ResponseEntity<String> processPrintSynchronized() throws InterruptedException {
        List<String> names = getAllStudentNames();

        printNameSynchronized(names.get(0));
        printNameSynchronized(names.get(1));

        processNamesWithThreads(names);

        return ResponseEntity.ok("Names printed synchronously");
    }

    public void processNamesWithThreads(List<String> names) {
        Thread t1 = new Thread(() -> {
            printNameSynchronized(names.get(2));
            printNameSynchronized(names.get(3));
        }, "Thread-1");

        Thread t2 = new Thread(() -> {
            printNameSynchronized(names.get(4));
            printNameSynchronized(names.get(5));
        }, "Thread-2");

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void printNameSynchronized(String name) {
        System.out.println("Thread: " + Thread.currentThread().getName() + ", Name: " + name);
    }
}
