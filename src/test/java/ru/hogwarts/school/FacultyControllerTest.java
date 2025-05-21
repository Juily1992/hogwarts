package ru.hogwarts.school;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.UriComponentsBuilder;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;
import ru.hogwarts.school.services.FacultyService;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FacultyControllerTest {
    private FacultyService facultyService;
    @Autowired
    private FacultyRepository facultyRepository;
    @Autowired
    private StudentRepository studentRepository;
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getRootUrl() {
        return "http://localhost:" + port + "/faculty";
    }

    @BeforeEach
    public void setup() {
        Faculty faculty = new Faculty();
        faculty.setColour("Red");
        faculty.setName("Griff");
        Student student = new Student();
        student.setAge(11);
        student.setName("Harry");
        student.setSurname("Potter");
        student.setFaculty(faculty);
        facultyRepository.save(faculty);
        studentRepository.save(student);
    }

    @AfterEach
    public void clear() {
        studentRepository.deleteAll();
        facultyRepository.deleteAll();
    }

    @Test
    void testGetFacultyById_ShouldReturnFaculty_WhenExists() {
        Long id = 1L;
        ResponseEntity<Faculty> response = restTemplate.getForEntity(getRootUrl() + "/{id}", Faculty.class, id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(id);
    }

    private Faculty createTestFaculty(Long id, String name, String colour) {
        Faculty faculty = new Faculty();
        faculty.setId(id);
        faculty.setName(name);
        faculty.setColour(colour);
        return faculty;
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
        String url = UriComponentsBuilder.fromHttpUrl(getRootUrl() + "/filter")
                .queryParam("colour", colour)
                .build()
                .toUriString();

        ResponseEntity<List<Faculty>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Faculty>>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty()
                .allMatch(f -> f.getColour().equalsIgnoreCase(colour));
    }

    @Test
    void testFindFacultiesByName_ShouldReturnSingleFaculty() {
        String name = "Gryffindor";

        String url = UriComponentsBuilder.fromHttpUrl(getRootUrl() + "/filter")
                .queryParam("name", name)
                .build()
                .toUriString();

        ResponseEntity<Faculty[]> response = restTemplate.getForEntity(url, Faculty[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getName()).isEqualTo(name);
    }


    @Test
    void testFindAllFaculties_WhenNoFilterParams() {
        ResponseEntity<Faculty[]> response = restTemplate.getForEntity(getRootUrl() + "/filter", Faculty[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody()).hasSizeGreaterThan(0);
    }

    @Test
    void testCreateFaculty_ShouldReturnCreatedFaculty() {
        Faculty faculty = new Faculty();
        faculty.setName("New Faculty");
        faculty.setColour("Blue");

        ResponseEntity<Faculty> response = restTemplate.postForEntity(getRootUrl(), faculty, Faculty.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertNotNull(response.getBody());
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
        Assertions.assertNotNull(response.getBody());
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
