package com.techysoul.apache.storm.tradeprocessing.datasource;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;

import com.techysoul.apache.storm.tradeprocessing.utils.TradeProcessingConfig;

public class DataSource {

  private static final Logger LOGGER = Logger.getLogger(DataSource.class);
  private static DataSource dataSource;
  private BasicDataSource basicDataSource;
  private TradeProcessingConfig CONFIG = TradeProcessingConfig.getInstance();

  private DataSource() throws IOException, SQLException, PropertyVetoException {
    LOGGER.info("Creating datasource instance..");
    basicDataSource = new BasicDataSource();
    basicDataSource.setDriverClassName(CONFIG.get("DB_DRIVER_CLASS_NAME"));
    basicDataSource.setUsername(CONFIG.get("DB_USER_NAME"));
    basicDataSource.setPassword(CONFIG.get("DB_PASSWORD"));
    basicDataSource.setUrl(CONFIG.get("DB_URL"));

    // the settings below are optional -- dbcp can work with defaults
    basicDataSource.setMinIdle(CONFIG.getInt("DB_MIN_IDLE"));
    basicDataSource.setMaxIdle(CONFIG.getInt("DB_MAX_IDLE"));
    basicDataSource.setMaxOpenPreparedStatements(CONFIG.getInt("DB_MAX_OPEN_PS"));

  }

  public static DataSource getInstance() throws IOException, SQLException, PropertyVetoException {
    if (dataSource == null) {
      dataSource = new DataSource();
      return dataSource;
    } else {
      return dataSource;
    }
  }

  public Connection getConnection() throws SQLException {
    return this.basicDataSource.getConnection();
  }

}
