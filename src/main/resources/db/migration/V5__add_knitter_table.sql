CREATE TABLE knitter (
    id UUID NOT NULL,
    email VARCHAR NOT NULL UNIQUE,
    name VARCHAR,
    image_url VARCHAR,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id)
);
