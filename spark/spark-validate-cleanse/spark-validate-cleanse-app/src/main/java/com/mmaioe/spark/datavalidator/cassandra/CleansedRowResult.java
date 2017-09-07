package com.mmaioe.spark.datavalidator.cassandra;


import org.apache.spark.sql.Row;
import java.io.Serializable;

/*
 * Wrapper class to contain our cleansed row and its column validation results
 */
public class CleansedRowResult implements Serializable {

    public Row row;
    public boolean[] columnsValid;
    public boolean rowIsValid;

}