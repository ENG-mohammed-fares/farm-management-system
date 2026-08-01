
CREATE TABLE Users (
    user_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(10) NOT NULL DEFAULT 'WORKER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_role CHECK (role IN ('ADMIN', 'WORKER')),
    CONSTRAINT chk_email_or_phone CHECK (email IS NOT NULL OR phone IS NOT NULL),
    CONSTRAINT uq_name UNIQUE (name)
);

CREATE TABLE Farms (
    farm_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(200),
    size_dunums DECIMAL(12,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Farm_Workers (
    fw_id SERIAL PRIMARY KEY,
    farm_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    job_type VARCHAR(20) NOT NULL,
    wage_per_unit DECIMAL(10,2) NOT NULL,
    wage_unit VARCHAR(10) NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    hired_at DATE DEFAULT CURRENT_DATE,
    CONSTRAINT chk_job_type CHECK (job_type IN ('IRRIGATOR', 'HARVESTER', 'PLOWER')),
    CONSTRAINT chk_wage_unit CHECK (wage_unit IN ('liter', 'dunum', 'kg', 'piece')),
    CONSTRAINT chk_fw_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT uq_farm_user UNIQUE (farm_id, user_id)
);

CREATE TABLE Fields (
    field_id SERIAL PRIMARY KEY,
    farm_id INTEGER NOT NULL,
    name VARCHAR(100) NOT NULL,
    size_dunums DECIMAL(12,2),
    soil_status VARCHAR(15) DEFAULT 'NOT_TESTED',
    location VARCHAR(200),
    CONSTRAINT chk_soil CHECK (soil_status IN ('EXCELLENT', 'GOOD', 'FAIR', 'POOR', 'NOT_TESTED'))
);

CREATE TABLE Crops (
    crop_id SERIAL PRIMARY KEY,
    field_id INTEGER NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(15) NOT NULL,
    planted_date DATE,
    quantity VARCHAR(50),
    status VARCHAR(15) DEFAULT 'GROWING',
    CONSTRAINT chk_crop_type CHECK (type IN ('VEGETABLE', 'FRUIT', 'GRAIN', 'TREE')),
    CONSTRAINT chk_crop_status CHECK (status IN ('GROWING', 'READY', 'HARVESTED'))
);

CREATE TABLE Fertilizers_Medicines (
    fm_id SERIAL PRIMARY KEY,
    field_id INTEGER,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(15) NOT NULL,
    composition TEXT,
    active_ingredient VARCHAR(100),
    target_disease VARCHAR(100),
    quantity DECIMAL(10,2),
    unit VARCHAR(20),
    is_organic BOOLEAN DEFAULT FALSE,
    applied_date DATE,
    notes TEXT,
    CONSTRAINT chk_fm_type CHECK (type IN ('FERTILIZER', 'MEDICINE'))
);

CREATE TABLE Farm_Logs (
    log_id SERIAL PRIMARY KEY,
    field_id INTEGER NOT NULL,
    fw_id INTEGER NOT NULL,
    log_type VARCHAR(15) NOT NULL,
    description TEXT,
    quantity DECIMAL(10,2),
    log_date DATE DEFAULT CURRENT_DATE,
    CONSTRAINT chk_log_type CHECK (log_type IN ('IRRIGATION', 'PLOWING', 'PLANTING', 'FERTILIZING', 'NOTE'))
);

CREATE TABLE Harvests (
    harvest_id SERIAL PRIMARY KEY,
    field_id INTEGER NOT NULL,
    crop_id INTEGER NOT NULL,
    fw_id INTEGER NOT NULL,
    quantity_good DECIMAL(10,2) NOT NULL DEFAULT 0,
    quantity_damaged DECIMAL(10,2) NOT NULL DEFAULT 0,
    unit VARCHAR(10) NOT NULL,
    harvest_date DATE DEFAULT CURRENT_DATE,
    notes TEXT,
    status VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT chk_harvest_unit CHECK (unit IN ('kg', 'piece')),
    CONSTRAINT chk_harvest_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE TABLE Transactions (
    transaction_id SERIAL PRIMARY KEY,
    type VARCHAR(10) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    description TEXT,
    related_harvest_id INTEGER,
    related_user_id INTEGER,
    transaction_date DATE DEFAULT CURRENT_DATE,
    CONSTRAINT chk_tx_type CHECK (type IN ('SALE', 'PURCHASE', 'PAYMENT'))
);

ALTER TABLE Farm_Workers
    ADD CONSTRAINT fk_fw_farm FOREIGN KEY (farm_id) REFERENCES Farms(farm_id),
    ADD CONSTRAINT fk_fw_user FOREIGN KEY (user_id) REFERENCES Users(user_id);

ALTER TABLE Fields
    ADD CONSTRAINT fk_field_farm FOREIGN KEY (farm_id) REFERENCES Farms(farm_id);

ALTER TABLE Crops
    ADD CONSTRAINT fk_crop_field FOREIGN KEY (field_id) REFERENCES Fields(field_id);

ALTER TABLE Fertilizers_Medicines
    ADD CONSTRAINT fk_fm_field FOREIGN KEY (field_id) REFERENCES Fields(field_id);

ALTER TABLE Farm_Logs
    ADD CONSTRAINT fk_log_field FOREIGN KEY (field_id) REFERENCES Fields(field_id),
    ADD CONSTRAINT fk_log_fw FOREIGN KEY (fw_id) REFERENCES Farm_Workers(fw_id);

ALTER TABLE Harvests
    ADD CONSTRAINT fk_harvest_field FOREIGN KEY (field_id) REFERENCES Fields(field_id),
    ADD CONSTRAINT fk_harvest_crop FOREIGN KEY (crop_id) REFERENCES Crops(crop_id),
    ADD CONSTRAINT fk_harvest_fw FOREIGN KEY (fw_id) REFERENCES Farm_Workers(fw_id);

ALTER TABLE Transactions
    ADD CONSTRAINT fk_tx_harvest FOREIGN KEY (related_harvest_id) REFERENCES Harvests(harvest_id),
    ADD CONSTRAINT fk_tx_user FOREIGN KEY (related_user_id) REFERENCES Users(user_id);

INSERT INTO Users (name, email, phone, password, role) VALUES
('Admin', 'admin', '0599000000', '12345', 'ADMIN'),
('Ahmad Hassan', 'ahmad@hased.ps', '0599111111', '123456', 'WORKER'),
('Mohammad Khaled', 'mohammad@hased.ps', '0599222222', '123456', 'WORKER'),
('Sami Khalil', 'sami@hased.ps', '0599333333', '123456', 'WORKER'),
('Omar Ali', 'omar@hased.ps', '0599444444', '123456', 'WORKER'),
('Yousef Saleh', 'yousef@hased.ps', '0599555555', '123456', 'WORKER');

INSERT INTO Farms (name, location, size_dunums) VALUES
('Hased Farm', 'Nablus, Palestine', 50000.00);

INSERT INTO Fields (farm_id, name, size_dunums, soil_status, location) VALUES
(1, 'Field A-1', 8000.00, 'GOOD', 'North Section'),
(1, 'Field A-2', 5000.00, 'EXCELLENT', 'North Section'),
(1, 'Field B-1', 10000.00, 'FAIR', 'East Section'),
(1, 'Field C-1', 4000.00, 'GOOD', 'South Section'),
(1, 'Field C-3', 5000.00, 'NOT_TESTED', 'South Section');

INSERT INTO Farm_Workers (farm_id, user_id, job_type, wage_per_unit, wage_unit) VALUES
(1, 2, 'HARVESTER', 8.00, 'kg'),
(1, 3, 'HARVESTER', 8.00, 'kg'),
(1, 4, 'PLOWER', 50.00, 'dunum'),
(1, 5, 'IRRIGATOR', 6.00, 'liter'),
(1, 6, 'HARVESTER', 10.00, 'kg');

INSERT INTO Crops (field_id, name, type, planted_date, quantity, status) VALUES
(1, 'Olives', 'TREE', '2024-01-15', '450 trees', 'READY'),
(2, 'Tomatoes', 'VEGETABLE', '2025-03-01', '2 dunums', 'READY'),
(2, 'Cucumbers', 'VEGETABLE', '2025-03-15', '2 dunums', 'GROWING'),
(3, 'Wheat', 'GRAIN', '2025-01-10', '6 dunums', 'READY'),
(3, 'Barley', 'GRAIN', '2025-01-20', '4 dunums', 'HARVESTED'),
(4, 'Citrus', 'FRUIT', '2023-06-01', '150 trees', 'READY'),
(4, 'Figs', 'TREE', '2023-09-01', '80 trees', 'GROWING');

INSERT INTO Fertilizers_Medicines (field_id, name, type, composition, active_ingredient, target_disease, quantity, unit, is_organic, applied_date, notes) VALUES
(2, 'NPK Fertilizer', 'FERTILIZER', 'Nitrogen-Phosphorus-Potassium 20-20-20', NULL, NULL, 150.00, 'kg/dunum', FALSE, '2025-06-01', 'Applied before planting season'),
(3, 'Urea', 'FERTILIZER', 'CO(NH2)2 46% Nitrogen', NULL, NULL, 80.00, 'kg/dunum', FALSE, '2025-05-20', 'For wheat growth stage'),
(1, 'Fungicide Pro', 'MEDICINE', 'Copper hydroxide + Mancozeb', 'Copper hydroxide', 'Olive leaf spot', 12.00, 'liter', FALSE, '2025-05-15', 'Spray every 2 weeks'),
(2, 'Insecticide Max', 'MEDICINE', 'Lambda-cyhalothrin 5%', 'Lambda-cyhalothrin', 'Whitefly on tomatoes', 5.00, 'liter', FALSE, '2025-06-10', 'Apply early morning'),
(4, 'Compost Mix', 'FERTILIZER', 'Organic plant and animal waste', NULL, NULL, 500.00, 'kg/dunum', TRUE, '2025-04-01', 'Organic fertilizer for citrus'),
(3, 'Herbicide', 'MEDICINE', 'Glyphosate 41%', 'Glyphosate', 'Broadleaf weeds', 8.00, 'liter', FALSE, '2025-03-01', 'Pre-planting weed control');

INSERT INTO Farm_Logs (field_id, fw_id, log_type, description, quantity, log_date) VALUES
(2, 4, 'IRRIGATION', 'Morning irrigation for tomatoes', 15.00, '2025-07-05'),
(3, 3, 'PLOWING', 'Prepared soil for next season', 10.00, '2025-07-04'),
(1, 4, 'IRRIGATION', 'Olive tree irrigation', 20.00, '2025-07-03'),
(2, 1, 'PLANTING', 'Planted new cucumber seedlings', NULL, '2025-07-02'),
(4, 4, 'IRRIGATION', 'Citrus grove watering', 12.00, '2025-07-01'),
(2, 1, 'FERTILIZING', 'Applied NPK to tomato field', 30.00, '2025-06-28'),
(3, 3, 'PLOWING', 'Deep plowing for wheat field', 6.00, '2025-06-25'),
(1, 4, 'NOTE', 'Noticed pest damage on olive leaves', NULL, '2025-06-20');

INSERT INTO Harvests (field_id, crop_id, fw_id, quantity_good, quantity_damaged, unit, harvest_date, notes, status) VALUES
(2, 2, 1, 850.00, 30.00, 'kg', '2025-07-05', 'Morning harvest', 'PENDING'),
(2, 2, 2, 600.00, 20.00, 'kg', '2025-07-03', 'Afternoon batch', 'APPROVED'),
(3, 4, 1, 3200.00, 120.00, 'kg', '2025-07-02', 'Full field harvest', 'APPROVED'),
(1, 1, 5, 1150.00, 45.00, 'kg', '2025-06-30', 'First olive harvest', 'APPROVED'),
(2, 3, 2, 320.00, 15.00, 'kg', '2025-06-28', 'Early cucumber pick', 'PENDING'),
(4, 6, 1, 2800.00, 90.00, 'kg', '2025-06-25', 'Citrus collection', 'APPROVED'),
(3, 5, 5, 4100.00, 200.00, 'kg', '2025-06-20', 'Barley season end', 'REJECTED');

INSERT INTO Transactions (type, amount, description, related_harvest_id, related_user_id, transaction_date) VALUES
('SALE', 1200.00, 'Sold 500 kg tomatoes to Central Market', 2, NULL, '2025-07-03'),
('SALE', 2400.00, 'Sold 1000 kg wheat to distributor', 3, NULL, '2025-07-02'),
('PURCHASE', 320.00, 'Bought NPK fertilizer for Field A-2', NULL, NULL, '2025-07-01'),
('PAYMENT', 4800.00, 'Ahmad Hassan salary - harvest payment', NULL, 2, '2025-07-03'),
('PAYMENT', 2560.00, 'Mohammad Khaled salary - harvest payment', NULL, 3, '2025-06-28'),
('PURCHASE', 60.00, 'Water cost: 15 m3 x 4 NIS', NULL, NULL, '2025-07-05'),
('SALE', 3200.00, 'Sold 800 kg olives to olive press', 4, NULL, '2025-06-30'),
('PAYMENT', 500.00, 'Sami Khalil salary - plowing', NULL, 4, '2025-06-25');
