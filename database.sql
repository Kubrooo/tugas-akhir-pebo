-- Database: db_oriteh_sapuro
CREATE DATABASE IF NOT EXISTS db_oriteh_sapuro;
USE db_oriteh_sapuro;

-- Table structure for user
CREATE TABLE IF NOT EXISTS user (
    id_user INT PRIMARY KEY,
    nama VARCHAR(100) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL,
    role VARCHAR(50) NOT NULL
);

-- Table structure for produk
CREATE TABLE IF NOT EXISTS produk (
    kode_produk VARCHAR(5) PRIMARY KEY,
    nama_produk VARCHAR(100) NOT NULL,
    harga_produk DOUBLE NOT NULL,
    is_deleted TINYINT(1) DEFAULT 0
);

-- Table structure for transaksi (header)
CREATE TABLE IF NOT EXISTS transaksi (
    no_nota VARCHAR(50) PRIMARY KEY,
    tanggal TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    bayar DOUBLE NOT NULL,
    kembalian DOUBLE NOT NULL,
    total_bayar DOUBLE NOT NULL,
    id_user INT,
    FOREIGN KEY (id_user) REFERENCES user(id_user)
);

-- Table structure for detail_transaksi (items)
CREATE TABLE IF NOT EXISTS detail_transaksi (
    id_detail INT AUTO_INCREMENT PRIMARY KEY,
    no_nota VARCHAR(50),
    kode_produk VARCHAR(5),
    nama_produk VARCHAR(100) NOT NULL,
    harga_satuan DOUBLE NOT NULL,
    jumlah INT NOT NULL,
    subtotal DOUBLE NOT NULL,
    FOREIGN KEY (no_nota) REFERENCES transaksi(no_nota) ON DELETE CASCADE,
    FOREIGN KEY (kode_produk) REFERENCES produk(kode_produk) ON DELETE SET NULL
);

-- Insert sample users with manually assigned id_user (no AUTO_INCREMENT)
INSERT INTO user (id_user, nama, username, password, role) VALUES 
(1, 'Kasir Sapuro', 'kasir', 'kasir123', 'Kasir'),
(2, 'Owner Sapuro', 'owner', 'owner123', 'Owner')
ON DUPLICATE KEY UPDATE nama=VALUES(nama), username=VALUES(username), password=VALUES(password), role=VALUES(role);

-- Insert sample products
INSERT INTO produk (kode_produk, nama_produk, harga_produk) VALUES 
('M001', 'Avocado Fresh Milk', 10000),
('M002', 'Coffee Fresh Milk', 10000),
('M003', 'Chocolatte Fresh Milk', 10000),
('M004', 'Greentea Fresh Milk', 10000),
('M005', 'Oreo Fresh Milk', 10000),
('M006', 'Red Velvet Fresh Milk', 10000),
('M007', 'Taro Fresh Milk', 10000),
('T001', 'Oriteh', 3000),
('T002', 'Teh Kampul', 4000),
('T003', 'Peach Tea', 5000),
('T004', 'Mango Tea', 5000),
('T005', 'Kiwi Tea', 5000),
('T006', 'Blueberry Tea', 5000),
('T007', 'Apple Tea', 7000),
('T008', 'Lemon Tea', 7000),
('T009', 'Blackcurrant Tea', 7000),
('T010', 'Lychee Tea', 7000),
('T011', 'Teh Tarik', 7000),
('T012', 'Thai Tea', 10000),
('T013', 'Milo Tea', 10000)
ON DUPLICATE KEY UPDATE nama_produk=VALUES(nama_produk), harga_produk=VALUES(harga_produk);

