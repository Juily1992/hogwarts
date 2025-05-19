package ru.hogwarts.school;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;

import java.io.File;
import java.nio.file.Files;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class StudentControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getRootUrl() {
        return "http://localhost:" + port + "/student";
    }

    @Test
    void testGetStudentInfo() {
        Long id = 1L;
        ResponseEntity<Student> response = restTemplate.getForEntity(getRootUrl() + "/" + id, Student.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(id);
    }

    @Test
    void testGetAllStudents_shouldReturnOkAndCollectionOfStudents() {
        ResponseEntity<Student[]> response = restTemplate.getForEntity(
                getRootUrl(),
                Student[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void testFindStudentsByAgeBetween() {
        ResponseEntity<Student[]> response = restTemplate.getForEntity(
                getRootUrl() + "/filterByAge?min=20&max=30",
                Student[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void testFindStudentsByAge() {
        ResponseEntity<Student[]> response = restTemplate.getForEntity(
                getRootUrl() + "/filter?age=20",
                Student[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void testFindStudentsByName() {
        ResponseEntity<Student[]> response = restTemplate.getForEntity(
                getRootUrl() + "/filter?name=harry",
                Student[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void testFindStudentsByPartOfName() {
        ResponseEntity<Student[]> response = restTemplate.getForEntity(
                getRootUrl() + "/filter?part=rry",
                Student[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void testCreateStudent() {
        Student student = new Student();
        student.setName("Harry");
        student.setSurname("Potter");
        student.setAge(15);

        ResponseEntity<Student> response = restTemplate.postForEntity(getRootUrl(), student, Student.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
    }

    @Test
    void testEditStudent() {
        Student existing = restTemplate.getForObject(getRootUrl() + "/1", Student.class);
        existing.setAge(16);

        HttpEntity<Student> request = new HttpEntity<>(existing);
        ResponseEntity<Student> response = restTemplate.exchange(
                getRootUrl(),
                HttpMethod.PUT,
                request,
                Student.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getAge()).isEqualTo(16);
    }

    @Test
    void testDeleteStudent() {
        ResponseEntity<Void> response = restTemplate.exchange(
                getRootUrl() + "/{id}", HttpMethod.DELETE, null, Void.class, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testGetFacultyOfStudent() {
        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                getRootUrl() + "/{studentId}/faculty", Faculty.class, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void testUploadAvatar() throws Exception {
        File file = File.createTempFile("test", ".jpg");
        Files.write(file.toPath(), "fake-image-content".getBytes());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("avatar", new FileSystemResource(file));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getRootUrl() + "/{id}/avatar", request, String.class, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testDownloadAvatarPreview() {
        ResponseEntity<byte[]> response = restTemplate.getForEntity(
                getRootUrl() + "/{id}/avatar/preview", byte[].class, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void testDownloadAvatarAsStream() {
        ResponseEntity<byte[]> response = restTemplate.getForEntity(
                getRootUrl() + "/{id}/avatar", byte[].class, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }
}