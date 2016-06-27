package com.techysoul.apache.storm.tradeprocessing.spouts;


import backtype.storm.Config;
import backtype.storm.spout.ISpoutWaitStrategy;

import java.util.Map;

import org.apache.log4j.Logger;

import com.techysoul.apache.storm.tradeprocessing.dao.InflightCacheDAO;
import com.techysoul.apache.storm.tradeprocessing.utils.TradeProcessingConfig;


public class SpoutWaitStrategy implements ISpoutWaitStrategy {

  private static final Logger LOGGER = Logger.getLogger(SpoutWaitStrategy.class);
  long sleepMillis;
  private InflightCacheDAO inflightCacheDAO = new InflightCacheDAO();
  private static final TradeProcessingConfig CONFIG = TradeProcessingConfig.getInstance();

  @Override
  public void prepare(Map conf) {
    sleepMillis =
        ((Number) conf.get(Config.TOPOLOGY_SLEEP_SPOUT_WAIT_STRATEGY_TIME_MS)).longValue();
  }

  @Override
  public void emptyEmit(long streak) {
    try {
      int currentInflightCacheSize = this.inflightCacheDAO.getSize();
      int maxInflightTrades = CONFIG.getInt("MAX_INFLIGHT_TRADES");
      LOGGER.info("Max Inflight Trades=" + maxInflightTrades + " currentInflightCacheSize="
          + currentInflightCacheSize);
      if (currentInflightCacheSize > maxInflightTrades || currentInflightCacheSize == 0) {
        LOGGER.info("Max Inflight Trades Reached - Waiting for " + sleepMillis + " milliSeconds..");
        Thread.sleep(sleepMillis);
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
