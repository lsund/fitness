CREATE DATABASE fitness;
CREATE USER lsund WITH PASSWORD 'admin';
GRANT ALL PRIVILEGES ON DATABASE fitness TO lsund;

\connect fitness;

CREATE TABLE Exercise
(
    id              SERIAL PRIMARY KEY,
    exerciseid      INT,
    name            VARCHAR(64) NOT NULL,
    day             DATE NOT NULL,
    sets            INT,
    reps            INT,
    weight          INT,
    duration        INT,
    distance        INT,
    lowpulse        INT,
    highpulse       INT,
    level           INT
);

CREATE TABLE Squash
(
    id              SERIAL PRIMARY KEY,
    opponent        VARCHAR(64) NOT NULL,
    day             DATE NOT NULL,
    myscore         INT NOT NULL,
    opponentScore   INT NOT NULL
);

ALTER TABLE Exercise OWNER to lsund;
ALTER TABLE Squash OWNER to lsund;
