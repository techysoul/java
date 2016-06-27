package com.techysoul.apache.storm.tradeprocessing.app;

import com.techysoul.apache.storm.tradeprocessing.bolts.TradeEligibilityBolt;
import com.techysoul.apache.storm.tradeprocessing.bolts.TradeExclusionPersistenceBolt;
import com.techysoul.apache.storm.tradeprocessing.bolts.TradeReportPersistenceBolt;
import com.techysoul.apache.storm.tradeprocessing.spouts.TradeCollectorSpout;
import com.techysoul.apache.storm.tradeprocessing.utils.TradeProcessingConfig;
import com.techysoul.apache.storm.tradeprocessing.utils.TradeProcessingConstants;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradeProcessingTopology implements TradeProcessingConstants {

  private static final Logger LOGGER = LoggerFactory.getLogger(TradeProcessingTopology.class);

  private static final TradeProcessingConfig CONFIG = TradeProcessingConfig.getInstance();

  public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException {
    TopologyBuilder builder = new TopologyBuilder();

    LOGGER.info("Building Trade Processing Topology..");

    builder.setSpout(TRD_COLLECTOR_SPOUT, new TradeCollectorSpout(),
        CONFIG.getNumber("TRD_COLLECTOR_SPOUT_PARALLELISM"));

    builder
        .setBolt(TRD_ELIGIBILITY_BOLT, new TradeEligibilityBolt(),
            CONFIG.getNumber("TRD_ELIGIBILITY_BOLT_PARALLELISM"))
        .shuffleGrouping(TRD_COLLECTOR_SPOUT);

    builder
        .setBolt(TRD_REPORTING_BOLT, new TradeReportPersistenceBolt(),
            CONFIG.getNumber("TRD_REPORTING_BOLT_PARALLELISM"))
        .shuffleGrouping(TRD_ELIGIBILITY_BOLT, REPORT_STREAM);

    builder
        .setBolt(TRD_EXCLUSION_BOLT, new TradeExclusionPersistenceBolt(),
            CONFIG.getNumber("TRD_EXCLUSION_BOLT_PARALLELISM"))
        .shuffleGrouping(TRD_ELIGIBILITY_BOLT, EXCLUDE_STREAM);

    Config conf = new Config();
    conf.setDebug(CONFIG.is("DEBUG_FLAG"));
    conf.setNumWorkers(CONFIG.getInt("NUMBER_OF_WORKERS"));
    conf.setMaxTaskParallelism(CONFIG.getInt("MAX_TASK_PARALLELISM"));
    conf.setMaxSpoutPending(CONFIG.getInt("MAX_SPOUT_PENDING"));
    conf.put(Config.TOPOLOGY_SLEEP_SPOUT_WAIT_STRATEGY_TIME_MS,
        CONFIG.getInt("MAX_SPOUT_PENDING_WAIT_MS"));
    conf.put(Config.TOPOLOGY_SPOUT_WAIT_STRATEGY, CONFIG.get("TOPOLOGY_WAIT_STRATEGY"));
    conf.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, CONFIG.getInt("TOPOLOGY_MESSAGE_TIMEOUT_SECS"));
    conf.put(Config.TOPOLOGY_ENABLE_MESSAGE_TIMEOUTS,
        CONFIG.is("TOPOLOGY_ENABLE_MESSAGE_TIMEOUTS"));
    LOGGER.info("Submitting Trade Processing Topology..");
    if (args != null && args.length > 0) {
      conf.put(Config.NIMBUS_HOST, CONFIG.get("LOCAL_NIMBUS_HOST"));
      conf.put(Config.NIMBUS_THRIFT_PORT, CONFIG.getInt("LOCAL_NIMBUS_PORT"));
      conf.put(Config.STORM_ZOOKEEPER_PORT, CONFIG.getInt("LOCAL_ZOOKEEPER_PORT"));
      StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
    } else {
      LocalCluster cluster = new LocalCluster();
      cluster.submitTopology(TRD_PROCESSING_TOPOLOGY, conf, builder.createTopology());
      Utils.sleep(CONFIG.getLong("LOCAL_CLUSTER_RUNTIME"));
      cluster.killTopology(TRD_PROCESSING_TOPOLOGY);
      cluster.shutdown();
    }
  }
}
