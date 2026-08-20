-- =====================================================================
-- Bookworm Delta - Seed data
-- =====================================================================
-- Enough rows to exercise every screen: buy, rent, lending library,
-- royalty split across beneficiaries, and the admin product list.
--
-- Passwords are BCrypt hashes, which is what AuthService.login compares
-- against:
--     admin@bookworm.com / Admin@123   (is_Admin = TRUE)
--     reader@bookworm.com / User@123   (is_Admin = FALSE)
-- =====================================================================

USE bookworm_delta;

-- ---------------------------------------------------------------------
-- user
-- ---------------------------------------------------------------------
INSERT INTO `user` (User_Name, User_Email, User_Phone, User_Address, User_Password, is_Admin, Join_Date) VALUES
('Bookworm Admin', 'admin@bookworm.com',  '9820011111', 'Fort, Mumbai 400001',   '$2b$10$hO1Lo4.XDEibX7jDYiaWCOPgA5PyTVkNx88YtNosRpyObei1JWO1a', TRUE,  '2026-01-05'),
('Asha Kulkarni',  'reader@bookworm.com', '9820022222', 'Kothrud, Pune 411038',  '$2b$10$CnWT6xrj7/cZD4JK5NDB6OO6gzC00zU.PmDqBcm18u2IrD82VerfS', FALSE, '2026-02-14');

-- ---------------------------------------------------------------------
-- author
-- ---------------------------------------------------------------------
INSERT INTO author (Name, Bio) VALUES
('V. S. Khandekar',   'Jnanpith-winning Marathi novelist, best known for Yayati.'),
('Premchand',         'Pioneer of modern Hindi and Urdu social fiction.'),
('Damodar Mauzo',     'Konkani short-story writer and novelist from Goa.'),
('Sudha Murty',       'Author and philanthropist writing in English and Kannada.'),
('Ranjit Desai',      'Marathi historical novelist, best known for Shriman Yogi.');

-- ---------------------------------------------------------------------
-- publisher
-- ---------------------------------------------------------------------
INSERT INTO publisher (Name, Email) VALUES
('Mehta Publishing House', 'rights@mehtapublishing.in'),
('Rajkamal Prakashan',     'contact@rajkamal.in'),
('Jaico Publishing',       'hello@jaico.in');

-- ---------------------------------------------------------------------
-- language
-- ---------------------------------------------------------------------
INSERT INTO `language` (Language_Desc) VALUES
('Marathi'), ('Hindi'), ('Konkani'), ('English');

-- ---------------------------------------------------------------------
-- genere
-- ---------------------------------------------------------------------
INSERT INTO genere (Genere_Desc) VALUES
('Fiction'), ('Historical'), ('Short Stories'), ('Biography'), ('Poetry');

-- ---------------------------------------------------------------------
-- product_type_master
-- ---------------------------------------------------------------------
INSERT INTO product_type_master (Type_Desc) VALUES
('eBook'), ('Audiobook'), ('Music'), ('Film');

-- ---------------------------------------------------------------------
-- attribute
-- ---------------------------------------------------------------------
INSERT INTO attribute (Attribute_Desc) VALUES
('Pages'), ('Edition'), ('Duration (minutes)'), ('Format');

-- ---------------------------------------------------------------------
-- product
-- ---------------------------------------------------------------------
-- product_type / author / publisher / language / genere reference the
-- auto-increment ids inserted above, in the order they were listed.
INSERT INTO product
(product_name, product_type, product_author, product_publisher, product_lang, product_genere,
 product_baseprice, product_offerprice, discount_percent, royalty_percent,
 product_off_price_expirydate, product_description_short, product_description_long,
 product_isbn, is_rentable, is_library, rent_per_day, min_rent_days, product_Image)
VALUES
('Yayati', 1, 1, 1, 1, 1,
 499.00, 399.00, NULL, 10.00, '2026-12-31',
 'Khandekar''s retelling of the Mahabharata king who traded his son''s youth for his own.',
 'A Jnanpith-winning novel that reads the Yayati legend as a study of appetite. Told in turns by Yayati, Devayani, and Sharmishtha, it asks what a life spent chasing pleasure actually costs the people standing closest to it.',
 '9788177666011', TRUE, TRUE, 12.00, 3, NULL),

('Godaan', 1, 2, 2, 2, 1,
 425.00, NULL, 15.00, 8.00, NULL,
 'Premchand''s portrait of a peasant family and the cow it can never quite afford.',
 'Hori Mahato wants one thing: a cow of his own. Premchand follows that small want through debt, caste, and a village economy engineered to keep it out of reach, in what is widely read as his finest novel.',
 '9788126727049', TRUE, TRUE, 10.00, 3, NULL),

('Karmelin', 1, 3, 3, 3, 1,
 375.00, NULL, NULL, 12.00, NULL,
 'A Goan woman leaves for the Gulf and returns to a village that has moved on without her.',
 'Mauzo''s Sahitya Akademi-winning novel follows Karmelin from a Goan fishing village to domestic work in the Gulf and back again, tracing what migration costs a woman who was never given a choice about leaving.',
 '9788126019281', FALSE, TRUE, NULL, 0, NULL),

('Wise and Otherwise', 1, 4, 3, 4, 3,
 299.00, 249.00, NULL, 10.00, '2026-10-31',
 'Fifty short encounters from Sudha Murty''s travels across India.',
 'Short, plainly told pieces drawn from the people Murty met while travelling for her foundation''s work. The collection stays deliberately undramatic, which is what gives the sharper stories their weight.',
 '9788172234881', TRUE, FALSE, 8.00, 2, NULL),

('Shriman Yogi', 1, 5, 1, 1, 2,
 650.00, NULL, NULL, 14.00, NULL,
 'Ranjit Desai''s novel of Chhatrapati Shivaji Maharaj, from Shivneri to Raigad.',
 'Desai writes Shivaji as an administrator and a son as much as a general. The result is a long, deliberately unhurried historical novel that spends as much time on the building of a state as on its battles.',
 '9788177661279', TRUE, TRUE, 15.00, 5, NULL),

('Nirmala', 1, 2, 2, 2, 1,
 275.00, NULL, NULL, 8.00, NULL,
 'A young bride married into a household that has no room for her.',
 'Premchand''s short novel about dowry and the marriage market puts a fifteen-year-old girl in a house where every adult has already decided what she is for. It remains one of his most direct pieces of social criticism.',
 '9788126712397', FALSE, TRUE, NULL, 0, NULL),

('Teresa''s Man and Other Stories', 1, 3, 3, 3, 3,
 320.00, NULL, 10.00, 12.00, NULL,
 'Konkani short stories from the villages and shorelines of Goa.',
 'Fourteen stories that stay close to ordinary Goan life: a marriage under strain, a fisherman''s superstition, a village adjusting to tourist money. Mauzo works in small gestures rather than large turns.',
 '9788125042099', TRUE, TRUE, 9.00, 2, NULL),

('The Old Man and His God', 1, 4, 3, 4, 3,
 285.00, NULL, NULL, 10.00, NULL,
 'Everyday encounters that Sudha Murty found worth writing down.',
 'A companion to Wise and Otherwise. The subjects are unremarkable people in unremarkable circumstances, which is the point: the book argues that decency shows up in small, unwitnessed decisions.',
 '9780143063223', TRUE, FALSE, 7.00, 2, NULL);

-- ---------------------------------------------------------------------
-- product_attribute
-- ---------------------------------------------------------------------
INSERT INTO product_attribute (product_id, attribute_id, attribute_value) VALUES
(1, 1, '600'), (1, 2, '35th'),
(2, 1, '344'), (2, 2, '12th'),
(3, 1, '412'),
(4, 1, '190'),
(5, 1, '892'), (5, 2, '20th'),
(6, 1, '156'),
(7, 1, '224'),
(8, 1, '176');

-- ---------------------------------------------------------------------
-- beneficiary + beneficiary_assignment
-- ---------------------------------------------------------------------
-- Every product that carries a royalty_percent needs at least one
-- assigned beneficiary, otherwise checkout has nowhere to send the
-- royalty. A beneficiary is a reusable identity now, not tied to one
-- product - Premchand Trust and Damodar Mauzo each receive for two
-- different books here, which is exactly the case beneficiary_assignment
-- exists for.
INSERT INTO beneficiary
(beneficiary_name, beneficiary_type, beneficiary_email_id, beneficiary_contact_no, beneficiary_bank_name,
 beneficiary_bank_branch, beneficiary_ifsc, beneficiary_acc_no, beneficiary_acc_type, beneficiary_pan)
VALUES
('Khandekar Literary Estate', 'Other', 'estate@khandekar.in',      '9822010001', 'State Bank of India',  'Kolhapur Main',  'SBIN0000123', '30112233445', 'SAVINGS', 'AAAPK1234A'),
('Mehta Publishing House',    'Other', 'rights@mehtapublishing.in','9822010002', 'HDFC Bank',            'Pune Camp',      'HDFC0000456', '50100123456', 'CURRENT', 'AABCM5678B'),
('Premchand Trust',           'Other', 'trust@premchand.org',      '9822010003', 'Punjab National Bank', 'Varanasi Cantt', 'PUNB0011200', '11223344556', 'SAVINGS', 'AAATP9012C'),
('Damodar Mauzo',             'Other', 'mauzo@konkani.in',         '9822010004', 'Bank of Baroda',       'Margao',         'BARB0MARGAO', '20033445566', 'SAVINGS', 'AAFPM3456D'),
('Sudha Murty Foundation',    'Other', 'grants@murtyfound.org',    '9822010005', 'Canara Bank',          'Jayanagar',      'CNRB0002233', '40011223344', 'CURRENT', 'AAETM7890E'),
('Ranjit Desai Estate',       'Other', 'estate@ranjitdesai.in',    '9822010006', 'Axis Bank',            'Kolhapur',       'UTIB0000789', '91800112233', 'SAVINGS', 'AAGPD2345F');

-- product_id 1=Yayati, 2=Godaan, 3=Karmelin, 4=Wise and Otherwise, 5=Shriman Yogi,
-- 6=Nirmala, 7=Teresa's Man and Other Stories, 8=The Old Man and His God
-- (matches product insert order above); beneficiary_id matches insert order just above.
INSERT INTO beneficiary_assignment (product_id, beneficiary_id) VALUES
(1, 1), (1, 2),   -- Yayati: Khandekar Literary Estate, Mehta Publishing House
(2, 3),           -- Godaan: Premchand Trust
(3, 4),           -- Karmelin: Damodar Mauzo
(4, 5),           -- Wise and Otherwise: Sudha Murty Foundation
(5, 6),           -- Shriman Yogi: Ranjit Desai Estate
(6, 3),           -- Nirmala: Premchand Trust (same trust as Godaan)
(7, 4),           -- Teresa's Man and Other Stories: Damodar Mauzo (same as Karmelin)
(8, 5);           -- The Old Man and His God: Sudha Murty Foundation (same as Wise and Otherwise)

-- ---------------------------------------------------------------------
-- library_package
-- ---------------------------------------------------------------------
-- One package only, matching the BRD's own lending-library example
-- almost exactly (BRD section 6: "Library package for 30 days for 12
-- books at price say Rs 120"). LibraryPackageController/Service already
-- handle any number of packages generically, so nothing but this seed
-- data needed to change.
INSERT INTO library_package (name, cost, validity_days, book_limit, description) VALUES
('Granthaalay', 1500.00, 15, 10, 'Borrow up to 10 library titles for 15 days.');
