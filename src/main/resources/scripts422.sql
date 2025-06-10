
CREATE TABLE person (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        age INT NOT NULL CHECK (age >= 0),
                        has_license BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE car (
                     id BIGSERIAL PRIMARY KEY,
                     make TEXT NOT NULL,
                     model TEXT NOT NULL,
                     price NUMERIC(19, 2) NOT NULL CHECK (price >= 0)
);

CREATE TABLE person_car (
                            person_id BIGINT REFERENCES person(id),
                            car_id BIGINT REFERENCES car(id),
                            PRIMARY KEY (person_id, car_id)
);
INSERT INTO person(name, age, has_license) VALUES
                                               ('Harry', 30, true),
                                               ('Ron', 28, false),
                                               ('Hermione', 30, true);
INSERT INTO car(make, model, price) VALUES
                                        ('Toyota', 'Corolla', 20000.00),
                                        ('Honda', 'Civic', 22000.00),
                                        ('Tesla', 'Model S', 90000.00);
INSERT INTO person_car(person_id, car_id) VALUES
                                              (1, 1),
                                              (2, 1),
                                              (3, 3);