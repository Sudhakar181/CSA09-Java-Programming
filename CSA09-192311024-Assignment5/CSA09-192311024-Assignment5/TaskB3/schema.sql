CREATE DATABASE IF NOT EXISTS transport_db;
USE transport_db;

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(30) PRIMARY KEY,
    pwd VARCHAR(100) NOT NULL
);

INSERT IGNORE INTO users(id, pwd) VALUES ('admin', 'secret123');

CREATE TABLE IF NOT EXISTS bus_pass (
    pass_id VARCHAR(20) PRIMARY KEY,
    reg_no VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    route_no INT NOT NULL,
    amount DOUBLE NOT NULL
);
