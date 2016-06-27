package com.techysoul.apache.storm.tradeprocessing.utils;

public interface TradeProcessingConstants {

  // Generic Constants
  public static final String COMMA_SEPARATOR = ",";

  // Topology Definition - Spouts, Bolts and Streams
  public static final String TRD_PROCESSING_TOPOLOGY = "TradeProcessingTopology";
  public static final String TRD_COLLECTOR_SPOUT = "tradeCollector";
  public static final String TRD_ELIGIBILITY_BOLT = "tradeEligibility";
  public static final String TRD_REPORTING_BOLT = "tradeReport";
  public static final String TRD_EXCLUSION_BOLT = "tradeExclusion";
  public static final String REPORT_STREAM = "report";
  public static final String EXCLUDE_STREAM = "exclude";
  public static final String TRD_FIELDS = "trade";

  // Logger Messages
  public static final String EXEC_EXCP_MSG =
      "An exception occured while processing the trade in execute method - {}";

  // public static final String CONFIG_PATH =
  // "C:/0-DRIVE/Apps/storm-local/trade-processing.properties";
  public static final String CONFIG_PATH =
      "C:/0-DRIVE/workspace/storm-trade-processing/src/main/resources/trade-processing.properties";

  /*
   * //Business Logic Related Config //No 3 and 6 are Non Reportable Legal Entities public static
   * final String[] LEGAL_ENTITIES = {"LE-0","LE-1","LE-2","LE-4","LE-5","LE-7","LE-8","LE-9"};
   * 
   * //Persistence Paths public static final String EXCL_PERSISTENCE_PATH =
   * "C:/0-DRIVE/Apps/storm-local/Exclusion.txt"; public static final String REPT_PERSISTENCE_PATH =
   * "C:/0-DRIVE/Apps/storm-local/Report.txt";
   */

  /*
   * //Topology Cluster Config public static final String LOCAL_NIMBUS_HOST = "localhost"; public
   * static final int LOCAL_NIMBUS_PORT = 6627; public static final int LOCAL_ZOOKEEPER_PORT = 2181;
   * public static final long LOCAL_CLUSTER_RUNTIME = 100000000;
   * 
   * // public static final int NUMBER_OF_WORKERS = 20; public static final int
   * TRD_COLLECTOR_SPOUT_PARALLELISM = 1; public static final int TRD_ELIGIBILITY_BOLT_PARALLELISM =
   * 10; public static final int TRD_REPORTING_BOLT_PARALLELISM = 10; public static final int
   * TRD_EXCLUSION_BOLT_PARALLELISM = 10; public static final int MAX_SPOUT_PENDING =5; public
   * static final int MAX_SPOUT_PENDING_WAIT_MS = 5000; public static final int MAX_TASK_PARALLELISM
   * = 1;// 10; public static final int ELIGIBILITY_CHECK_TIME = 500; public static final int
   * REPORTING_PERSISTENCE_TIME = 800; public static final int EXCLUSION_PERSISTENCE_TIME = 700;
   * 
   * public static final boolean ACK_ON = true; //
   * 
   * //JMS Topic Connection Properties public static final String UPSTREAM_TOPIC_NAME =
   * "upstream-trade-booking"; public static final String DURABLE_SUBSCRIBER_NAME = "pub"; public
   * static final String JMS_CONNECTION_CLIENTID = "clientid";
   */
}
