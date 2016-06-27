package com.techysoul.apache.storm.tradeprocessing.spouts;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

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

import com.techysoul.apache.storm.tradeprocessing.dao.InflightCacheDAO;
import com.techysoul.apache.storm.tradeprocessing.utils.TradeProcessingConfig;
import com.techysoul.apache.storm.tradeprocessing.utils.TradeProcessingConstants;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class TradeCollectorSpout extends BaseRichSpout
    implements MessageListener, TradeProcessingConstants {

  /**
   * 
   */
  private static final long serialVersionUID = -461077232117481532L;

  private static final Logger LOGGER = Logger.getLogger(TradeCollectorSpout.class);

  private static final TradeProcessingConfig CONFIG = TradeProcessingConfig.getInstance();

  SpoutOutputCollector _collector;
  BufferedReader bufferedReader;
  private Connection connection;
  private Session session;
  private InflightCacheDAO inflightCacheDAO;
  boolean isbufferClosed = false;
  private Queue queue = new LinkedBlockingQueue<>();
  private Map<Integer, Message> inflightCache = new HashMap<>();

  @Override
  public void nextTuple() {
    try {
      Message message = (Message) queue.poll();

      if (message != null) {
        String txtmsg = ((TextMessage) message).getText();
        Values vals = new Values(txtmsg);
        LOGGER.info("Going to emit:" + txtmsg);
        if (txtmsg != null)
          _collector.emit(vals, txtmsg.hashCode());
        // LOGGER.info("Acknowledging Message post-emit:" + txtmsg);
        // message.acknowledge();
      }
    } catch (Exception e) {
      LOGGER.error(e);
    }
  }

  @Override
  public void open(Map arg0, TopologyContext arg1, SpoutOutputCollector arg2) {
    try {
      _collector = arg2;
      this.setUpJMSConnection();
      inflightCacheDAO = new InflightCacheDAO();
    } catch (Exception e) {
      LOGGER.error(e);
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer arg0) {
    arg0.declare(new Fields(TRD_FIELDS));

  }

  private void setJMSConnection(String clientid) {
    ConnectionFactory connectionFactory =
        new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);

    try {
      connection = connectionFactory.createConnection();
      if (clientid != null)
        connection.setClientID(clientid);
      session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
    } catch (Exception e) {
      LOGGER.error(e);
    }
  }

  private void setUpJMSConnection() throws JMSException {
    this.setJMSConnection("clientid");
    Topic topic = session.createTopic(CONFIG.get("UPSTREAM_TOPIC_NAME"));
    TopicSubscriber subscriber =
        session.createDurableSubscriber(topic, CONFIG.get("DURABLE_SUBSCRIBER_NAME"));// (topic);
    subscriber.setMessageListener(this);
    LOGGER.info("About to connect..");
    connection.start();
    LOGGER.info("Consumption should begin now..");
  }

  public void onMessage(Message message) {
    try {
      String txtmsg = ((TextMessage) message).getText();
      LOGGER.info("Received message :-" + txtmsg);
      inflightCacheDAO.insert(txtmsg.hashCode(), txtmsg);
      inflightCache.put(txtmsg.hashCode(), message);
      queue.offer(message);
    } catch (Exception e) {
      LOGGER.error(e);
    }
  }

  @Override
  public void fail(Object msgId) {
    LOGGER.info("Failed as Trade could not be processed in 30 seconds : " + msgId);
    LOGGER.info("Re-Initiating the processing for Failed Trade : " + msgId);
    // Re-Initiating the Processing for trades failed due to time-outs
    queue.offer(inflightCache.get(msgId));
    super.fail(msgId);
  }

  @Override
  public void ack(Object msgId) {
    LOGGER.info("Removing Trade from Inflight Cache after successful processing : " + msgId);
    try {
      Message message = inflightCache.get(msgId);
      if (message != null) {
        inflightCacheDAO.remove(Integer.parseInt(msgId.toString()));
        message.acknowledge();
        inflightCache.remove(msgId);
      } else {
        LOGGER.error(
            "Unable to locate Trade from Inflight Cache after successful processing : " + msgId);
      }
    } catch (Exception e) {
      LOGGER.error("Exception : Couldn't ACK Message for Id=" + msgId + "Error is -> " + e);
    }

    super.ack(msgId);
  }
}
