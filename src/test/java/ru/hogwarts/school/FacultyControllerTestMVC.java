package ru.hogwarts.school;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.controllers.FacultyController;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.services.FacultyService;

import java.util.Collection;
import java.util.List;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FacultyController.class)
public class FacultyControllerTestMVC {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FacultyService facultyService;

    @Test
    void shouldReturnFacultyById() throws Exception {
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Gryffindor");
        faculty.setColour("Red");

        when(facultyService.findFaculty(1L)).thenReturn(faculty);

        mockMvc.perform(get("/faculty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gryffindor"))
                .andExpect(jsonPath("$.colour").value("Red"));

        verify(facultyService, times(1)).findFaculty(1L);
    }

    @Test
    void shouldReturnNotFoundWhenFacultyDoesNotExist() throws Exception {
        when(facultyService.findFaculty(999L)).thenReturn(null);

        mockMvc.perform(get("/faculty/999"))
                .andExpect(status().isNotFound());

        verify(facultyService, times(1)).findFaculty(999L);
    }

    @Test
    void shouldReturnAllFaculties() throws Exception {
        Collection<Faculty> faculties = List.of(
                new Faculty(1L, "Gryffindor", "Red"),
                new Faculty(2L, "Slytherin", "Green")
        );

        when(facultyService.getAllFaculty()).thenReturn(faculties);

        mockMvc.perform(get("/faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));

        verify(facultyService, times(1)).getAllFaculty();
    }

    @Test
    void shouldFilterFacultiesByColour() throws Exception {
        Collection<Faculty> filtered = List.of(new Faculty(1L, "Gryffindor", "Red"));

        when(facultyService.findByColour("red")).thenReturn(filtered);

        mockMvc.perform(get("/faculty/filter?colour=red"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].colour").value("Red"));
    }

    @Test
    void shouldFilterFacultiesByName() throws Exception {
        Faculty faculty = new Faculty(1L, "Gryffindor", "Red");

        when(facultyService.findByName("Gryffindor")).thenReturn(faculty);

        mockMvc.perform(get("/faculty/filter?name=Gryffindor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Gryffindor"));
    }

    @Test
    void shouldReturnAllFacultiesWhenNoFilterParams() throws Exception {
        Collection<Faculty> allFaculties = List.of(
                new Faculty(1L, "Gryffindor", "Red"),
                new Faculty(2L, "Hufflepuff", "Yellow")
        );

        when(facultyService.getAllFaculty()).thenReturn(allFaculties);

        mockMvc.perform(get("/faculty/filter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void shouldCreateFacultySuccessfully() throws Exception {
        Faculty input = new Faculty();
        input.setName("New Faculty");
        input.setColour("Blue");

        Faculty output = new Faculty();
        output.setId(3L);
        output.setName("New Faculty");
        output.setColour("Blue");

        when(facultyService.createFaculty(any(Faculty.class))).thenReturn(output);

        mockMvc.perform(post("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Faculty\",\"colour\":\"Blue\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3L));
    }

    @Test
    void shouldEditExistingFaculty() throws Exception {
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Updated Gryffindor");
        faculty.setColour("Dark Red");

        when(facultyService.findFaculty(1L)).thenReturn(faculty);

        mockMvc.perform(put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"Updated Gryffindor\",\"colour\":\"Dark Red\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Gryffindor"));
    }

    @Test
    void shouldReturnNotFoundWhenEditingNonExistentFaculty() throws Exception {
        when(facultyService.findFaculty(999L)).thenReturn(null);

        mockMvc.perform(put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":999,\"name\":\"Unknown\",\"colour\":\"Black\"}"))
                .andExpect(status().isNotFound());
    }


    @Test
    void shouldDeleteFacultySuccessfully() throws Exception {
        Faculty deleted = new Faculty();
        deleted.setId(1L);
        deleted.setName("Gryffindor");

        when(facultyService.deleteFaculty(1L)).thenReturn(deleted);

        mockMvc.perform(delete("/faculty/1"))
                .andExpect(status().isOk());

        verify(facultyService, times(1)).deleteFaculty(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentFaculty() throws Exception {
        when(facultyService.deleteFaculty(999L)).thenReturn(null);

        mockMvc.perform(delete("/faculty/999"))
                .andExpect(status().isNotFound());

        verify(facultyService, times(1)).deleteFaculty(999L);
    }

    @Test
    void shouldReturnStudentsOfFaculty() throws Exception {
        Student student = new Student();
        student.setId(1L);
        student.setName("Harry Potter");
        student.setAge(15);

        when(facultyService.getStudentsByFacultyId(1L)).thenReturn(List.of(student));

        mockMvc.perform(get("/faculty/1/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Harry Potter"));
    }

    @Test
    void shouldReturnNotFoundWhenFacultyHasNoStudents() throws Exception {
        when(facultyService.getStudentsByFacultyId(999L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/faculty/999/students"))
                .andExpect(status().isNotFound());
    }
}