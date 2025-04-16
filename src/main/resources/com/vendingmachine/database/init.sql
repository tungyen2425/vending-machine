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

INSERT INTO products (name, price, quantity, image_url) VALUES
('Nước suối', 10000, 50, 'nuockhoang.png'),
('Coca Cola', 10000, 50, 'coca.png'),
('Nước cam', 12000, 50, 'fanta.png'),
('Trà xanh', 12000, 50, 'traxanh.png'),
('Chanh muối', 10000, 50, 'revive.png'),
('Nước tăng lực', 20000, 50, 'redbull.png'),
('Aquafina', 8000, 50, 'nuockhoang.png'),
('Pepsi', 10000, 50, 'coca.png');

INSERT INTO transactions (product_id, quantity, total_price) VALUES
(1, 2, 20000),  -- Mua 2 nước suối
(2, 1, 10000),  -- Mua 1 coca
(3, 3, 36000),  -- Mua 3 nước cam
(4, 1, 12000),  -- Mua 1 trà xanh
(5, 2, 20000),  -- Mua 2 chanh muối
(6, 1, 20000),  -- Mua 1 nước tăng lực
(7, 3, 24000),  -- Mua 3 aquafina
(8, 2, 20000);  -- Mua 2 pepsi