package com.mmaioe.nifi.processors.ingest;


import com.google.common.io.LineReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.List;
import java.util.Vector;

/**
 *
 */
public class ColumnSpec{

    private String name;

    private String comment;

    private String dataType;

    private boolean pk;

    private boolean createDt;

    private boolean modifiedDt;

    public ColumnSpec(String name, String dataType, String comment) {
        this(name, dataType, comment, false, false, false);
    }

    public ColumnSpec(String name, String dataType, String comment, boolean pk, boolean createDt, boolean modifiedDt) {
        super();
        Validate.notEmpty(name);

        this.name = name.toLowerCase().trim();
        this.dataType = (StringUtils.isEmpty(dataType) ? "string" : dataType);
        this.comment = comment;
        this.pk = pk;
        this.createDt = createDt;
        this.modifiedDt = modifiedDt;
        validate();
    }

    /**
     * Method  for defining a column specification as a pipe-delimited format:  column|data type|comment (optional)|pk|created|modified. Each row separated by a newline
     */
    public static ColumnSpec[] createFromString(String specString) {
        if (StringUtils.isEmpty(specString)) {
            return null;
        }
        List<ColumnSpec> specs = new Vector<>();
        try {
            LineReader lineReader = new LineReader(
                new StringReader(specString));

            String line;
            while ((line = lineReader.readLine()) != null) {
                String[] parts = line.split("\\|");
                int len = parts.length;
                if (len > 0) {
                    String columnName = "";
                    String comment = "";
                    String dataType = "string";
                    boolean pk = false;
                    boolean modifiedDt = false;
                    boolean createDt = false;
                    switch (len) {
                        default:
                        case 6:
                            modifiedDt = "1".equals(parts[5].trim());
                        case 5:
                            createDt = "1".equals(parts[4].trim());
                        case 4:
                            pk = "1".equals(parts[3].trim());
                        case 3:
                            comment = parts[2];
                        case 2:
                            dataType = parts[1];
                        case 1:
                            columnName = parts[0];
                    }
                    specs.add(new ColumnSpec(columnName, dataType, comment, pk, createDt, modifiedDt));
                }
            }
            return specs.toArray(new ColumnSpec[0]);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse column specs[" + specString + "]", e);
        }

    }

    public void validate() {
        Validate.notEmpty(name);
        Validate.notEmpty(dataType);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isPk() {
        return pk;
    }

    public void setPk(boolean pk) {
        this.pk = pk;
    }

    public boolean isCreateDt() {
        return createDt;
    }

    public void setCreateDt(boolean createDt) {
        this.createDt = createDt;
    }

    public boolean isModifiedDt() {
        return modifiedDt;
    }

    public void setModifiedDt(boolean modifiedDt) {
        this.modifiedDt = modifiedDt;
    }

    public static String toCassandraDataTypeString(ColumnSpec[] columns){
        StringBuffer sb = new StringBuffer();
        for (int i=0;i<columns.length-1;i++){
            sb.append(columns[i].toCassandraDataTypeString()+",");
        }

        return sb.toString()+columns[columns.length-1].toCassandraDataTypeString();
    }

    public static String toInvalidCassandraDataTypeString(ColumnSpec[] columns){
        StringBuffer sb = new StringBuffer();
        for (int i=0;i<columns.length-1;i++){
            sb.append(columns[i].toInvalidCassandraDataTypeString()+",");
        }

        return sb.toString()+columns[columns.length-1].toInvalidCassandraDataTypeString();
    }
    public String toCassandraDataTypeString(){
        String str = this.name+" ";

        if(this.dataType.equals("string")){
            str += "varchar";
        }else{
            str += this.dataType;
        }

        if(this.pk){
            return str+" PRIMARY KEY";
        }

        return str;
    }

    public String toInvalidCassandraDataTypeString(){
        String str = this.name+" text";

        if(this.pk){
            return str+" PRIMARY KEY";
        }

        return str;
    }

    public String toString(){
        return "cassandra create : "+this.name+","+dataType;
    }
}
