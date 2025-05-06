CREATE DATABASE IF NOT EXISTS vending_machine;
USE vending_machine;


CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(20) DEFAULT 'admin',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


INSERT INTO users (username, password, full_name, role) 
VALUES ('admin', 'admin123', 'Administrator', 'admin');

CREATE TABLE IF NOT EXISTS products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT,
    quantity INT NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE IF NOT EXISTS vending_machine (
    id INT PRIMARY KEY AUTO_INCREMENT,
    machine_code VARCHAR(50) NOT NULL UNIQUE,
    location VARCHAR(255),
    current_balance DECIMAL(10,2) DEFAULT 0,
    last_maintained TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert default vending machine
INSERT INTO vending_machine (machine_code, location, current_balance) 
VALUES ('VM001', 'Main Building', 0);

INSERT INTO products (name, price, quantity, image_url) VALUES
('Nước suối', 10000, 50, 'nuockhoang.png'),
('Coca Cola', 10000, 50, 'coca.png'),
('Nước cam', 12000, 50, 'fanta.png'),
('Trà xanh', 12000, 50, 'traxanh.png'),
('Chanh muối', 10000, 50, 'revive.png'),
('Nước tăng lực', 20000, 50, 'redbull.png'),
('Aquafina', 8000, 50, 'nuockhoang.png'),
('Pepsi', 10000, 50, 'coca.png');

