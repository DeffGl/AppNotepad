CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE TABLE tasks(
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    name VARCHAR(256) NOT NULL UNIQUE,
    description VARCHAR(10240),
    created_date TIMESTAMP NOT NULL,
    modified_date TIMESTAMP,
    completed BOOLEAN NOT NULL
);

