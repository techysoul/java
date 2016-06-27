CREATE USER 'tp_db_user'@'%' IDENTIFIED WITH mysql_native_password AS '***';GRANT ALL PRIVILEGES ON *.* TO 'tp_db_user'@'%' REQUIRE NONE WITH GRANT OPTION MAX_QUERIES_PER_HOUR 0 MAX_CONNECTIONS_PER_HOUR 0 MAX_UPDATES_PER_HOUR 0 MAX_USER_CONNECTIONS 0;GRANT ALL PRIVILEGES ON `tp_db_user\_%`.* TO 'tp_db_user'@'%';

CREATE TABLE `tpdb`.`inflight_cache` ( `tradeId` BIGINT NOT NULL , `sourceTradeId` INT NOT NULL , `legalEntityCode` VARCHAR(5) NOT NULL , `notionalValue` VARCHAR(10) NOT NULL , `tradeBookingTimeStamp` VARCHAR(30) NOT NULL ) ENGINE = MyISAM COMMENT = 'Table to hold inflight trades during processing';

ALTER TABLE `inflight_cache` ADD `timeInMillis` VARCHAR(20) NOT NULL AFTER `tradeBookingTimeStamp`;

ALTER TABLE `tpdb`.`inflight_cache` ADD PRIMARY KEY(`tradeId`);