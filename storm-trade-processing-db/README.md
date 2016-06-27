##Storm Trade Processing - FileStore

###Description
Storm Trade Processing consists of an Apache Storm Topology that listens to a JMS Topic for incoming trades in the form a csv message. Each trade is passed to check eligibility depending on the legal entity code from the message. All eligible trades are then persisted into report.txt and in-eligible trades are persisted into exclusion.txt at the specified path.

###Flow Chart
![Alt text](http://g.gravizo.com/g?
  digraph G {
    aize ="4,4";
    TradeBookingSystem [shape=box];
    UpstreamJMS [shape=box style=dotted];
    TradeBookingSystem -> UpstreamJMS [weight=8 style=dotted];
    TradeCollectorSpout [shape=box];
    UpstreamJMS -> TradeCollectorSpout [weight=8 style=dotted];
    TradeCollectorSpout -> TradeEligibilityBolt [weight=8];
    TradeEligibilityBolt -> CheckEligibility;
    edge [color=green];
    CheckEligibility -> TradeReportPersistenceBolt [style=bold,label="Yes"];
    edge [color=red];
    CheckEligibility -> TradeExclusionPersistenceBolt [style=bold,label="No"];
    CheckEligibility [label="Check Eligibility"];
    node [shape=box,style=filled,color=".7 .3 1.0"];
    edge [color=blue];
    TradeCollectorSpout -> Database;
    edge [color=green];
    FileStoreDatabase [label="FileStore / Database"];
    TradeReportPersistenceBolt -> FileStoreDatabase;
    edge [color=red];
    TradeExclusionPersistenceBolt -> FileStoreDatabase;
  }
)

###Pre-Requisites

####Apache ActiveMQ
[Apache ActiveMQ][1] is required as a JMS Platform to Trade Booking System to publish the trades and this application consumes the trades.
Trades can be published manually using the Apache [ActiveMQ GUI][2] or [spring-jms-tradegen][3] can be used to publish required number of trades easily in the required format.

#### MySQL Database
MySQL Database is used to store the trades as soon as they are received as a part of inflight cache. This is required to be able to process the trades even if the entire application goes down during the processing.

####Maven
[Maven][4] is required to build the project and generate the deployable topology jar file.

###Dependencies
All required dependencies are provided in the [pom.xml][5]

###Installation
The deployable topology jar file with all dependent jars is created using following command 
```
mvn clean eclipse:clean install eclipse:eclipse
```
> **Note:** Maven configuration for Apache Storm dependency should NOT include scope as provided if the resultant jar is going to be deployed directly in the eclipse. However this dependency must be specified with this scope as provided if the resultant jar is going to be deployed in the storm cluster.

Database installation can be done using [this][6] script

###Execution
1. Eclipse : Run TradeProcessingTopology as Java Application in eclipse
2. Storm Cluster : All required steps for setting up storm and then running this topology locally are given [here][7]

[1]: http://activemq.apache.org
[2]: http://localhost:8161/admin/browse.jsp?JMSDestination=upstream-trade-booking
[3]: https://github.com/techysoul/java/tree/master/spring-jms-tradegen
[4]: https://maven.apache.org
[5]: https://github.com/techysoul/java/blob/master/storm-trade-processing-db/pom.xml
[6]: https://github.com/techysoul/java/blob/master/storm-trade-processing-db/src/main/resources/databae.sql
[7]: https://www.techysoul.com/java/setting-up-storm-cluster-in-local-machine
