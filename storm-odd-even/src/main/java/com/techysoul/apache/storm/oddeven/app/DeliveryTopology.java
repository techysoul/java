package com.techysoul.apache.storm.oddeven.app;

import org.apache.log4j.Logger;

import com.techysoul.apache.storm.oddeven.bolts.DeliveryCheckBolt;
import com.techysoul.apache.storm.oddeven.bolts.DeliveryCheckEvenBolt;
import com.techysoul.apache.storm.oddeven.bolts.DeliveryCheckOddBolt;
import com.techysoul.apache.storm.oddeven.spouts.DeliveryCheckSpout;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;

public class DeliveryTopology {

  private static final Logger LOGGER = Logger.getLogger(DeliveryTopology.class);

  public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException {
    TopologyBuilder builder = new TopologyBuilder();

    LOGGER.info("Starting..");
    builder.setSpout("trade", new DeliveryCheckSpout(), 1);
    builder.setBolt("eligibility", new DeliveryCheckBolt(), 10).shuffleGrouping("trade");
    builder.setBolt("odd", new DeliveryCheckOddBolt(), 10).shuffleGrouping("eligibility",
        "oddstream");
    builder.setBolt("even", new DeliveryCheckEvenBolt(), 10).shuffleGrouping("eligibility",
        "evenstream");

    Config conf = new Config();
    conf.setDebug(false);
    conf.setMaxSpoutPending(5);

    if (args != null && args.length > 0) {
      conf.setNumWorkers(1);
      LOGGER.info("Submitting DeliveryTopology");
      StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
    } else {

      LocalCluster cluster = new LocalCluster();
      cluster.submitTopology("DeliveryTopology", conf, builder.createTopology());
      Utils.sleep(100000000);
      cluster.killTopology("DeliveryTopology");
      cluster.shutdown();
    }
  }


}
