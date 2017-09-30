package com.thinkbiganalytics.cassandra.module;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.thinkbiganalytics.cassandra.module.config.CassandraClientConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by mi186020 on 2017/09/30.
 */
@Service
public class CassandraService {

    @Autowired
    CassandraClientConfig config;

    private Cluster cluster;
    private Session session;

    @PostConstruct
    private void init() {
        this.cluster = Cluster.builder().addContactPoint(config.getHost()).build();
        this.session = cluster.connect();
    }

    public ResultSet execute(String query){
        return this.session.execute(query);
    }
    public void close(){
        this.session.close();
        this.cluster.close();
    }
}
