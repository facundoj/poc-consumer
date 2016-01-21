package org.k12.caliper.poc.persistence;

import org.apache.spark.SparkContext;
import org.apache.spark.sql.hive.HiveContext;

/**
 * Created by tomas on 21/01/16.
 * This class wraps a HiveContext to operate as a singleton.
 */
public class HiveContextSingleton {
    static private transient HiveContext instance = null;
    static public HiveContext getInstance(SparkContext sparkContext) {
        if (instance == null) {
            instance = new HiveContext(sparkContext);
        }
        return instance;
    }
}