package ru.hogwarts.school;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.controllers.StudentController;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.services.StudentService;

import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
public class StudentControllerTestMVC {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentService studentService;


    @Test
    void shouldReturnStudentById() throws Exception {
        Student student = new Student(1L, "Harry Potter", 15);
        when(studentService.getStudentById(1L)).thenReturn(student);

        mockMvc.perform(get("/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Harry Potter"));

        verify(studentService, times(1)).getStudentById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenStudentDoesNotExist() throws Exception {
        when(studentService.getStudentById(999L)).thenReturn(null);

        mockMvc.perform(get("/student/999"))
                .andExpect(status().isNotFound());

        verify(studentService, times(1)).getStudentById(999L);
    }

    @Test
    void shouldReturnAllStudents() throws Exception {
        Collection<Student> students = List.of(
                new Student(1L, "Harry", 15),
                new Student(2L, "Hermione", 16)
        );
        when(studentService.getAllStudents()).thenReturn(students);

        mockMvc.perform(get("/student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));

        verify(studentService, times(1)).getAllStudents();
    }

    @Test
    void shouldFilterStudentsByAgeBetween() throws Exception {
        Collection<Student> students = List.of(new Student(1L, "Ron", 20));
        when(studentService.findStudentByAgeBetween(20, 30)).thenReturn(students);

        mockMvc.perform(get("/student/filterByAge")
                        .param("min", "20")
                        .param("max", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));

        verify(studentService, times(1)).findStudentByAgeBetween(20, 30);
    }

    @Test
    void shouldReturnBadRequestWhenMinOrMaxNotProvided() throws Exception {
        mockMvc.perform(get("/student/filterByAge"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFilterStudentsByExactAge() throws Exception {
        Collection<Student> students = List.of(new Student(1L, "Harry", 20));
        when(studentService.findStudentByAge(20)).thenReturn(students);

        mockMvc.perform(get("/student/filter")
                        .param("age", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));

        verify(studentService, times(1)).findStudentByAge(20);
    }

    @Test
    void shouldFilterStudentsByName() throws Exception {
        Student student = new Student(1L, "Harry", 15);
        when(studentService.findStudentByName("harry")).thenReturn(student);

        mockMvc.perform(get("/student/filter")
                        .param("name", "harry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Harry"));

        verify(studentService, times(1)).findStudentByName("harry");
    }

    @Test
    void shouldReturnEmptyListWhenNameNotFound() throws Exception {
        when(studentService.findStudentByName("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/student/filter")
                        .param("name", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(studentService, times(1)).findStudentByName("nonexistent");
    }

    @Test
    void shouldFilterStudentsByPartOfName() throws Exception {
        Collection<Student> students = List.of(new Student(1L, "Harry", 15));
        when(studentService.findByNameContaining("rry")).thenReturn(students);

        mockMvc.perform(get("/student/filter")
                        .param("part", "rry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));

        verify(studentService, times(1)).findByNameContaining("rry");
    }

    @Test
    void shouldCreateStudentSuccessfully() throws Exception {
        Student input = new Student();
        input.setName("New Student");
        input.setSurname("Example");
        input.setAge(20);

        Student output = new Student();
        output.setId(1L);
        output.setName("New Student");
        output.setSurname("Example");
        output.setAge(20);

        when(studentService.createStudent(any(Student.class))).thenReturn(output);

        mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Student\",\"surname\":\"Example\",\"age\":20}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("New Student"));
    }

    @Test
    void shouldEditExistingStudent() throws Exception {
        Student existing = new Student(1L, "Old Name", 15);
        when(studentService.findStudent(1L)).thenReturn(existing);

        mockMvc.perform(put("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"Updated\",\"surname\":\"Student\",\"age\":16}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));

        verify(studentService, times(1)).findStudent(1L);
    }

    @Test
    void shouldReturnNotFoundWhenEditingNonExistentStudent() throws Exception {
        when(studentService.findStudent(999L)).thenReturn(null);

        mockMvc.perform(put("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":999,\"name\":\"Unknown\"}"))
                .andExpect(status().isNotFound());

        verify(studentService, times(1)).findStudent(999L);
    }

    @Test
    void shouldDeleteStudentSuccessfully() throws Exception {
        Student deleted = new Student(1L, "Harry", 15);
        when(studentService.deleteStudent(1L)).thenReturn(deleted);

        mockMvc.perform(delete("/student/1"))
                .andExpect(status().isOk());

        verify(studentService, times(1)).deleteStudent(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentStudent() throws Exception {
        when(studentService.deleteStudent(999L)).thenReturn(null);

        mockMvc.perform(delete("/student/999"))
                .andExpect(status().isNotFound());

        verify(studentService, times(1)).deleteStudent(999L);
    }

    @Test
    void shouldReturnFacultyOfStudent() throws Exception {
        Faculty faculty = new Faculty(1L, "Gryffindor", "Red");

        Student student = new Student(1L, "Harry", 15);
        student.getFaculty();

        when(studentService.getStudentById(1L)).thenReturn(student);

        mockMvc.perform(get("/student/1/faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gryffindor"));

        verify(studentService, times(1)).getStudentById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenStudentHasNoFaculty() throws Exception {
        when(studentService.getStudentById(999L)).thenReturn(null);

        mockMvc.perform(get("/student/999/faculty"))
                .andExpect(status().isNotFound());

        verify(studentService, times(1)).getStudentById(999L);
    }

    @Test
    void shouldUploadAvatarSuccessfully() throws Exception {
        // Мокаем успешное выполнение
        doNothing().when(studentService).uploadAvatar(1L, any(MultipartFile.class));

        mockMvc.perform(multipart("/student/1/avatar")
                        .file("avatar", "test content".getBytes()))
                .andExpect(status().isOk());

        verify(studentService, times(1)).uploadAvatar(1L, any(MultipartFile.class));
    }

    @Test
    void shouldRejectLargeAvatarUpload() throws Exception {
        byte[] largeFile = new byte[1024 * 1024 + 1]; // больше 1MB

        mockMvc.perform(multipart("/student/1/avatar")
                        .file("avatar", largeFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The size of avatar is too large"));

        verify(studentService, never()).uploadAvatar(anyLong(), any(MultipartFile.class));
    }

    @Test
    void shouldDownloadAvatarPreview() throws Exception {
        Avatar avatar = new Avatar();
        avatar.setMediaType("image/jpeg");
        avatar.setPreview("fake-image-data".getBytes());

        when(studentService.findStudentAvatar(1L)).thenReturn(avatar);

        mockMvc.perform(get("/student/1/avatar/preview"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/jpeg"))
                .andExpect(content().bytes("fake-image-data".getBytes()));
    }

    @Test
    void shouldReturnNotFoundWhenAvatarDoesNotExist() throws Exception {
        when(studentService.findStudentAvatar(999L)).thenReturn(null);

        mockMvc.perform(get("/student/999/avatar/preview"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldStreamAvatarSuccessfully() throws Exception {
        Avatar avatar = new Avatar();
        avatar.setFilePath("src/test/resources/test.jpg");
        avatar.setFileSize(1024L);
        avatar.setMediaType("image/jpeg");

        when(studentService.findStudentAvatar(1L)).thenReturn(avatar);

        mockMvc.perform(get("/student/1/avatar"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/jpeg"));

        verify(studentService, times(1)).findStudentAvatar(1L);
    }

    @Test
    void shouldReturnNotFoundWhenStreamingAvatarOfUnknownStudent() throws Exception {
        when(studentService.findStudentAvatar(999L)).thenReturn(null);

        mockMvc.perform(get("/student/999/avatar"))
                .andExpect(status().isNotFound());

        verify(studentService, times(1)).findStudentAvatar(999L);
    }
}
