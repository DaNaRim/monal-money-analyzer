-- ADD CONSTRAINT For category name, type and parent_category_id to be unique and not distinct (NULL, NULL) values

ALTER TABLE transaction_category -- Drop constraint to avoid conflict
    DROP CONSTRAINT IF EXISTS transaction_category_name_type_parent_category_id_uindex;

ALTER TABLE transaction_category
    ADD CONSTRAINT transaction_category_name_type_parent_category_id_uindex
        UNIQUE NULLS NOT DISTINCT (name, type, parent_category_id);

--     DATA

--     OUTCOME categories

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Food And Beverages', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Grocery',
        'OUTCOME',
        (SELECT id FROM transaction_category WHERE name = 'Food And Beverages' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Restaurant',
        'OUTCOME',
        (SELECT id FROM transaction_category WHERE name = 'Food And Beverages' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Cafe', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Food And Beverages' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Alcohol and Bars',
        'OUTCOME',
        (SELECT id FROM transaction_category WHERE name = 'Food And Beverages' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Snacks',
        'OUTCOME',
        (SELECT id FROM transaction_category WHERE name = 'Food And Beverages' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;


INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Transportation', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Public Transportation',
        'OUTCOME',
        (SELECT id FROM transaction_category WHERE name = 'Transportation' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Taxi', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Transportation' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Gas', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Transportation' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Parking', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Transportation' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Car maintenance',
        'OUTCOME',
        (SELECT id FROM transaction_category WHERE name = 'Transportation' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;


INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Travel', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;


INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Entertainment', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Movies', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Entertainment' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Concerts', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Entertainment' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Theater', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Entertainment' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Games', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Entertainment' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;


INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Family', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Partner', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Family' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Children', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Family' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Parents', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Family' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Pets', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Family' AND type = 'OUTCOME'))
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
VALUES ('Gym', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Sport' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Sport equipment', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Sport' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;


INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Personal care', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Haircut', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Personal care' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Beauty', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Personal care' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Cosmetics', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Personal care' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Spa', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Personal care' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;


INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Health', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Pharmacy', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Health' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Primary care', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Health' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Dental care', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Health' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Specialty care', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Health' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Surgery', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Health' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Medical devices', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Health' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;


INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Education', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Books', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Education' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Courses', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Education' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;


INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Shopping', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Clothing', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Shopping' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Shoes', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Shopping' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Electronics', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Shopping' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Accessories', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Shopping' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Home', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Shopping' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;


INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Bills', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Subscription', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Bills' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Phone Bill', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Bills' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Internet Bill', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Bills' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Television Bill', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Bills' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Rent', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Bills' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Water Bill', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Bills' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Electricity Bill', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Bills' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Gas Bill', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Bills' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;


INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Gift', 'OUTCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Birthday', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Gift' AND type = 'OUTCOME'))
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Charity', 'OUTCOME', (SELECT id FROM transaction_category WHERE name = 'Gift' AND type = 'OUTCOME'))
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
VALUES ('Salary', 'INCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Gift', 'INCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Award', 'INCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Sponsorship', 'INCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Business', 'INCOME', NULL)
    ON CONFLICT DO NOTHING;

INSERT INTO transaction_category (name, type, parent_category_id)
VALUES ('Other', 'INCOME', NULL)
    ON CONFLICT DO NOTHING;


