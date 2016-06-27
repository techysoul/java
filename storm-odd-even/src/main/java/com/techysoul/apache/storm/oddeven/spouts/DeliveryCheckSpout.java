package com.techysoul.apache.storm.oddeven.spouts;

import java.io.BufferedReader;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
// import java.util.stream.Collector;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class DeliveryCheckSpout extends BaseRichSpout implements MessageListener {

  /**
  * 
  */
  private static final long serialVersionUID = 871217562907112939L;

  private static final Logger LOGGER = Logger.getLogger(DeliveryCheckSpout.class);
  SpoutOutputCollector _collector;
  BufferedReader bufferedReader;
  private Connection connection;
  private Session session;
  boolean isbufferClosed = false;
  private Queue queue = new LinkedBlockingQueue<>();

  @Override
  public void nextTuple() {
    try {
      /*
       * if(!isbufferClosed && bufferedReader.read()!=-1){ String str= bufferedReader.readLine();
       * Values vals = new Values(str);
       */

      /*
       * double waittime = Math.random(); if(waittime<10){ waittime=waittime+200; }else{
       * waittime=(waittime%10) + 300; }
       */
      long startTime = System.currentTimeMillis();
      /*
       * while(System.currentTimeMillis()- startTime <=waittime){ Thread.sleep(20); }
       */
      Message message = (Message) queue.poll();
      // System.out.println("Queue size after poll:"+ queue.size());
      if (message != null) {

        String txtmsg = ((TextMessage) message).getText();
        Values vals = new Values(message.getJMSTimestamp());

        LOGGER.info("Going to emit:" + txtmsg);
        if (txtmsg != null)
          _collector.emit(vals, txtmsg.hashCode() + "qwe");
      }


    } catch (Exception e) {
      LOGGER.error(e);
    }
  }

  @Override
  public void open(Map arg0, TopologyContext arg1, SpoutOutputCollector arg2) {
    /*
     * FileReader fileReader; try { fileReader = new
     * FileReader("C:/proj/Migration/apache-storm-0.9.4/apache-storm-0.9.4/logs/InPut.txt");
     * bufferedReader =new BufferedReader(fileReader); _collector= arg2; } catch
     * (FileNotFoundException e) { // TODO Auto-generated catch block e.printStackTrace(); }
     */

    try {
      _collector = arg2;
      setUpConnection();
    } catch (JMSException e) {
      LOGGER.error(e);
    }

  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer arg0) {
    arg0.declare(new Fields("trade"));

  }

  private void setConnection(String clientid) {
    ConnectionFactory connectionFactory =
        new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);

    try {
      connection = connectionFactory.createConnection();
      if (clientid != null)
        connection.setClientID(clientid);
      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);



    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setUpConnection() throws JMSException {
    setConnection("clientid");
    Topic topic = session.createTopic("upstream-odd-even");
    // connection.setClientID("durable1");
    TopicSubscriber subscriber = session.createDurableSubscriber(topic, "sub");// (topic);
    // subscriber.
    subscriber.setMessageListener(this);
    connection.start();
  }

  public void onMessage(Message message) {
    try {
      LOGGER.info("received message" + ((TextMessage) message).getText());
      double waittime = Math.random();
      /*
       * if(waittime<100){ waittime=waittime+100; }else{ waittime=(waittime%100) + 150; } long
       * startTime = System.currentTimeMillis(); while(System.currentTimeMillis()- startTime
       * <=waittime){
       * 
       * }
       */
      queue.offer(message);
      LOGGER.info("Queue size after offer:" + queue.size());
      // str.toLowerCase();
    } catch (JMSException e) {
      LOGGER.error(e);
    }

  }

  @Override
  public void fail(Object msgId) {
    LOGGER.info("Failed for :" + (String) msgId);
    super.fail(msgId);
  }

  @Override
  public void ack(Object msgId) {
    super.ack(msgId);
  }
}
