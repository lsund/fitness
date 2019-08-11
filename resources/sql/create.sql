CREATE TABLE Exercise
(
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(64) NOT NULL,
    day             DATE NOT NULL,
    duration        INT,
    distance        INT,
    lowpulse        INT,
    highpulse       INT,
    level           INT,
    sets            INT,
    reps            INT,
    weight          INT
);

CREATE TABLE SquashOpponent
(
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(64) UNIQUE
);

CREATE TABLE SquashResult
(
    id              SERIAL PRIMARY KEY,
    day             DATE NOT NULL,
    opponentid      INT NOT NULL,
    myscore         INT NOT NULL,
    opponentScore   INT NOT NULL,
    FOREIGN KEY     (opponentid) REFERENCES SquashOpponent (id)
);
