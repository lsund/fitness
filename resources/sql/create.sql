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

CREATE TABLE Squash
(
    id              SERIAL PRIMARY KEY,
    opponent        VARCHAR(64) NOT NULL,
    day             DATE NOT NULL,
    myscore         INT NOT NULL,
    opponentScore   INT NOT NULL
);
