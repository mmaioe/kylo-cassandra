package com.mmaioe.nifi.services.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.google.common.collect.Lists;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnDisabled;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.controller.ControllerService;
import org.apache.nifi.controller.ControllerServiceInitializationContext;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * Created by mi186020 on 2017/08/31.
 */
@Tags({"cassandra","connection", "pooling"})
@CapabilityDescription("Provides Cassandra connection service")
public class CassandraConnectionPool extends AbstractControllerService implements CassandraService {

    public static final PropertyDescriptor CONTACT_POINTS_URLS = new PropertyDescriptor.Builder()
        .name("Contact Points")
        .description("Contact Points Urls to Cassandra Cluster")
        .defaultValue(null)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .required(true)
        .build();

    private volatile Cluster cluster;
    private volatile Session session;
    private volatile List<InetAddress> addresses = Lists.newArrayList();
    /**
     * List of properties
     */
    private List<PropertyDescriptor> properties;

    @Override
    public Session getSession() throws Exception {
        if(this.session == null){
            if(this.cluster == null){
                this.cluster = Cluster.builder().addContactPoints(addresses).build();
                this.session = cluster.connect();
            }else if(this.cluster.isClosed()){
                this.cluster = Cluster.builder().addContactPoints(addresses).build();
                this.session = cluster.connect();
            }
        }else if(this.session.isClosed()){
            if(this.cluster == null){
                this.cluster = Cluster.builder().addContactPoints(addresses).build();
                this.session = cluster.connect();
            }else if(this.cluster.isClosed()){
                this.cluster = Cluster.builder().addContactPoints(addresses).build();
                this.session = cluster.connect();
            }
        }

        return session;
    }

    @Override
    public Cluster getCluster() throws Exception {
        if(this.cluster == null){
            this.cluster = Cluster.builder().addContactPoints(addresses).build();
        }else if(this.cluster.isClosed()){
            this.cluster = Cluster.builder().addContactPoints(addresses).build();
        }
        return cluster;
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @Override
    protected void init(@Nonnull final ControllerServiceInitializationContext config) throws InitializationException {
        // Create list of properties
        final List<PropertyDescriptor> props = new ArrayList<>();
        props.add(CONTACT_POINTS_URLS);
        properties = Collections.unmodifiableList(props);
    }

    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException {
        final String pointsUrls = context.getProperty(CONTACT_POINTS_URLS).getValue();

        try {
            for (String contactPoint : pointsUrls.split(",")) {

                addresses.add(InetAddress.getByName(contactPoint));
            }
            cluster = Cluster.builder().addContactPoints(addresses).build();
            session = cluster.connect();
            getLogger().info("Created new Cassandra Connection Pool for " + pointsUrls);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Shutdown pool, close all open connections.
     */
    @OnDisabled
    public void shutdown() {
        session.close();
        cluster.close();
    }

    @Override
    public String toString() {
        return "CassandraConnectionPool[id=" + getIdentifier() + "]";
    }
}
