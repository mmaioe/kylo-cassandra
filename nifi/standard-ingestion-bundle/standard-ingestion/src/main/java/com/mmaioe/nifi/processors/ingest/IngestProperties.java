package com.mmaioe.nifi.processors.ingest;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;

/**
 * Created by mi186020 on 2017/09/01.
 */
public interface IngestProperties {
    PropertyDescriptor FEED_CATEGORY = new PropertyDescriptor.Builder()
        .name("System feed category")
        .description("System category of the feed this processor supports")
        .required(true)
        .defaultValue("${category}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    PropertyDescriptor FEED_NAME = new PropertyDescriptor.Builder()
        .name("System feed name")
        .description("Name of feed this processor supports")
        .defaultValue("${feed}")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    PropertyDescriptor FEED_FIELD_SPECIFICATION = new PropertyDescriptor.Builder()
        .name("Feed Field specification")
        .description("Pipe-delim and newline for each field: column name|data type|comment")
        .defaultValue("${metadata.table.feedFieldStructure}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    // Standard Relationships
    Relationship REL_SUCCESS = new Relationship.Builder()
        .name("success")
        .description("Successfully relationship.")
        .build();

    Relationship REL_FAILURE = new Relationship.Builder()
        .name("failure")
        .description("SQL query execution failed. Incoming FlowFile will be penalized and routed to this relationship")
        .build();

}
