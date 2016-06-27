package com.techysoul.apache.storm.tradeprocessing.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradeProcessingConfig implements TradeProcessingConstants {

  private static TradeProcessingConfig instance = null;
  private Properties properties;
  private static final Logger LOGGER = LoggerFactory.getLogger(TradeProcessingConfig.class);

  protected TradeProcessingConfig() throws IOException {
    FileInputStream fis = null;
    try {
      properties = new Properties();
      fis = new FileInputStream(CONFIG_PATH);
      properties.load(fis);
    } catch (IOException ioe) {
      LOGGER.error("Unable to find/parse the properties file at path - {}", CONFIG_PATH);
    } finally {
      fis.close();
    }
  }

  public static TradeProcessingConfig getInstance() {
    if (instance == null) {
      try {
        instance = new TradeProcessingConfig();
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
    return instance;
  }

  private String getValue(String key) {
    String value = null;
    if (key != null && !"".equals(key.trim())) {
      value = properties.getProperty(key);
      if (value != null) {
        value = value.trim();
      }
    }
    LOGGER.info("CONFIG ->> [key=" + key + "] value=[" + value + "]");
    return value;
  }

  public String get(String key) {
    return this.getValue(key);
  }

  public Number getNumber(String key) {
    Number number = null;
    if (this.getValue(key) != null) {
      number = Long.parseLong(this.getValue(key));
    }
    return number;
  }

  public int getInt(String key) {
    int number = 0;
    if (this.getValue(key) != null) {
      number = Integer.parseInt(this.getValue(key));
    }
    return number;
  }

  public long getLong(String key) {
    long number = 0;
    if (this.getValue(key) != null) {
      number = Long.parseLong(this.getValue(key));
    }
    return number;
  }

  public boolean is(String key) {
    boolean flag = false;
    if (this.getValue(key) != null) {
      flag = "TRUE".equalsIgnoreCase(this.getValue(key));
    }
    return flag;
  }

}
