-- MySQL dump 10.11
--
-- Host: localhost    Database: flowservice
-- ------------------------------------------------------
-- Server version	5.0.45-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `flowservice`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `${flowservice.db.name}` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `${flowservice.db.name}`;

--
-- Table structure for table `flow`
--

DROP TABLE IF EXISTS `flow`;
CREATE TABLE `flow` (
  `id` bigint(20) NOT NULL auto_increment,
  `creatorId` bigint(20) NOT NULL,
  `dateCreated` datetime NOT NULL,
  `description` text NOT NULL,
  `keyWords` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `isTemplate` bit(1) NOT NULL,
  `uri` text NOT NULL,
  `instanceOf` bigint(20) default NULL,
  `typeName` varchar(255) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FK30012E4E20E557` (`instanceOf`)
) ENGINE=MyISAM AUTO_INCREMENT=132 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `flow`
--

LOCK TABLES `flow` WRITE;
/*!40000 ALTER TABLE `flow` DISABLE KEYS */;
INSERT INTO `flow` VALUES (2,-2,'2010-01-23 12:21:37','This algorithm performs structural segmentation (e.g. segmenting music into verse, chorus, bridge, etc) on music. It was submitted to the 2009 iteration of MIREX. This flow performs the segmentation of the music, and subsequently evaluates the algorithms ','analysis, structure, segmentation, evaluation, mirex','Classification: Andreas F. Ehmann','','http://imirsel.org/classification/classification_ehmann/classification_ehmann/',NULL,'Classification'),(3,-2,'2010-01-23 12:21:37','','extraction jAudio','Feature Extraction using jAudio','','http://imirsel.org/feature_extraction/jaudio_feature_extractor/jaudio_feature_extractor/',NULL,'Feature Extraction'),(4,-2,'2010-01-23 12:21:37','This flow executes George Tzanetakis\'s marsyas program to classify audio files. Features are first extracted from audio files. A classifier is then trained on a training set and used to classify unknown music files. The classification performance is evalu','classification, evaluation, mirex','Classification: Marsyas','','http://imirsel.org/classification/classification_marsyas/classification_marsyas/',NULL,'Classification'),(5,-2,'2010-01-23 12:21:37','This algorithm extracts the frequency contour of the main melody in a piece of polyphonic music. It was submitted to the 2009 iteration of MIREX. This flow runs Dressler\'s algorithm on a piece of music, and plots the extracted melody contour against a hum','analysis, melody, evaluation, mirex','Melody Extraction using Dressler','','http://imirsel.org/analysis/melody/melody_dressler/melody_dressler/',NULL,'Feature Extraction'),(6,-2,'2010-01-23 12:21:37','This algorithm extracts the frequency contour of the main melody in a piece of polyphonic music. It was submitted to the 2009 iteration of MIREX. This flow runs Durrieu\'s SIMM algorithm on a piece of music, and plots the extracted melody contour against a','analysis, melody, evaluation, mirex','Melody Extraction using Durrieu','','http://test.org/datatypetest/',NULL,'Feature Extraction'),(7,-2,'2010-01-23 12:21:37','This flow performs phase vocoder analysis. The fundamental frequency of the input is assumed to be not changing much over time. Hence, this analysis is most appropriate for individual notes. The analysis frequency is estimated by performing a monophonic p','phase-vocoder analysis','Phase Vocoder Analysis','','http://imirsel.org/analysis/phase_vocoder/phase_vocoder/phase_vocoder/',NULL,'Analysis'),(8,-2,'2010-01-23 12:21:37','This algorithm performs structural segmentation (e.g. segmenting music into verse, chorus, bridge, etc) on music. It was submitted to the 2009 iteration of MIREX. This flow performs the segmentation of the music, and subsequently evaluates the algorithms ','analysis, structure, segmentation, evaluation, mirex','Structural Segmentation by Mauch','','http://imirsel.org/analysis/structure/structure_mauch/structure_mauch/',NULL,'Analysis'),(10,-2,'2010-01-23 12:21:37','Updates the MIREX evaluation results to the task you are submitting to.','mirex evaluation','Update MIREX Evaluation Results','','http://imirsel.org/evaluation/mirex_evaluation/mirex_evaluation/',NULL,'Analysis'),(11,-2,'2010-01-23 12:21:37','This flow executes Klaus Seyerlehner and Markus Schedle to classify audio files. Features are first extracted from audio files. A classifier is then trained on a training set and used to classify unknown music files. The classification performance is eval','classification, evaluation, mirex','Classification: Seyerlehner','','http://imirsel.org/classification/classification_seyerlehner/classification_seyerlehner/',NULL,'Classification');
/*!40000 ALTER TABLE `flow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `job`
--

DROP TABLE IF EXISTS `job`;
CREATE TABLE `job` (
  `id` bigint(20) NOT NULL auto_increment,
  `description` longtext,
  `endTimestamp` datetime default NULL,
  `execPort` int(11) default NULL,
  `executionInstanceId` longtext,
  `host` varchar(255) default NULL,
  `name` varchar(255) NOT NULL,
  `numTries` int(11) NOT NULL,
  `ownerEmail` varchar(255) NOT NULL,
  `ownerId` bigint(20) NOT NULL,
  `port` int(11) default NULL,
  `startTimestamp` datetime default NULL,
  `statusCode` int(11) NOT NULL,
  `submitTimestamp` datetime default NULL,
  `token` varchar(255) NOT NULL,
  `updateTimestamp` datetime default NULL,
  `flowInstanceId` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FK19BBD781BC449` (`flowInstanceId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `job`
--

LOCK TABLES `job` WRITE;
/*!40000 ALTER TABLE `job` DISABLE KEYS */;
/*!40000 ALTER TABLE `job` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jobResult`
--

DROP TABLE IF EXISTS `jobResult`;
CREATE TABLE `jobResult` (
  `id` bigint(20) NOT NULL auto_increment,
  `resultType` varchar(255) NOT NULL,
  `url` longtext NOT NULL,
  `jobId` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FK40CDCE3A40A162B8` (`jobId`),
  KEY `FK40CDCE3A3A97C23B` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `jobResult`
--

LOCK TABLES `jobResult` WRITE;
/*!40000 ALTER TABLE `jobResult` DISABLE KEYS */;
/*!40000 ALTER TABLE `jobResult` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` bigint(20) NOT NULL auto_increment,
  `dateCreated` datetime NOT NULL,
  `message` longtext NOT NULL,
  `recipientEmail` varchar(255) NOT NULL,
  `recipientId` bigint(20) NOT NULL,
  `deliveryStatusCode` int(11) default NULL,
  `errorMessage` varchar(255) default NULL,
  `subject` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-02-22 19:18:07
/*grant permissions to the user*/
grant all privileges on ${flowservice.db.name}.* to '${jdbc.username}'@'localhost' identified by '${jdbc.password}'

flush privileges;


