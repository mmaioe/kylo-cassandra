package com.mmaioe.nifi.processors.ingest;

import com.datastax.driver.core.Session;
import com.mmaioe.nifi.processor.AbstractNifiProcessor;
import com.mmaioe.nifi.services.cassandra.CassandraService;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.mmaioe.nifi.processors.ingest.IngestProperties.FEED_FIELD_SPECIFICATION;
import static com.mmaioe.nifi.processors.ingest.IngestProperties.FEED_CATEGORY;
import static com.mmaioe.nifi.processors.ingest.IngestProperties.FEED_NAME;
import static com.mmaioe.nifi.processors.ingest.IngestProperties.REL_FAILURE;
import static com.mmaioe.nifi.processors.ingest.IngestProperties.REL_SUCCESS;

/**
 * Created by mi186020 on 2017/08/31.
 */

@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@Tags({"Cassandra", "register", "mmaioe"})
@CapabilityDescription("Creates a set of standard feed tables in Cassandra")
public class RegisterFeedTables extends AbstractNifiProcessor {

    public static final PropertyDescriptor CASSANDRA_SERVICE = new PropertyDescriptor.Builder()
        .name("Cassandra Connection Pooling Service")
        .description("The Controller Service that is used to obtain connection to database")
        .required(true)
        .identifiesControllerService(CassandraService.class)
        .build();

    public static final PropertyDescriptor CREATE_KEY_SPACE_WITH_OPTION = new PropertyDescriptor.Builder()
        .name("statement after WITH OPTION of Cassandra")
        .description("Specify satetement following after WITH option of Cassandra")
        .required(true)
        .defaultValue("replication = {'class':'SimpleStrategy', 'replication_factor' : 3}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    private static final String INVALID_TABLE_SUFFIX = "_invalid";
    private final Set<Relationship> relationships;
    private final List<PropertyDescriptor> properties;

    public RegisterFeedTables() {
        final Set<Relationship> r = new HashSet<>();
        r.add(REL_SUCCESS);
        r.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(r);

        final List<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(CASSANDRA_SERVICE);
        pds.add(FEED_CATEGORY);
        pds.add(FEED_NAME);
        pds.add(FEED_FIELD_SPECIFICATION);
        pds.add(CREATE_KEY_SPACE_WITH_OPTION);

        properties = Collections.unmodifiableList(pds);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        getLog().info("Hi start Register Feed Tables on Cassandra");
        final FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        final ColumnSpec[] columnSpecs = Optional.ofNullable(context.getProperty(FEED_FIELD_SPECIFICATION).evaluateAttributeExpressions(flowFile).getValue())
            .filter(StringUtils::isNotEmpty)
            .map(ColumnSpec::createFromString)
            .orElse(new ColumnSpec[0]);

        if (columnSpecs == null || columnSpecs.length == 0) {
            getLog().error("Missing field specification");
            session.transfer(flowFile, IngestProperties.REL_FAILURE);
            return;
        }

        getLog().info("Hello show the spec now - itomie");
        for(ColumnSpec spe: columnSpecs){
            getLog().info(spe.toString());
        }

        // Cassandra Service for registering
        final CassandraService cassandraService = context.getProperty(CASSANDRA_SERVICE).asControllerService(CassandraService.class);

        getLog().info("show attributes of a flowfile");
        getLog().info(flowFile.getAttributes().toString());
        Map<PropertyDescriptor, String> contextProps = context.getProperties();
        getLog().info("Show context props ");
        getLog().info(contextProps.toString());

        try (final Session conn = cassandraService.getSession()) {
            final String entity = context.getProperty(IngestProperties.FEED_NAME).evaluateAttributeExpressions(flowFile).getValue();
            getLog().info("check entity value: "+entity);
            if (entity == null || entity.isEmpty()) {
                getLog().error("Missing feed name");
                session.transfer(flowFile, IngestProperties.REL_FAILURE);
                return;
            }

            final String source = context.getProperty(IngestProperties.FEED_CATEGORY).evaluateAttributeExpressions(flowFile).getValue();
            if (source == null || source.isEmpty()) {
                getLog().error("Missing category name");
                session.transfer(flowFile, IngestProperties.REL_FAILURE);
                return;
            }

            final String key_space_with_options = context.getProperty(CREATE_KEY_SPACE_WITH_OPTION).evaluateAttributeExpressions(flowFile).getValue();

            getLog().info("Let's create a set of keyspaces");
            getLog().info("category:"+source);
            getLog().info("feed:"+entity);
            getLog().info("CREATE KEYSPACE IF NOT EXISTS "+source+" WITH "+key_space_with_options+";");
            getLog().info("CREATE TABLE IF NOT EXISTS "+source+"."+entity+"("+ColumnSpec.toCassandraDataTypeString(columnSpecs)+")");

            conn.execute("CREATE KEYSPACE IF NOT EXISTS "+source+" WITH "+key_space_with_options+";");

            conn.execute("CREATE TABLE IF NOT EXISTS "+source+"."+entity+"("+ColumnSpec.toCassandraDataTypeString(columnSpecs)+")");
            conn.execute("CREATE TABLE IF NOT EXISTS "+source+"."+entity+INVALID_TABLE_SUFFIX+"("+ColumnSpec.toCassandraDataTypeString(columnSpecs)+")");

            session.transfer(flowFile,REL_SUCCESS);
        } catch (Exception e) {
            getLog().error("Unable to obtain connection for {} due to {}; routing to failure", new Object[]{flowFile, e});
            session.transfer(flowFile, REL_FAILURE);
        }
    }
}
