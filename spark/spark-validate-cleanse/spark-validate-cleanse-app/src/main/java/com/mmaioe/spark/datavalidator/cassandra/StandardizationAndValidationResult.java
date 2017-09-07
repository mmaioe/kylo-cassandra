package com.mmaioe.spark.datavalidator.cassandra;

/**
 * Created by mi186020 on 2017/09/05.
 */
import com.thinkbiganalytics.policy.validation.ValidationResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Store the result of all standardizations and validations on a field
 */
public class StandardizationAndValidationResult {

    private List<ValidationResult> validationResults = null;

    private Object fieldValue = null;

    public StandardizationAndValidationResult(Object value){
        fieldValue = value;
    }


    public ValidationResult getFinalValidationResult(){
        ValidationResult finalResult = Validator.VALID_RESULT;
        if(validationResults == null || validationResults.isEmpty()){
            finalResult = Validator.VALID_RESULT;
        }
        else {
            for(ValidationResult r : validationResults) {
                if(r != Validator.VALID_RESULT){
                    finalResult = r;
                    break;
                }
            }
        }
        return finalResult;
    }

    public void addValidationResult(ValidationResult validationResult){
        validationResults = (validationResults == null ? new ArrayList<ValidationResult>() : validationResults);
        validationResults.add(validationResult);
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(Object fieldValue) {
        this.fieldValue = fieldValue;
    }

    public List<ValidationResult> getValidationResults() {
        return validationResults;
    }

    public String getFieldValueForValidation(){
        return((fieldValue == null) ? null:fieldValue.toString());
    }
}