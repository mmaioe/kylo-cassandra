package com.mmaioe.spark.datavalidator.cassandra;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.launcher.SparkLauncher;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by mi186020 on 2017/09/07.
 */
public class ExecuteSparkJobTest {


    public static void main(String[] args) throws IOException, InterruptedException {
        String appJar = "/Users/mi186020/kylo-cassandra-spark-validate-cleanse-app-0.8.2.1-SNAPSHOT-jar-with-dependencies.jar";
        String mainClass = "com.mmaioe.spark.datavalidator.cassandra.Validator";
        String sparkMaster = "localhost";
        String sparkHome = "/usr/local/Cellar/apache-spark/2.2.0";
        String sparkApplicationName = "test";

        String appArgs = "cs_test,iphone,1504770965111,/tmp/kylo-nifi/spark/cs_test/iphone/1504770965111/iphone_field_policy.json,/tmp/kylo-nifi/spark/cs_test/iphone/1504770965111/iphone_field_structure.json,/tmp/kylo-nifi/spark/cs_test/iphone/1504770965111/myiphone1.csv";
        String[] argsSpark = null;
        if (!StringUtils.isEmpty(appArgs)) {
            argsSpark = appArgs.split(",");
        }

        System.out.println("length : "+argsSpark.length);

        SparkLauncher launcher = new SparkLauncher()
            .setAppResource(appJar)
            .setMainClass(mainClass)
            .setMaster(sparkMaster)
            .setSparkHome(sparkHome)
            .setAppName(sparkApplicationName);

        if (args != null) {
            launcher.addAppArgs(argsSpark);
        }


        Process spark = launcher.launch();

            /* Read/clear the process input stream */
//        boolean completed = spark.waitFor(10000L, TimeUnit.SECONDS);
        spark.waitFor();

        int exitCode = spark.exitValue();

        System.out.println("exit: "+exitCode);
    }
}
