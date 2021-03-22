-- --------------------------------------------------------
-- Host:                         10.0.0.16
-- Server version:               10.5.8-MariaDB - FreeBSD Ports
-- Server OS:                    FreeBSD12.2
-- HeidiSQL Version:             11.1.0.6116
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for scanx
DROP DATABASE IF EXISTS `scanx`;
CREATE DATABASE IF NOT EXISTS `scanx` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_bin */;
USE `scanx`;

-- Dumping structure for table scanx.Inventory
DROP TABLE IF EXISTS `Inventory`;
CREATE TABLE IF NOT EXISTS `Inventory` (
  `USER_ID` int(11) DEFAULT NULL,
  `SERIAL_NUMBER` varchar(30) COLLATE utf8_bin DEFAULT NULL,
  `PRODUCT_TITLE` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `PRICE` decimal(65,2) DEFAULT NULL,
  `QUANTITY_ON_HAND` int(11) DEFAULT NULL,
  `MIN_QUANTITY_BEFORE_NOTIFY` int(11) DEFAULT NULL,
  `LAST_UPDATE` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- Dumping data for table scanx.Inventory: ~5 rows (approximately)
/*!40000 ALTER TABLE `Inventory` DISABLE KEYS */;
REPLACE INTO `Inventory` (`USER_ID`, `SERIAL_NUMBER`, `PRODUCT_TITLE`, `PRICE`, `QUANTITY_ON_HAND`, `MIN_QUANTITY_BEFORE_NOTIFY`, `LAST_UPDATE`) VALUES
	(1, '11111111111', 'Peanut Butter', 9.99, 124, 10, '2021-02-21 16:55:58'),
	(2, '11111111112', 'Granola Bars', 8.99, 99, 40, '2021-02-21 17:02:15'),
	(2, '11111111113', 'Gummy Worms', 2.99, 68, 20, '2021-02-21 17:02:34'),
	(3, '11111111114', 'Donuts', 6.99, 52, 8, '2021-03-17 19:58:57');
/*!40000 ALTER TABLE `Inventory` ENABLE KEYS */;

-- Dumping structure for table scanx.Scans
DROP TABLE IF EXISTS `Scans`;
CREATE TABLE IF NOT EXISTS `Scans` (
  `SCAN_ID` int(11) NOT NULL AUTO_INCREMENT,
  `BARCODE_ID` varchar(30) COLLATE utf8_bin DEFAULT NULL,
  `USER_ID` int(11) DEFAULT NULL,
  `LAST_UPDATE` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`SCAN_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- Dumping data for table scanx.Scans: ~3 rows (approximately)
/*!40000 ALTER TABLE `Scans` DISABLE KEYS */;
REPLACE INTO `Scans` (`SCAN_ID`, `BARCODE_ID`, `USER_ID`, `LAST_UPDATE`) VALUES
	(1, '11111111111', 1, '2021-03-22 01:05:47'),
	(2, '11111111112', 2, '2021-03-22 01:05:56'),
	(3, '11111111113', 3, '2021-03-22 01:06:01');
/*!40000 ALTER TABLE `Scans` ENABLE KEYS */;

-- Dumping structure for table scanx.Users
DROP TABLE IF EXISTS `Users`;
CREATE TABLE IF NOT EXISTS `Users` (
  `USER_ID` int(11) NOT NULL AUTO_INCREMENT,
  `USERNAME` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `PASSWORD` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `LAST_UPDATE` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`USER_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- Dumping data for table scanx.Users: ~2 rows (approximately)
/*!40000 ALTER TABLE `Users` DISABLE KEYS */;
REPLACE INTO `Users` (`USER_ID`, `USERNAME`, `PASSWORD`, `LAST_UPDATE`) VALUES
	(1, 'bob', '469f00d7239c6a23bd2d40396548efab207efeea9e1705f70959a7a3e2111d55', '2021-03-22 01:06:55'),
	(2, 'joe', 'd5fd7e1d51d1a8faa23116d48cbd4569d89d4e006b29b4526637ad06b7cf5701', '2021-03-22 01:07:02'),
	(3, 'alice', '19cfa62e7ab8d0c973c3f9f1c019c4a67551a0399c0cd636a6994f64b604a3c2', '2021-03-22 01:07:50');
/*!40000 ALTER TABLE `Users` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
