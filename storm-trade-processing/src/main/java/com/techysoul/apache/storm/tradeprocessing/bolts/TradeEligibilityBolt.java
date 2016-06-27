package com.techysoul.apache.storm.tradeprocessing.bolts;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import org.apache.log4j.Logger;

import com.techysoul.apache.storm.tradeprocessing.utils.TradeProcessingConfig;
import com.techysoul.apache.storm.tradeprocessing.utils.TradeProcessingConstants;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.utils.Utils;

public class TradeEligibilityBolt extends BaseRichBolt implements TradeProcessingConstants {

  private static final long serialVersionUID = 5434908148047359830L;
  private static final TradeProcessingConfig CONFIG = TradeProcessingConfig.getInstance();

  private static final Logger LOGGER = Logger.getLogger(TradeEligibilityBolt.class);
  File file;
  private OutputCollector _collector;

  @Override
  public void execute(Tuple tuple) {
    try {
      // Parsing Incoming Trade
      String[] tradeDetails = tuple.getString(0).split(COMMA_SEPARATOR);
      // Checking if the incoming trade is in valid format
      if (tradeDetails != null && tradeDetails.length > 1) {
        // Checking the eligibility of the trade for reporting
        if (CONFIG.is("ELIGIBILITY_TIME_DELAY_ON")) {
          Utils.sleep(CONFIG.getLong("ELIGIBILITY_CHECK_TIME"));
        }
        if (this.isTradeEligible(tradeDetails[1])) {
          LOGGER.info("Emitting Trade as ELIGIBLE");
          _collector.emit(REPORT_STREAM, tuple.getValues());
        } else {
          LOGGER.info("Emitting Trade as INELIGIBLE");
          _collector.emit(EXCLUDE_STREAM, tuple.getValues());
        }
      }
      // Checking and Performing Ack
      if (CONFIG.is("ACK_ON")) {
        _collector.ack(tuple);
      }
    } catch (Throwable e) {
      LOGGER.error(EXEC_EXCP_MSG, e);
      _collector.fail(tuple);
    }
  }

  /**
   * This method performs eligibility checks based on the given legalEntity and returns the flag
   * 
   * @param legalEntity - Legal Entity
   * @return boolean - Indicating whether the legal entity is reportable or not.
   **/
  private boolean isTradeEligible(String legalEntity) {
    // Checking the input legalEntity against all legal entities in scope
    // Returning the eligibility flag
    return Arrays.asList(CONFIG.get("LEGAL_ENTITIES").split(COMMA_SEPARATOR)).contains(legalEntity);
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer arg0) {
    arg0.declare(new Fields(TRD_FIELDS));
    arg0.declareStream(REPORT_STREAM, new Fields(TRD_FIELDS));
    arg0.declareStream(EXCLUDE_STREAM, new Fields(TRD_FIELDS));
  }

  @Override
  public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
    _collector = arg2;
  }

}
