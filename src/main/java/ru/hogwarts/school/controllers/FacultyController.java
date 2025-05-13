package ru.hogwarts.school.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.services.FacultyService;

import java.util.Collection;
import java.util.Collections;

@RestController
@RequestMapping("/faculty")
    public class FacultyController {

        private final FacultyService facultyService;

        public FacultyController(FacultyService facultyService) {
            this.facultyService = facultyService;
        }

        @GetMapping("{id}")
        public ResponseEntity <Faculty> getFacultyInfo (@PathVariable Long id) {
            Faculty faculty = facultyService.findFaculty(id);
            if (faculty == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(faculty);
        }
        @GetMapping
        public ResponseEntity <Collection<Faculty>> getAllFaculty () {
            return ResponseEntity.ok(facultyService.getAllFaculty())  ;  }

    @GetMapping("/filter")
    public ResponseEntity<Collection<Faculty>> findFaculties(@RequestParam(required = false) String color) {
        if (color != null && !color.isBlank()) {
            return ResponseEntity.ok(facultyService.findByColor(color));
        }
        return ResponseEntity.ok(Collections.emptyList());
    }
        @PostMapping
        public Faculty createFaculty (@RequestBody Faculty faculty) {
            return facultyService.createFaculty(faculty);
        }
        @PutMapping
        public ResponseEntity <Faculty> editFaculty (@RequestBody Faculty faculty) {
            Faculty foundFaculty = facultyService.findFaculty(faculty.getId());
            if (foundFaculty == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(foundFaculty);

        }
        @DeleteMapping("{id}")
        public ResponseEntity deleteFaculty (@PathVariable Long id) {
            facultyService.deleteFaculty(id);
            return ResponseEntity.ok().build();
        }
}
