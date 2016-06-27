package com.techysoul.apache.storm.oddeven.bolts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class DeliveryCheckBolt extends BaseRichBolt {

  /**
  * 
  */
  private static final long serialVersionUID = 1068748861563415596L;

  private static final Logger LOGGER = Logger.getLogger(DeliveryCheckBolt.class);
  File file;
  private OutputCollector _collector;

  @Override
  public void execute(Tuple arg0) {
    boolean isOdd = false;
    double waittime = Math.random();
    if (waittime < 999) {
      waittime = waittime + 1000;

    } else {
      waittime = (waittime % 1000) + 1500;

    }
    if (!((waittime % 2) > 0.5))
      isOdd = true;
    try {

      // long startTime = System.currentTimeMillis();
      /*
       * while(System.currentTimeMillis()- startTime <=waittime){ Thread.sleep(10); }
       */
      Thread.sleep(1000);
      FileWriter fileWriter = new FileWriter("C:/0-DRIVE/Apps/storm-local/OutPut.txt", true);

      long boltTime = System.currentTimeMillis();
      Long timeInTransit = boltTime - arg0.getLong(0);
      BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
      bufferedWriter.write(timeInTransit.toString());
      bufferedWriter.newLine();
      bufferedWriter.close();
      if (!isOdd) {
        _collector.emit("oddstream", new Values(arg0));
      } else {
        _collector.emit("evenstream", new Values(arg0));
      }
      /*
       * String str ="str"; str.charAt(4);
       */
      _collector.ack(arg0);
    } catch (Throwable e) {
      e.printStackTrace();
      // _collector.fail(arg0);

    }

  }

  @Override
  public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
    // TODO Auto-generated method stub
    _collector = arg2;
    // File file = new File();

  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer arg0) {
    arg0.declare(new Fields("trade"));
    arg0.declareStream("oddstream", new Fields("trade"));
    arg0.declareStream("evenstream", new Fields("trade"));

  }

  private void throwException(Tuple arg0) throws Exception {
    _collector.fail(arg0);
    IOException ioe = new IOException();
  }
}
