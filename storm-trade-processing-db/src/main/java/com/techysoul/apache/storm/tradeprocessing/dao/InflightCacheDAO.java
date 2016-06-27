package com.techysoul.apache.storm.tradeprocessing.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.techysoul.apache.storm.tradeprocessing.datasource.DataSource;
import com.techysoul.apache.storm.tradeprocessing.utils.TradeProcessingConfig;
import com.techysoul.apache.storm.tradeprocessing.utils.TradeProcessingConstants;

public class InflightCacheDAO implements TradeProcessingConstants {

  private static final Logger LOGGER = Logger.getLogger(InflightCacheDAO.class);
  private static final TradeProcessingConfig CONFIG = TradeProcessingConfig.getInstance();

  public void insert(int tradeId, String tradeData) throws SQLException {
    LOGGER.info("Inserting into InflightCache table for tradeId = " + tradeId);
    Connection connection = null;
    Statement statement = null;
    PreparedStatement preparedStatement = null;
    try {
      // Setup the connection with the DB
      connection = DataSource.getInstance().getConnection();

      // Statements allow to issue SQL queries to the database
      statement = connection.createStatement();

      // PreparedStatements can use variables and are more efficient
      preparedStatement = connection.prepareStatement(CONFIG.get("INFLIGHT_CACHE_INSERT"));

      // Parameters start with 1
      int attrCount = 1;
      preparedStatement.setInt(attrCount, tradeId);
      for (String tradeAttr : tradeData.split(COMMA_SEPARATOR)) {
        attrCount++;
        preparedStatement.setString(attrCount, tradeAttr);
      }
      preparedStatement.executeUpdate();

    } catch (Exception e) {
      LOGGER.error("DB Exception:Error occured while inserting into Inflight Cache {}", e);
    } finally {

      if (statement != null) {
        statement.close();
      }

      if (connection != null) {
        connection.close();
      }
    }

  }


  public void remove(int tradeId) throws SQLException {
    LOGGER.info("Removing from InflightCache table for tradeID = " + tradeId);
    Connection connection = null;
    Statement statement = null;
    PreparedStatement preparedStatement = null;
    try {

      // Setup the connection with the DB
      connection = DataSource.getInstance().getConnection();

      // Statements allow to issue SQL queries to the database
      statement = connection.createStatement();

      // Remove again the insert comment
      preparedStatement = connection.prepareStatement(CONFIG.get("INFLIGHT_CACHE_DELETE"));
      preparedStatement.setInt(1, tradeId);
      preparedStatement.executeUpdate();

    } catch (Exception e) {
      LOGGER.error("DB Exception:Error occured while deleting from Inflight Cache {}", e);
    } finally {

      if (statement != null) {
        statement.close();
      }

      if (connection != null) {
        connection.close();
      }
    }

  }


  public int getSize() throws SQLException {
    int size = 0;
    ResultSet resultSet = null;
    Connection connection = null;
    Statement statement = null;
    PreparedStatement preparedStatement = null;
    try {

      // Setup the connection with the DB
      connection = DataSource.getInstance().getConnection();
      // Statements allow to issue SQL queries to the database
      statement = connection.createStatement();
      // Result set get the result of the SQL query
      resultSet = statement.executeQuery(CONFIG.get("INFLIGHT_CACHE_COUNT"));
      if (resultSet.next()) {
        size = resultSet.getInt(1);
      }
    } catch (Exception e) {
      LOGGER.error("DB Exception:Error occured while getting Inflight Cache size {}", e);
    } finally {
      if (resultSet != null) {
        resultSet.close();
      }

      if (statement != null) {
        statement.close();
      }

      if (connection != null) {
        connection.close();
      }
    }
    return size;

  }


}
