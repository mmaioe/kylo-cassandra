package com.mmaioe.spark.datavalidator.cassandra;

import com.datastax.spark.connector.CassandraRow;
import com.datastax.spark.connector.japi.CassandraJavaUtil;
import com.thinkbiganalytics.policy.BaseFieldPolicy;
import com.thinkbiganalytics.policy.FieldPolicy;
import com.thinkbiganalytics.policy.standardization.AcceptsEmptyValues;
import com.thinkbiganalytics.policy.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policy.validation.ValidationPolicy;
import com.thinkbiganalytics.policy.validation.ValidationResult;

import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.policy.FieldPolicyLoader;
import com.thinkbiganalytics.spark.util.InvalidFormatException;
import com.thinkbiganalytics.spark.validation.HCatDataType;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.mmaioe.spark.datavalidator.cassandra.dao.CassandraRowWriter.CassandraRowWriterFactory;

import javassist.compiler.ast.FieldDecl;

import scala.Function1;

/**
 *
 * spark-submit --master local --class com.mmaioe.spark.datavalidator.cassandra.Validator --name CassandraTest /Users/mi186020/OSS/kylo-integrations-cassandra/spark/spark-validate-cleanse/spark-validate-cleanse-app/target/kylo-cassandra-spark-validate-cleanse-app-0.8.2.1-SNAPSHOT-jar-with-dependencies.jar cs_test users g /Users/mi186020/test_field_policy.json /Users/mi186020/test_field_structure.json /Users/mi186020/test.csv
 * spark-submit --master local --class com.mmaioe.spark.datavalidator.cassandra.Validator --name CassandraTest /Users/mi186020/OSS/kylo-integrations-cassandra/spark/spark-validate-cleanse/spark-validate-cleanse-app/target/kylo-cassandra-spark-validate-cleanse-app-0.8.2.1-SNAPSHOT-jar-with-dependencies.jar cs_test iphone g /private/tmp/kylo-nifi/spark/cs_test/iphone/1504758250377/iphone_field_policy.json /private/tmp/kylo-nifi/spark/cs_test/iphone/1504758250377/iphone_field_structure.json /var/dropzone/userdata1.csv
 *
 * Created by mi186020 on 2017/09/04.
 */
@Component
public class Validator implements Serializable{

    private static final Logger log = LoggerFactory.getLogger(Validator.class);

    /**
     * Make sure that this suffix is the same as the one defined in "RegisterFeedTables" processor java.
     */
    private static final String INVALID_TABLE_SUFFIX = "_invalid";

    private static CassandraRowWriterFactory cassandraDataWriter = new CassandraRowWriterFactory();

    private String sourceCSVPath;
    private String validTableName;
    private String invalidTableName;
    private String targetDatabase;
    private String partition;
    private Map<String, FieldPolicy> policyMap = new HashMap<>();
    private ColumnSpec[] schema;


//    @Qualifier("com.thinkbiganalytics.spark.policy.FieldPolicyLoader")
//    @Autowired
    private FieldPolicyLoader loader = new FieldPolicyLoader();
//    private FieldPolicyLoader loader;
    /**
     * Path to the file containing the JSON for the Field Policies. If called from NIFI it will pass it in as a command argument in the Validate processor The JSON should conform to the array of
     * FieldPolicy objects found in the thinkbig-field-policy-rest-model module
     */
    private String fieldPolicyJsonPath;
    private String fieldStructureJsonPath;
    protected static ValidationResult VALID_RESULT = new ValidationResult();
    private Map<Class, Class> validatorParamType = new HashMap<>();

//    private Map<String, FieldPolicy> policyMap = new HashMap<>();
    public static void main(String[] args) {
        log.info("Running Spark Validator with the following command line args (comma separated):" + StringUtils.join(args, ","));

        try {


            ApplicationContext ctx = new AnnotationConfigApplicationContext("com.mmaioe.spark");
            Validator app = ctx.getBean(Validator.class);

            log.info("args[0]: "+args[0]);
            log.info("args[1]: "+args[1]);
            log.info("args[2]: "+args[2]);
            log.info("args[3]: "+args[3]);
            log.info("args[4]: "+args[4]);
            log.info("args[5]: "+args[5]);

            app.setArguments(args[0],args[1],args[2],args[3],args[4],args[5]);
            app.doValidate();

        } catch (Exception e) {
            log.error("Failed to perform validation", e);
            System.exit(1);
        }
    }

    public void setArguments(String targetDatabase, String entity, String partition, String fieldPolicyJsonPath, String fieldStructureJsonPath, String sourceCSVPath) {
        this.targetDatabase = targetDatabase;
        this.validTableName = entity;
        this.invalidTableName = this.validTableName+INVALID_TABLE_SUFFIX;

        this.fieldPolicyJsonPath = fieldPolicyJsonPath;
        this.fieldStructureJsonPath = fieldStructureJsonPath;
        this.sourceCSVPath = sourceCSVPath;
    }


    public void doValidate() {
        try {


            SparkConf sparkConf = new SparkConf().setAppName("CassandraTest").setMaster("local").set("spark.cassandra.connection.host", "127.0.0.1");
            SparkContext sparkContext = new SparkContext(sparkConf);
            SQLContext sqlContext = new SQLContext(sparkContext);

//            fieldStructureJsonPath = "/Users/mi186020/test_field_structure.json";
//            fieldPolicyJsonPath = "/Users/mi186020/test_field_policy.json";

            policyMap = loader.loadFieldPolicy(fieldPolicyJsonPath);
//            policies = policyMap.values().toArray(new FieldPolicy[0]);
            schema = ColumnSpec.createFromFile(fieldStructureJsonPath);

//            for(ColumnSpec spec: structures){
//                System.out.println(" column "+spec.getName()+": "+spec.getDataType());
//            }

            System.out.println(policyMap.keySet());
//            Dataset csvData = sqlContext.read().csv("/Users/mi186020/test.csv");

//            JavaRDD<Row> csvRDD = sqlContext.read().csv("/Users/mi186020/test.csv").javaRDD();
            JavaRDD<Row> csvRDD = sqlContext.read().csv(this.sourceCSVPath).javaRDD();
//
            JavaRDD<CleansedRowResult> cleansedRowResultRDD = csvRDD.map(new Function<Row, CleansedRowResult>() {
                @Override
                public CleansedRowResult call(Row row) throws Exception {
                    return cleanseAndValidateRow(row);
                }
            });

            JavaRDD<CassandraRow> validResultRDD = cleansedRowResultRDD.filter(new Function<CleansedRowResult, Boolean>() {
                @Override
                public Boolean call(CleansedRowResult cleansedRowResult) throws Exception {
                    return cleansedRowResult.rowIsValid;
                }
            }).map(new Function<CleansedRowResult, CassandraRow>() {
                @Override
                public CassandraRow call(CleansedRowResult cleansedRowResult) throws Exception {
                    return transformRowToCassandraRow(cleansedRowResult.row);
                }
            });

            JavaRDD<CassandraRow> invalidResultRDD = cleansedRowResultRDD.filter(new Function<CleansedRowResult, Boolean>() {
                @Override
                public Boolean call(CleansedRowResult cleansedRowResult) throws Exception {
                    return cleansedRowResult.rowIsValid == false;
                }
            }).map(new Function<CleansedRowResult, CassandraRow>() {
                @Override
                public CassandraRow call(CleansedRowResult cleansedRowResult) throws Exception {
                    return transformRowToCassandraRow(cleansedRowResult.row);
                }
            });

            cassandraDataWriter.setColumnNames(schema);

            CassandraJavaUtil.javaFunctions(validResultRDD).writerBuilder(this.targetDatabase,this.validTableName,cassandraDataWriter).saveToCassandra();

            CassandraJavaUtil.javaFunctions(invalidResultRDD).writerBuilder(this.targetDatabase,this.invalidTableName,cassandraDataWriter).saveToCassandra();
        } catch (Exception e) {
            log.error("Failed to perform validation", e);
            System.exit(1);
        }
    }

    /**
     * Spark function to perform both cleansing and validation of a data row based on data policies and the target datatype
     */
    private CassandraRow transformRowToCassandraRow(Row row) {
        List<String> columnNames = new ArrayList<String>();
        Iterator<String> key = policyMap.keySet().iterator();
        while(key.hasNext()) columnNames.add(key.next());

        List<Object> columnValues = new ArrayList<Object>();
        for(int i=0;i<row.size();i++) columnValues.add(row.get(i));

//        return new CassandraRow(scala.collection.JavaConversions.asScalaBuffer(columnNames).toList().toIndexedSeq(),scala.collection.JavaConversions.asScalaBuffer(columnValues).toList().toIndexedSeq());
        return CassandraRDDUtil.ToCassandraRow(
            scala.collection.JavaConversions.asScalaBuffer(columnNames).toList(),
            scala.collection.JavaConversions.asScalaBuffer(columnValues).toList()
        );
    }

    private CleansedRowResult cleanseAndValidateRow(Row row) {
        int nulls = 1;

        // Create placeholder for the new values plus one columns for reject_reason
//        Object[] newValues = new Object[schema.length + 1];
        boolean rowValid = true;
//        String sbRejectReason = null;
        List<ValidationResult> results = null;
        boolean[] columnsValid = new boolean[schema.length];

        // Iterate through columns to cleanse and validate
        for (int idx = 0; idx < schema.length; idx++) {
            ValidationResult result = VALID_RESULT;
            FieldPolicy fieldPolicy = policyMap.get(schema[idx].getName());
            ColumnSpec dataType = schema[idx];
            boolean columnValid = true;

            // Extract the value (allowing for null or missing field for odd-ball data)
            Object val = (idx == row.length() || row.isNullAt(idx) ? null : row.get(idx));
            // Handle complex types by passing them through

            Object fieldValue = (val != null ? val : null);

            if (fieldValue == null) {
                nulls++;
            }

            StandardizationAndValidationResult standardizationAndValidationResult = standardizeAndValidateField(fieldPolicy, fieldValue, dataType);
            result = standardizationAndValidationResult.getFinalValidationResult();
            //only apply the standardized result value if the routine is valid
            fieldValue = result.isValid() ? standardizationAndValidationResult.getFieldValue() : fieldValue;

//            newValues[idx] = fieldValue;

            if (!result.isValid()) {
                rowValid = false;
                results = (results == null ? new Vector<ValidationResult>() : results);
                results.addAll(standardizationAndValidationResult.getValidationResults());
                //results.add(result);
                columnValid = false;
            }

            // Record fact that we there was an invalid column
            columnsValid[idx] = columnValid;
        }
        // Return success unless all values were null.  That would indicate a blank line in the file.
        if (nulls >= schema.length) {
            rowValid = false;
            results = (results == null ? new Vector<ValidationResult>() : results);
            results.add(ValidationResult.failRow("empty", "Row is empty"));
        }

        // Convert to reject reasons to JSON
//        sbRejectReason = toJSONArray(results);
//
//        // Record the results in the appended columns, move processing partition value last
//        newValues[schema.length] = newValues[schema.length - 1]; //PROCESSING_DTTM_COL
//        newValues[schema.length-1] = sbRejectReason;   //REJECT_REASON_COL
        //   newValues[schema.length - 1] = (rowValid ? "1" : "0");  //VALID_INVALID_COL

        CleansedRowResult cleansedRowResult = new CleansedRowResult();
//        cleansedRowResult.row = RowFactory.create(newValues);
        cleansedRowResult.row = row;
        cleansedRowResult.columnsValid = columnsValid;
        cleansedRowResult.rowIsValid = rowValid;
        return cleansedRowResult;
    }

    private String toJSONArray(List<ValidationResult> results) {
        // Convert to reject reasons to JSON
        StringBuffer sb = null;
        if (results != null) {
            sb = new StringBuffer();
            for (ValidationResult result : results) {
                if (sb.length() > 0) {
                    sb.append(",");
                } else {
                    sb.append("[");
                }
                sb.append(result.toJSON());
            }
            sb.append("]");
        }
        return (sb == null ? "" : sb.toString());
    }

    protected StandardizationAndValidationResult standardizeAndValidateField(FieldPolicy fieldPolicy, Object value, ColumnSpec dataType) {
        StandardizationAndValidationResult result = new StandardizationAndValidationResult(value);

        List<BaseFieldPolicy> fieldPolicies = fieldPolicy.getAllPolicies();
        int idx = 0;
        for (BaseFieldPolicy p : fieldPolicies) {
            if (p instanceof StandardizationPolicy) {
                StandardizationPolicy standardizationPolicy = (StandardizationPolicy) p;
                boolean isEmpty = ((value == null) || (StringUtils.isEmpty(value.toString())));
                boolean shouldStandardize = true;
                if (isEmpty && !(standardizationPolicy instanceof AcceptsEmptyValues)) {
                    shouldStandardize = false;
                }

                if (!standardizationPolicy.accepts(value)) {
                    shouldStandardize = false;
                }

                if (shouldStandardize) {
                    Object newValue = standardizationPolicy.convertRawValue(result.getFieldValue());
                    result.setFieldValue(newValue != null ? newValue.toString() : newValue);
                }
            }

            if (p instanceof ValidationPolicy) {

                ValidationPolicy validationPolicy = (ValidationPolicy) p;
                ValidationResult validationResult = validateValue(validationPolicy, dataType, result.getFieldValueForValidation(), idx);
                //only need to add those that are invalid
                if (validationResult != VALID_RESULT) {
                    result.addValidationResult(validationResult);
                    break; //exit out of processing if invalid records found.
                }
            }

        }
        ValidationResult finalValidationCheck = finalValidationCheck(fieldPolicy, dataType, result.getFieldValueForValidation());
        if (finalValidationCheck != VALID_RESULT) {
            result.addValidationResult(finalValidationCheck);
        }

        return result;
    }

    /**
     * Perform validation using both schema validation the validation policies
     */
    protected ValidationResult finalValidationCheck(FieldPolicy fieldPolicy, ColumnSpec fieldDataType, String fieldValue) {

        boolean isEmpty = (StringUtils.isEmpty(fieldValue));
        if (isEmpty) {
            ValidationPolicy validator;
            if ((validator = fieldPolicy.getNotNullValidator()) != null) {
                ValidationResult result = validateValue(validator, fieldDataType, fieldValue, -1);
                if (result != VALID_RESULT) {
                    return result;
                }
            }
        }else if(!fieldDataType.isValueConvertibleToType(fieldValue)){
            return ValidationResult
                    .failField("incompatible", fieldDataType.getName(),
                               "Not convertible to " + fieldDataType.getDataType());
        }

        return VALID_RESULT;
    }

    protected ValidationResult validateValue(ValidationPolicy validator, ColumnSpec fieldDataType, String fieldValue, Integer idx) {
        try {
            // Resolve the type of parameter required by the validator. A cache is used to avoid cost of reflection.
            Class expectedParamClazz = resolveValidatorParamType(validator);
            Object nativeValue = fieldValue;
//            if (expectedParamClazz != String.class) {
//                nativeValue = fieldDataType.toNativeValue(fieldValue);
//            }
            if (!validator.validate(nativeValue)) {

                //get any fields in this validator annotated with PolicyProperty

                return ValidationResult
                    .failFieldRule("rule", fieldDataType.getName(), validator.getClass().getSimpleName(),
                                   "Rule violation");
            }
            return VALID_RESULT;
        } catch (ClassCastException e) {
            return ValidationResult
                .failField("incompatible", fieldDataType.getName(),
                           "Not convertible to " + fieldDataType.getDataType());
        }
    }

    /* Resolve the type of param required by the validator. A cache is used to avoid cost of reflection */
    protected Class resolveValidatorParamType(ValidationPolicy validator) {
        Class expectedParamClazz = validatorParamType.get(validator.getClass());
        if (expectedParamClazz == null) {
            // Cache for future references

            Object t = validator.getClass().getGenericInterfaces()[0];
            if (t instanceof ParameterizedType) {
                ParameterizedType type = (ParameterizedType) t;
                expectedParamClazz = (Class) type.getActualTypeArguments()[0];
            } else {
                expectedParamClazz = String.class;
            }
            validatorParamType.put(validator.getClass(), expectedParamClazz);
        }
        return expectedParamClazz;
    }
}
