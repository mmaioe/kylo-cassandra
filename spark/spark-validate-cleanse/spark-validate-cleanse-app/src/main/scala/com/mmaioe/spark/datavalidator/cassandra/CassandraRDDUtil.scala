package com.mmaioe.spark.datavalidator.cassandra


import com.datastax.spark.connector.CassandraRowMetadata
import com.datastax.spark.connector.CassandraRow
import org.apache.spark.sql.Row

import scala.collection.mutable
//import com.datastax.spark.connector.C;

/**
  * Created by mi186020 on 2017/09/06.
  */
object CassandraRDDUtil {
  def ToCassandraRow(columnNames :scala.collection.immutable.List[String], columnValues:scala.collection.immutable.List[AnyRef]): CassandraRow={

    return new CassandraRow(new CassandraRowMetadata(columnNames.toIndexedSeq),columnValues.toIndexedSeq);
  }
}
