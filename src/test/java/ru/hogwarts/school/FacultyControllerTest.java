package ru.hogwarts.school;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;

import java.util.Collection;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FacultyControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getRootUrl() {
        return "http://localhost:" + port + "/faculty";
    }

    @Test
    void testGetFacultyById_ShouldReturnFaculty_WhenExists() {
        Long id = 1L;
        ResponseEntity<Faculty> response = restTemplate.getForEntity(getRootUrl() + "/{id}", Faculty.class, id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(id);
    }

    @Test
    void testGetAllFaculties_ShouldReturnCollection() {
        ResponseEntity<Collection<Faculty>> response = restTemplate.exchange(
                getRootUrl(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Collection<Faculty>>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }


    @Test
    void testFindFacultiesByColour_ShouldReturnFilteredFaculties() {
        String colour = "red";
        ResponseEntity<Collection<Faculty>> response = restTemplate.getForEntity(
                getRootUrl() + "/filter?colour=" + colour,
                new ParameterizedTypeReference<Collection<Faculty>>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void testFindFacultiesByName_ShouldReturnSingleFaculty() {
        String name = "Gryffindor";
        ResponseEntity<Collection<Faculty>> response = restTemplate.getForEntity(
                getRootUrl() + "/filter?name=" + name,
                new ParameterizedTypeReference<Collection<Faculty>>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }


    @Test
    void testFindAllFaculties_WhenNoFilterParams() {
        ResponseEntity<Collection<Faculty>> response = restTemplate.getForEntity(
                getRootUrl() + "/filter",
                new ParameterizedTypeReference<Collection<Faculty>>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void testCreateFaculty_ShouldReturnCreatedFaculty() {
        Faculty faculty = new Faculty();
        faculty.setName("New Faculty");
        faculty.setColour("Blue");

        ResponseEntity<Faculty> response = restTemplate.postForEntity(getRootUrl(), faculty, Faculty.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isNotNull();
    }


    @Test
    void testEditFaculty_ShouldReturnUpdatedFaculty() {
        Faculty existing = restTemplate.getForObject(getRootUrl() + "/1", Faculty.class);

        existing.setColour("Dark Blue");

        HttpEntity<Faculty> request = new HttpEntity<>(existing);
        ResponseEntity<Faculty> response = restTemplate.exchange(
                getRootUrl(),
                HttpMethod.PUT,
                request,
                Faculty.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getColour()).isEqualTo("Dark Blue");
    }

    @Test
    void testDeleteFaculty_ShouldReturnOk() {
        ResponseEntity<Faculty> response = restTemplate.exchange(
                getRootUrl() + "/{id}",
                HttpMethod.DELETE,
                null,
                Faculty.class,
                1L
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testGetStudentsOfFaculty_ShouldReturnListOfStudents() {
        ResponseEntity<Collection<Student>> response = restTemplate.exchange(
                getRootUrl() + "/{facultyId}/students",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Collection<Student>>() {
                },
                1L
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }
}
