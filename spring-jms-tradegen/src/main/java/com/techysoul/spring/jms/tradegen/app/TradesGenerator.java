package com.techysoul.spring.jms.tradegen.app;

import java.io.File;
import java.util.Date;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.util.FileSystemUtils;

@SpringBootApplication
@EnableJms
public class TradesGenerator {

  private static final String TOPIC_NAME = "upstream-trade-booking"; // "top";
                                                                     // //"upstream-trade-booking";
  private static final Logger logger = LoggerFactory.getLogger(TradesGenerator.class);
  @Autowired
  private static ActiveMQConnectionFactoryProperties config;
  private static int tradeCount = 1;
  private static final String SEPARATOR = ",";

  public static void main(String[] args) {
    // Clean out any ActiveMQ data from a previous run
    FileSystemUtils.deleteRecursively(new File("activemq-data"));

    // Launch the application
    ConfigurableApplicationContext context = SpringApplication.run(TradesGenerator.class, args);
    JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);

    // Marking this as Topic - This value is false by default which means it is a Queue
    jmsTemplate.setPubSubDomain(true);

    for (int mCount = 1; mCount <= 1000; mCount++) {
      // Send a message
      MessageCreator messageCreator = new MessageCreator() {
        @Override
        public Message createMessage(Session session) throws JMSException {
          StringBuilder tradeBuilder = new StringBuilder();
          tradeBuilder.append(tradeCount);
          tradeBuilder.append(SEPARATOR);
          tradeBuilder.append("LE-");
          tradeBuilder.append((int) (Math.random() * 10));
          tradeBuilder.append(SEPARATOR);
          tradeBuilder.append("value");
          tradeBuilder.append((int) (Math.random() * 100));
          tradeBuilder.append(SEPARATOR);
          tradeBuilder.append(new Date());
          tradeBuilder.append(SEPARATOR);
          tradeBuilder.append(new Date().getTime());
          tradeCount++;
          return session.createTextMessage(tradeBuilder.toString());
        }
      };
      logger.info("Sending message with ID =" + mCount);
      jmsTemplate.send(TOPIC_NAME, messageCreator);
    }
  }

  @Configuration
  @ConditionalOnClass(ActiveMQConnectionFactory.class)
  @ConditionalOnMissingBean(ConnectionFactory.class)
  @EnableConfigurationProperties(ActiveMQConnectionFactoryProperties.class)
  protected static class ActiveMQConnectionFactoryCreator {

    @Autowired
    private ActiveMQConnectionFactoryProperties config;

    @Bean
    ConnectionFactory jmsConnectionFactory() {
      if (this.config.isPooled()) {
        PooledConnectionFactory pool = new PooledConnectionFactory();
        pool.setConnectionFactory(new ActiveMQConnectionFactory(this.config.getBrokerURL()));
        return pool;
      } else {
        return new ActiveMQConnectionFactory(this.config.getBrokerURL());
      }
    }

  }

  @ConfigurationProperties(value = "spring.activemq")
  public static class ActiveMQConnectionFactoryProperties {

    private String brokerURL = "tcp://localhost:61616";
    private String topicName = TOPIC_NAME;
    private String activMQFileName = "activemq-data";
    private boolean inMemory = false;
    private boolean pooled = false;

    public String getActivMQFileName() {
      return activMQFileName;
    }

    public String getTopicName() {
      return this.topicName;
    }

    // Will override brokerURL if inMemory is set to true
    public String getBrokerURL() {
      if (this.inMemory) {
        return "vm://localhost";
      } else {
        return this.brokerURL;
      }
    }

    public boolean isPooled() {
      return pooled;
    }

    /*
     * @Bean // Strictly speaking this bean is not necessary as boot creates a default
     * JmsListenerContainerFactory<?> myJmsContainerFactory(ConnectionFactory connectionFactory) {
     * SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
     * factory.setConnectionFactory(connectionFactory); return factory; }
     */

  }
}
