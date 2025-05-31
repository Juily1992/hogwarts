ALTER TABLE student ADD CONSTRAINT student_age_check CHECK (age >= 16);

ALTER TABLE student ALTER COLUMN name SET NOT NULL;
CREATE UNIQUE INDEX idx_student_unique_name ON student(name);

ALTER TABLE faculty ALTER COLUMN name SET NOT NULL;
ALTER TABLE faculty ALTER COLUMN colour SET NOT NULL;
CREATE UNIQUE INDEX idx_faculty_unique_name_colour ON faculty(name, colour);

ALTER TABLE student ALTER COLUMN age SET DEFAULT 20;
ALTER TABLE student ALTER COLUMN age SET NOT NULL;
