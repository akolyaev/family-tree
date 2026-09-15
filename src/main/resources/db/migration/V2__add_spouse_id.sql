ALTER TABLE person ADD COLUMN spouse_id BIGINT REFERENCES person(id);
