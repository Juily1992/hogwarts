package ru.hogwarts.school.controllers;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.services.StudentService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;

    }

    @GetMapping("{id}")
    public ResponseEntity<Student> getStudentInfo(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(student);
    }

    @GetMapping
    public ResponseEntity<Collection<Student>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/filterByAge")
    public ResponseEntity<Collection<Student>> findStudents(
            @RequestParam(required = false) Integer min,
            @RequestParam(required = false) Integer max) {

        if (min != null && max != null) {
            return ResponseEntity.ok(studentService.findStudentByAgeBetween(min, max));
        }
        return ResponseEntity.badRequest().body(null);
    }

    @GetMapping("/filter")
    public ResponseEntity<Collection<Student>> findStudents(
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String part) {

        if (age != null && age > 0) {
            Collection<Student> byAge = studentService.findStudentByAge(age);
            return ResponseEntity.ok(byAge);
        }

        if (name != null && !name.isBlank()) {
            Student byName = studentService.findStudentByName(name);
            if (byName == null) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(Collections.singletonList(byName));
        }

        if (part != null && !part.isBlank()) {
            Collection<Student> byPart = studentService.findByNameContaining(part);
            return ResponseEntity.ok(byPart);
        }

        Collection<Student> allStudents = studentService.getAllStudents();
        return ResponseEntity.ok(allStudents);
    }

    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        Student createdStudent = studentService.createStudent(student);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }

    @PutMapping
    public ResponseEntity<Student> editStudent(@RequestBody Student student) {
        Student updated = studentService.editStudent(student);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{studentId}/faculty")
    public ResponseEntity<Faculty> getFacultyOfStudent(@PathVariable Long studentId) {
        Student student = studentService.getStudentById(studentId);
        if (student == null || student.getFaculty() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(student.getFaculty());
    }

    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAvatar(@PathVariable Long id, @RequestParam MultipartFile avatar) throws IOException {
        if (avatar.getSize() >= 1024 * 1024) {
            return ResponseEntity.badRequest().body("The size of avatar is too large");
        }
        studentService.uploadAvatar(id, avatar);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{id}/avatar/preview")
    public ResponseEntity<byte[]> downloadAvatar(@PathVariable Long id) {
        Avatar avatar = studentService.findStudentAvatar(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(avatar.getMediaType()));
        headers.setContentLength(avatar.getPreview().length);
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(avatar.getPreview());
    }

    @GetMapping(value = "/{id}/avatar")
    public void downloadAvatar(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Avatar avatar = studentService.findStudentAvatar(id);

        Path path = Path.of(avatar.getFilePath());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(avatar.getMediaType());
        response.setContentLengthLong(avatar.getFileSize());

        try (InputStream is = Files.newInputStream(path);
             OutputStream os = response.getOutputStream()) {
            is.transferTo(os);
        }
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotalStudents() {
        return ResponseEntity.ok(studentService.getTotalStudents());
    }

    @GetMapping("/total/by-faculty/{facultyId}")
    public ResponseEntity<Long> getStudentCountByFaculty(@PathVariable Long facultyId) {
        Long count = studentService.getStudentCountByFaculty(facultyId);
        if (count == null || count == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(count);
    }

    @GetMapping("/average-age")
    public ResponseEntity<Double> getAverageStudentAge() {
        Double averageAge = studentService.getAverageStudentAge();
        if (averageAge == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(averageAge);
    }

    @GetMapping("/latest")
    public ResponseEntity<List<Student>> getLatestStudents() {
        List<Student> latestStudents = studentService.getLatestFiveStudents();
        return ResponseEntity.ok(latestStudents);
    }

    @GetMapping("/{studentId}/avatars")
    public ResponseEntity<Page<Avatar>> getAvatarsByStudentId(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Page<Avatar> avatars = studentService.getAvatarsByStudentId(studentId, page, size);

        if (avatars.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(avatars);
    }

    @GetMapping("/names-start-with-a")
    public List<String> getNamesStartingWithA() {
        return studentService.getNamesStartingWithA();
    }

    @GetMapping("/average-age")
    public double getAverageAgeOfStudents() {
        return studentService.getAverageAgeOfStudents();
    }

    @GetMapping("/sum")
    public int calculateSum() {
        return studentService.calculateSumUsingParallelStream();
    }

    @GetMapping("/students/print-parallel")
    public ResponseEntity<String> printParallel() {
        return studentService.processStudentsParallel();
    }

    @GetMapping("/students/print-synchronized")
    public ResponseEntity<String> printSynchronized() throws InterruptedException {
       return studentService.processPrintSynchronized();
    }
}

