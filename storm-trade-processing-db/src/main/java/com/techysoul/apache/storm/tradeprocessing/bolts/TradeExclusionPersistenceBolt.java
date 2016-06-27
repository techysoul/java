package com.techysoul.apache.storm.tradeprocessing.bolts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import com.techysoul.apache.storm.tradeprocessing.utils.TradeProcessingConfig;
import com.techysoul.apache.storm.tradeprocessing.utils.TradeProcessingConstants;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.utils.Utils;

public class TradeExclusionPersistenceBolt extends BaseRichBolt
    implements TradeProcessingConstants {

  /**
  * 
  */
  private static final long serialVersionUID = 5434908128047359830L;

  private static final Logger LOGGER = Logger.getLogger(TradeExclusionPersistenceBolt.class);

  private static final TradeProcessingConfig CONFIG = TradeProcessingConfig.getInstance();

  File file;
  private OutputCollector _collector;

  @Override
  public void execute(Tuple tuple) {
    LOGGER.info("Processing INELIGIBLE Trade");
    try {
      if (CONFIG.is("EXCLUSION_TIME_DELAY_ON")) {
        Utils.sleep(CONFIG.getLong("EXCLUSION_PERSISTENCE_TIME"));
      }
      long newTime = 0;
      FileWriter fileWriter = new FileWriter(CONFIG.get("EXCL_PERSISTENCE_PATH"), true);
      BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
      bufferedWriter.write(tuple.getString(0));
      bufferedWriter.write(COMMA_SEPARATOR);
      bufferedWriter.write(String.valueOf(new Date()));
      bufferedWriter.write(COMMA_SEPARATOR);
      newTime = new Date().getTime();
      bufferedWriter.write(String.valueOf(newTime));
      bufferedWriter.write(COMMA_SEPARATOR);
      bufferedWriter.write(
          String.valueOf(newTime - Long.parseLong(tuple.getString(0).split(COMMA_SEPARATOR)[4])));
      bufferedWriter.newLine();
      bufferedWriter.close();
      // Checking and Performing Ack
      if (CONFIG.is("ACK_ON")) {
        _collector.ack(tuple);
      }
    } catch (Throwable e) {
      LOGGER.error(EXEC_EXCP_MSG, e);
      _collector.fail(tuple);
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer arg0) {
    arg0.declare(new Fields(TRD_FIELDS));
  }


  @Override
  public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
    _collector = arg2;
  }
}
