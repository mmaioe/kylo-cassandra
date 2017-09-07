package com.mmaioe.nifi.services.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.annotations.Table;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;

/**
 * Created by mi186020 on 2017/08/31.
 */
@Tags({"cassandra", "connection", "database"})
@CapabilityDescription("Provides Cassandra Connection Pooling Service.")
public interface CassandraService extends ControllerService{
    Session getSession() throws Exception;
    Cluster getCluster() throws Exception;
}
