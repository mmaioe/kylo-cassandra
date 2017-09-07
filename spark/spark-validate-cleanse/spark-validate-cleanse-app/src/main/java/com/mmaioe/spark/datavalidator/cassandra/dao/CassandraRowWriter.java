package com.mmaioe.spark.datavalidator.cassandra.dao;

import com.datastax.spark.connector.CassandraRow;
import com.datastax.spark.connector.writer.RowWriter;
import com.datastax.spark.connector.writer.RowWriterFactory;

import com.datastax.spark.connector.ColumnRef;
import com.datastax.spark.connector.cql.TableDef;
import com.datastax.spark.connector.writer.RowWriter;
import com.datastax.spark.connector.writer.RowWriterFactory;
import com.mmaioe.spark.datavalidator.cassandra.ColumnSpec;
import com.thinkbiganalytics.policy.FieldPolicy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import scala.collection.IndexedSeq;
import scala.collection.Seq;
/**
 * Created by mi186020 on 2017/09/05.
 */
public class CassandraRowWriter implements RowWriter<CassandraRow> {
    private static scala.collection.immutable.List columns;
    private static final long serialVersionUID = 1L;
    private static CassandraRowWriter writer = new CassandraRowWriter();

    // Factory
    public static class CassandraRowWriterFactory implements RowWriterFactory<CassandraRow>, Serializable {
        private static final long serialVersionUID = 1L;

        public void setColumnNames(ColumnSpec[] schema){
            writer.setColumnNames(schema);
        }

        @Override
        public RowWriter<CassandraRow> rowWriter(TableDef arg0, IndexedSeq<ColumnRef> arg1) {
            return writer;
        }
    }

    public void setColumnNames(ColumnSpec[] schema){
        List<String> columnNames = new ArrayList<String>();
        for(ColumnSpec spec: schema) columnNames.add(spec.getName());
        columns = scala.collection.JavaConversions.asScalaBuffer(columnNames).toList();
    }

    @Override
    public Seq<String> columnNames() {
        return columns;
    }

    @Override
    public void readColumnValues(CassandraRow data, Object[] buffer) {

        for(int i=0;i<data.size();i++){
            buffer[i] = data.getRaw(i);
        }
    }
}
