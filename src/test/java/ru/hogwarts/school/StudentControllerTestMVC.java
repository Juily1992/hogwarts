package ru.hogwarts.school;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
        Student student = createTestStudent(1L, "Harry Potter", 15);
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
                createTestStudent(1L, "Harry", 15),
                createTestStudent(2L, "Hermione", 16)
        );
        when(studentService.getAllStudents()).thenReturn(students);

        mockMvc.perform(get("/student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));

        verify(studentService, times(1)).getAllStudents();
    }

    private Student createTestStudent(Long id, String name, int age) {
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setAge(age);
        return student;
    }
    @Test
    void shouldFilterStudentsByAgeBetween() throws Exception {
        Collection<Student> students = List.of(createTestStudent(1L, "Ron", 20));
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
                .andExpect(status().isBadRequest()).andExpect(content().string(""));;
    }

    @Test
    void shouldFilterStudentsByExactAge() throws Exception {
        Collection<Student> students = List.of(createTestStudent(1L, "Harry", 20));
        when(studentService.findStudentByAge(20)).thenReturn(students);

        mockMvc.perform(get("/student/filter")
                        .param("age", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));

        verify(studentService, times(1)).findStudentByAge(20);
    }

    @Test
    void shouldFilterStudentsByName() throws Exception {
        Student student = createTestStudent(1L, "Harry", 15);
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
        Collection<Student> students = List.of(createTestStudent(1L, "Harry", 15));
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
        Student existing = createTestStudent(1L, "Old Name", 15);
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

    private Faculty createTestFaculty(Long id, String name, String colour) {
        Faculty faculty = new Faculty();
        faculty.setId(id);
        faculty.setName(name);
        faculty.setColour(colour);
        return faculty;
    }
    @Test
    void shouldReturnFacultyOfStudent() throws Exception {
        Faculty faculty = createTestFaculty(1L, "Gryffindor", "Red");

        Student student = createTestStudent(1L, "Harry", 15);
        student.setFaculty(faculty);

        when(studentService.getStudentById(1L)).thenReturn(student);

        mockMvc.perform(get("/student/1/faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gryffindor"))
                .andExpect(jsonPath("$.colour").value("Red"));

        verify(studentService, times(1)).getStudentById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenStudentHasNoFaculty() throws Exception {
        when(studentService.getStudentById(999L)).thenReturn(null);

        mockMvc.perform(get("/student/999/faculty"))
                .andExpect(status().isNotFound());

        verify(studentService, times(1)).getStudentById(999L);
    }

//    @Test
//    void shouldRejectLargeAvatarUpload() throws Exception {
//        MockMultipartFile file = new MockMultipartFile(
//                "avatar", "large.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[1024 * 1024 + 1]
//        );
//
//        mockMvc.perform(multipart("/student/1/avatar").file(file))
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string("The size of avatar is too large"));
//
//        verify(studentService, never()).uploadAvatar(eq(1L), any(MultipartFile.class));
//    }

//    @Test
//    void shouldDownloadAvatarPreview() throws Exception {
//        Avatar avatar = new Avatar();
//        avatar.setMediaType("image/jpeg");
//        avatar.setPreview("fake-image-data".getBytes());
//
//        when(studentService.findStudentAvatar(1L)).thenReturn(avatar);
//
//        mockMvc.perform(get("/student/1/avatar/preview"))
//                .andExpect(status().isOk())
//                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/jpeg"))
//                .andExpect(content().bytes("fake-image-data".getBytes()));
//    }
//
//    @Test
//    void shouldReturnNotFoundWhenAvatarDoesNotExist() throws Exception {
//        when(studentService.findStudentAvatar(999L)).thenReturn(null);
//
//        mockMvc.perform(get("/student/999/avatar/preview"))
//                .andExpect(status().isNotFound());
//    }

//    @Test
//    void shouldStreamAvatarSuccessfully() throws Exception {
//        Avatar avatar = new Avatar();
//        avatar.setFilePath("src/test/resources/test.jpg");
//        avatar.setFileSize(1024L);
//        avatar.setMediaType("image/jpeg");
//
//        when(studentService.findStudentAvatar(1L)).thenReturn(avatar);
//
//        mockMvc.perform(get("/student/1/avatar"))
//                .andExpect(status().isOk())
//                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/jpeg"));
//
//        verify(studentService, times(1)).findStudentAvatar(1L);
//    }

    @Test
    void shouldReturnNotFoundWhenStreamingAvatarOfUnknownStudent() throws Exception {
        when(studentService.findStudentAvatar(999L)).thenReturn(null);

        mockMvc.perform(get("/student/999/avatar"))
                .andExpect(status().isNotFound());

        verify(studentService, times(1)).findStudentAvatar(999L);
    }

    @Test
    void testUploadAvatar_ShouldReturnOk_WhenFileIsValid() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "avatar", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image".getBytes()
        );

        mockMvc.perform(multipart("/student/1/avatar").file(file))
                .andExpect(status().isOk());

        verify(studentService, times(1)).uploadAvatar(eq(1L), any(MultipartFile.class));
    }
}
