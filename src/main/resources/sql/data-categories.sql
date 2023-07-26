-- ADD CONSTRAINT For category name and parent_category_id to be unique and not distinct (NULL, NULL) values

ALTER TABLE transaction_category -- Drop constraint to avoid conflict
    DROP CONSTRAINT IF EXISTS transaction_category_name_parent_category_id_uindex;

ALTER TABLE transaction_category
    ADD CONSTRAINT transaction_category_name_parent_category_id_uindex
        UNIQUE NULLS NOT DISTINCT (name, parent_category_id);

--     DATA

--     OUTCOME categories

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Food And Beverages', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Grocery', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Food And Beverages')),
       ('Restaurant', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Food And Beverages')),
       ('Cafe', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Food And Beverages')),
       ('Alcohol and Bars', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Food And Beverages')),
       ('Snacks', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Food And Beverages'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Transportation', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Public Transportation', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Transportation')),
       ('Taxi', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Transportation')),
       ('Gas', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Transportation')),
       ('Parking', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Transportation')),
       ('Car warranty', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Transportation')),
       ('Car maintenance', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Transportation'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Travel', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Entertainment', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Movies', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Entertainment')),
       ('Concerts', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Entertainment')),
       ('Theater', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Entertainment')),
       ('Games', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Entertainment'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Family', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Partner', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Family')),
       ('Children', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Family')),
       ('Parents', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Family')),
       ('Pets', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Family'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Friends', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Hobby', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Sport', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Gym', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Sport')),
       ('Sport equipment', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Sport'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Personal care', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Haircut', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Personal care')),
       ('Beauty', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Personal care')),
       ('Cosmetics', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Personal care')),
       ('Spa', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Personal care'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Health', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Pharmacy', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Health')),
       ('Primary care', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Health')),
       ('Dental care', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Health')),
       ('Specialty care', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Health')),
       ('Surgery', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Health')),
       ('Medical devices', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Health'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Education', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Books', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Education')),
       ('Courses', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Education'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Shopping', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Clothing', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Shopping')),
       ('Shoes', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Shopping')),
       ('Electronics', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Shopping')),
       ('Accessories', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Shopping')),
       ('Home', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Shopping'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Bills', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Subscription', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Bills')),
       ('Phone Bill', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Bills')),
       ('Internet Bill', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Bills')),
       ('Television Bill', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Bills')),
       ('Rent', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Bills')),
       ('Watter Bill', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Bills')),
       ('Electricity Bill', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Bills')),
       ('Gas Bill', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Bills'))
    ON CONFLICT DO NOTHING;


INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Gift', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Birthday', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Gift')),
       ('Charity', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Gift'))
    ON CONFLICT DO NOTHING;


INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Business', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Savings', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Other', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

--        INCOME categories

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Salary', 'INCOME', NULL),
       ('Gift', 'INCOME', NULL),
       ('Award', 'INCOME', NULL),
       ('Sponsorship', 'INCOME', NULL),
       ('Business', 'INCOME', NULL),
       ('Other', 'INCOME', NULL)
    ON CONFLICT DO NOTHING;

