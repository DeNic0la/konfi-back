CREATE TABLE brunch
(
    id              VARCHAR(50)  NOT NULL,
    title           VARCHAR(255) NOT NULL,
    require_email   BIT(1) DEFAULT 0 NULL,
    email_regexp    VARCHAR(255) NULL,
    CONSTRAINT pk_brunch PRIMARY KEY (id)
);
CREATE TABLE brunch_authorization
(
    brunch_id            VARCHAR(50) NOT NULL,
    admin_password_hash  VARCHAR(70) NULL,
    voting_password_hash VARCHAR(70) NULL,
    CONSTRAINT pk_brunch_authorization PRIMARY KEY (brunch_id)
);

ALTER TABLE brunch_authorization
    ADD CONSTRAINT FK_BRUNCH_AUTHORIZATION_ON_BRUNCH FOREIGN KEY (brunch_id) REFERENCES brunch (id);

CREATE TABLE question
(
    id          INT AUTO_INCREMENT NOT NULL,
    title       VARCHAR(255)       NULL,
    link        VARCHAR(255)       NULL,
    min         INT    DEFAULT 0   NULL,
    max         INT    DEFAULT 0   NULL,
    recommended INT                NULL,
    sort_order_number     INT                NULL,
    brunch_id   VARCHAR(50)        NOT NULL,
    optional    BIT(1) DEFAULT 0   NULL,
    CONSTRAINT pk_question PRIMARY KEY (id)
);

ALTER TABLE question
    ADD CONSTRAINT FK_QUESTION_ON_BRUNCH FOREIGN KEY (brunch_id) REFERENCES brunch (id);

CREATE TABLE vote
(
    id        INT AUTO_INCREMENT NOT NULL,
    email     VARCHAR(255)       NULL,
    name      VARCHAR(255)       NULL,
    brunch_id VARCHAR(50)        NOT NULL,
    CONSTRAINT pk_vote PRIMARY KEY (id)
);

ALTER TABLE vote
    ADD CONSTRAINT FK_VOTE_ON_BRUNCH FOREIGN KEY (brunch_id) REFERENCES brunch (id);

CREATE TABLE vote_answer
(
    id               INT AUTO_INCREMENT NOT NULL,
    answer_to_id     INT                NOT NULL,
    konfidence_value INT                NOT NULL,
    vote_id          INT                NOT NULL,
    CONSTRAINT pk_vote_answer PRIMARY KEY (id)
);

ALTER TABLE vote_answer
    ADD CONSTRAINT FK_VOTE_ANSWER_ON_ANSWER_TO FOREIGN KEY (answer_to_id) REFERENCES question (id);

ALTER TABLE vote_answer
    ADD CONSTRAINT FK_VOTE_ANSWER_ON_VOTE FOREIGN KEY (vote_id) REFERENCES vote (id);



insert into brunch (id, title, require_email, email_regexp)
values  ('PI-25-3-Rudolf', 'Planning 25-3 Team Rudolf Confidence Voting', false, null);