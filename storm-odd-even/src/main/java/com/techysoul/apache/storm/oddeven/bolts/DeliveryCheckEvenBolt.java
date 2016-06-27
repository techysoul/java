package com.techysoul.apache.storm.oddeven.bolts;

import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

public class DeliveryCheckEvenBolt extends BaseRichBolt {

  /**
  * 
  */
  private static final long serialVersionUID = 35590445506179606L;

  private OutputCollector _collector;

  @Override
  public void execute(Tuple arg0) {
    System.out.println("In Even Bolt:::::::::" + arg0);

  }

  @Override
  public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
    _collector = arg2;
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer arg0) {
    arg0.declare(new Fields("trade"));
  }

}
