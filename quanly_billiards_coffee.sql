-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Mar 15, 2026 at 10:07 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `quanly_billiards_coffee`
--

-- --------------------------------------------------------

--
-- Table structure for table `billiard_sessions`
--

CREATE TABLE `billiard_sessions` (
  `session_id` int(11) NOT NULL,
  `table_id` int(11) DEFAULT NULL,
  `order_id` int(11) DEFAULT NULL,
  `start_time` datetime DEFAULT current_timestamp(),
  `end_time` datetime DEFAULT NULL,
  `price_per_hour` decimal(10,2) DEFAULT 120000.00,
  `total_price` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `billiard_sessions`
--

INSERT INTO `billiard_sessions` (`session_id`, `table_id`, `order_id`, `start_time`, `end_time`, `price_per_hour`, `total_price`) VALUES
(1, 1, NULL, '2026-03-07 02:51:56', '2026-03-07 02:54:18', 120000.00, 4700.00),
(2, 2, NULL, '2026-03-07 02:52:07', '2026-03-07 02:55:05', 120000.00, 5933.00),
(3, 6, NULL, '2026-03-07 02:52:16', '2026-03-07 03:05:10', 120000.00, 25800.00),
(4, 3, NULL, '2026-03-07 02:52:28', '2026-03-07 03:05:06', 120000.00, 25233.00),
(5, 2, NULL, '2026-03-07 03:12:59', '2026-03-07 03:15:26', 120000.00, 4900.00),
(6, 2, NULL, '2026-03-07 04:01:20', '2026-03-07 04:13:04', 120000.00, 23467.00),
(7, 2, NULL, '2026-03-07 11:30:11', '2026-03-15 10:43:06', 120000.00, 22945833.00),
(8, 1, NULL, '2026-03-07 11:30:13', '2026-03-07 11:30:25', 120000.00, 0.00),
(9, 1, NULL, '2026-03-15 11:55:12', '2026-03-15 11:55:44', 120000.00, 1033.00),
(10, 3, 26, '2026-03-15 12:02:20', '2026-03-15 12:02:34', 120000.00, 467.00),
(11, 2, 27, '2026-03-15 12:02:57', '2026-03-15 12:03:43', 120000.00, 1500.00),
(12, 8, 28, '2026-03-15 12:06:25', '2026-03-15 12:06:41', 120000.00, 533.00),
(13, 1, 29, '2026-03-15 12:12:17', '2026-03-15 12:12:28', 120000.00, 333.00),
(14, 4, 31, '2026-03-15 12:22:06', '2026-03-15 12:22:32', 120000.00, 833.00),
(15, 5, 32, '2026-03-15 12:34:23', '2026-03-15 12:34:26', 120000.00, 67.00),
(16, 6, 34, '2026-03-15 14:06:48', '2026-03-15 14:08:44', 120000.00, 3833.00),
(17, 1, 36, '2026-03-15 14:23:21', '2026-03-15 14:38:08', 120000.00, 29567.00),
(18, 4, 37, '2026-03-15 14:23:24', '2026-03-15 14:38:16', 120000.00, 29700.00),
(19, 5, 38, '2026-03-15 14:23:25', '2026-03-15 14:38:19', 120000.00, 29767.00),
(20, 2, 39, '2026-03-15 14:23:28', '2026-03-15 14:38:12', 120000.00, 29433.00);

-- --------------------------------------------------------

--
-- Table structure for table `billiard_tables`
--

CREATE TABLE `billiard_tables` (
  `table_id` int(11) NOT NULL,
  `table_name` varchar(50) NOT NULL,
  `status` enum('TRONG','CO_KHACH') DEFAULT 'TRONG'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `billiard_tables`
--

INSERT INTO `billiard_tables` (`table_id`, `table_name`, `status`) VALUES
(1, 'Bàn 1', 'TRONG'),
(2, 'Bàn 2', 'TRONG'),
(3, 'Bàn 3', 'TRONG'),
(4, 'Bàn 4', 'TRONG'),
(5, 'Bàn 5', 'TRONG'),
(6, 'Bàn 6', 'TRONG'),
(7, 'Bàn 7', 'TRONG'),
(8, 'Bàn 8', 'TRONG'),
(9, 'Bàn 9', 'TRONG'),
(10, 'Bàn 10', 'TRONG');

-- --------------------------------------------------------

--
-- Table structure for table `drinks`
--

CREATE TABLE `drinks` (
  `drink_id` int(11) NOT NULL,
  `drink_name` varchar(100) NOT NULL,
  `price` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `drinks`
--

INSERT INTO `drinks` (`drink_id`, `drink_name`, `price`) VALUES
(1, 'Cà phê đen đá', 20000.00),
(2, 'Cà phê sữa đá', 25000.00),
(3, 'Bạc xỉu', 28000.00),
(4, 'Sting vàng', 15000.00),
(5, 'Redbull', 20000.00),
(6, '7 Up', 15000.00),
(7, 'Trà đào', 30000.00),
(8, 'Lipton đá', 25000.00),
(9, 'Bia Tiger', 25000.00),
(10, 'Khô mực', 100000.00),
(11, 'Cafe muối', 25000.00),
(12, 'Mì xào trứng', 20000.00),
(13, 'Đá chanh', 15000.00),
(14, 'Trà tắc', 15000.00),
(15, 'Cacao nóng', 25000.00),
(16, 'Soda việt quất', 20000.00),
(17, 'Cam vắt', 25000.00),
(18, 'Trà vải', 30000.00),
(19, 'Yogurt đá', 20000.00),
(20, 'Milo dầm', 25000.00),
(21, 'Trà chanh mật ong', 20000.00),
(22, 'Nước ép dưa hấu', 30000.00);

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `order_id` int(11) NOT NULL,
  `order_time` datetime DEFAULT current_timestamp(),
  `total_amount` decimal(12,2) DEFAULT 0.00,
  `status` varchar(20) DEFAULT 'PAID',
  `table_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `orders`
--

INSERT INTO `orders` (`order_id`, `order_time`, `total_amount`, `status`, `table_id`) VALUES
(1, '2026-03-01 09:15:22', 45000.00, 'PAID', 1),
(2, '2026-03-01 14:30:45', 120000.00, 'PAID', 3),
(3, '2026-03-02 10:00:10', 55000.00, 'PAID', 2),
(4, '2026-03-02 18:45:30', 215000.00, 'PAID', 5),
(5, '2026-03-02 21:20:15', 85000.00, 'PAID', 6),
(6, '2026-03-03 08:30:00', 30000.00, 'PAID', 1),
(7, '2026-03-03 15:10:12', 150000.00, 'PAID', 4),
(8, '2026-03-04 11:20:45', 65000.00, 'PAID', 7),
(9, '2026-03-04 20:05:33', 320000.00, 'PAID', 10),
(10, '2026-03-05 09:45:18', 40000.00, 'PAID', 2),
(11, '2026-03-05 16:30:55', 95000.00, 'PAID', 3),
(12, '2026-03-06 13:20:10', 110000.00, 'PAID', 8),
(13, '2026-03-06 22:15:40', 180000.00, 'PAID', 9),
(14, '2026-03-07 01:10:25', 75000.00, 'PAID', 1),
(15, '2026-03-07 02:30:00', 50000.00, 'PAID', 4),
(16, '2026-03-07 02:52:01', 44700.00, 'PAID', 1),
(17, '2026-03-07 02:52:08', 89933.00, 'PAID', 2),
(18, '2026-03-07 02:52:16', 225800.00, 'PAID', 6),
(19, '2026-03-07 02:52:20', 225233.00, 'PAID', 3),
(20, '2026-03-07 03:13:02', 84900.00, 'PAID', 2),
(21, '2026-03-07 04:01:23', 53467.00, 'PAID', 2),
(22, '2026-03-07 11:30:17', 70367.00, 'PAID', 1),
(23, '2026-03-15 10:43:04', 22965833.00, 'PAID', 2),
(24, '2026-03-15 11:55:10', 16033.00, 'PAID', 1),
(25, '2026-03-15 12:02:17', 40467.00, 'PAID', 3),
(26, '2026-03-15 12:02:20', 40467.00, 'PAID', 3),
(27, '2026-03-15 12:02:57', 1500.00, 'PAID', 2),
(28, '2026-03-15 12:06:25', 533.00, 'PAID', 8),
(29, '2026-03-15 12:12:17', 20333.00, 'PAID', 1),
(30, '2026-03-15 12:14:43', 56000.00, 'PAID', 2),
(31, '2026-03-15 12:22:06', 15833.00, 'PAID', 4),
(32, '2026-03-15 12:34:23', 67.00, 'PAID', 5),
(33, '2026-03-15 14:06:48', 18833.00, 'PAID', 6),
(34, '2026-03-15 14:06:48', 18833.00, 'PAID', 6),
(35, '2026-03-15 14:15:55', 40000.00, 'PAID', 4),
(36, '2026-03-15 14:23:21', 29567.00, 'PAID', 1),
(37, '2026-03-15 14:23:24', 29700.00, 'PAID', 4),
(38, '2026-03-15 14:23:25', 29767.00, 'PAID', 5),
(39, '2026-03-15 14:23:28', 94433.00, 'PAID', 2);

-- --------------------------------------------------------

--
-- Table structure for table `order_details`
--

CREATE TABLE `order_details` (
  `detail_id` int(11) NOT NULL,
  `order_id` int(11) DEFAULT NULL,
  `drink_id` int(11) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `price` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `order_details`
--

INSERT INTO `order_details` (`detail_id`, `order_id`, `drink_id`, `quantity`, `price`) VALUES
(1, 16, 1, 2, 20000.00),
(2, 17, 3, 3, 28000.00),
(3, 18, 10, 2, 100000.00),
(4, 19, 10, 1, 100000.00),
(5, 19, 9, 4, 25000.00),
(6, 20, 2, 2, 25000.00),
(7, 20, 7, 1, 30000.00),
(8, 21, 6, 2, 15000.00),
(9, 22, 1, 1, 20000.00),
(10, 22, 11, 2, 25000.00),
(11, 23, 1, 1, 20000.00),
(12, 24, 6, 1, 15000.00),
(13, 25, 6, 1, 15000.00),
(14, 25, 11, 1, 25000.00),
(15, 29, 12, 1, 20000.00),
(16, 30, 3, 2, 28000.00),
(17, 31, 6, 1, 15000.00),
(18, 33, 6, 1, 15000.00),
(19, 35, 16, 2, 20000.00),
(20, 39, 6, 1, 15000.00),
(21, 39, 2, 1, 25000.00),
(22, 39, 11, 1, 25000.00);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `role` enum('ADMIN','NHANVIEN') DEFAULT 'NHANVIEN'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `password`, `role`) VALUES
(1, 'ad', '1', 'ADMIN'),
(2, 'nv', '1', 'NHANVIEN');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `billiard_sessions`
--
ALTER TABLE `billiard_sessions`
  ADD PRIMARY KEY (`session_id`),
  ADD KEY `table_id` (`table_id`),
  ADD KEY `order_id` (`order_id`);

--
-- Indexes for table `billiard_tables`
--
ALTER TABLE `billiard_tables`
  ADD PRIMARY KEY (`table_id`);

--
-- Indexes for table `drinks`
--
ALTER TABLE `drinks`
  ADD PRIMARY KEY (`drink_id`);

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`order_id`),
  ADD KEY `table_id` (`table_id`);

--
-- Indexes for table `order_details`
--
ALTER TABLE `order_details`
  ADD PRIMARY KEY (`detail_id`),
  ADD KEY `order_id` (`order_id`),
  ADD KEY `drink_id` (`drink_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `billiard_sessions`
--
ALTER TABLE `billiard_sessions`
  MODIFY `session_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `billiard_tables`
--
ALTER TABLE `billiard_tables`
  MODIFY `table_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `drinks`
--
ALTER TABLE `drinks`
  MODIFY `drink_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `order_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=40;

--
-- AUTO_INCREMENT for table `order_details`
--
ALTER TABLE `order_details`
  MODIFY `detail_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `billiard_sessions`
--
ALTER TABLE `billiard_sessions`
  ADD CONSTRAINT `billiard_sessions_ibfk_1` FOREIGN KEY (`table_id`) REFERENCES `billiard_tables` (`table_id`),
  ADD CONSTRAINT `billiard_sessions_ibfk_2` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`);

--
-- Constraints for table `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`table_id`) REFERENCES `billiard_tables` (`table_id`);

--
-- Constraints for table `order_details`
--
ALTER TABLE `order_details`
  ADD CONSTRAINT `order_details_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  ADD CONSTRAINT `order_details_ibfk_2` FOREIGN KEY (`drink_id`) REFERENCES `drinks` (`drink_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
