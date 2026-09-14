CREATE TABLE person (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    bio TEXT,
    photo_url VARCHAR(2000),
    birth_date DATE NOT NULL,
    death_date DATE,
    owner_username VARCHAR(255),
    is_claimed BOOLEAN NOT NULL DEFAULT FALSE,
    father_id BIGINT REFERENCES person(id),
    mother_id BIGINT REFERENCES person(id)
);
