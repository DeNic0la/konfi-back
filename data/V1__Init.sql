CREATE TABLE brunch
(
    id              VARCHAR(50)  NOT NULL,
    title           VARCHAR(255) NOT NULL,
    require_email   BIT(1) DEFAULT 0 NULL,
    email_regexp    VARCHAR(255) NULL,
    admin_password  VARCHAR(255) NULL,
    voting_password VARCHAR(255) NULL,
    CONSTRAINT pk_brunch PRIMARY KEY (id)
);

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