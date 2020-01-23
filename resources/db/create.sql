CREATE DATABASE fitness;

\connect fitness;

CREATE TABLE exercise
(
    id              SERIAL PRIMARY KEY,
    exerciseid      INT,
    day             DATE NOT NULL,
    sets            INT,
    reps            INT,
    weight          INT,
    duration        INT,
    distance        INT,
    lowpulse        INT,
    highpulse       INT,
    active          BOOLEAN,
    level           INT,
    standard        BOOLEAN
);

CREATE TABLE exercise_meta
(
    id              SERIAL PRIMARY KEY,
    exerciseid      INT UNIQUE NOT NULL,
    standard        BOOLEAN,
    name            TEXT NOT NULL
);

CREATE TABLE squash
(
    id              SERIAL PRIMARY KEY,
    opponent        VARCHAR(64) NOT NULL,
    day             DATE NOT NULL,
    myscore         INT NOT NULL,
    opponentScore   INT NOT NULL
);
