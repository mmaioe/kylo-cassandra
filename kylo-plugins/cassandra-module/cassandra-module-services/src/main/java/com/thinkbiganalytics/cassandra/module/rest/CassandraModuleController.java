package com.thinkbiganalytics.cassandra.module.rest;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.cassandra.module.CassandraService;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

@Api(tags = "Cassandra Module")
@Path("/v1/cassandra/module")
@SwaggerDefinition(tags = @Tag(name = "Cassandra Module", description = "Cassandra Module"))
public class CassandraModuleController {

    @Autowired
    CassandraService cassandraService;

    /**
     * Get the list of keyspaces
     * http://localhost:8400/proxy/v1/cassandra/module/table
     * http://localhost:8420/api/v1/cassandra/module/keyspace
     *
     * @return A list of foods
     */
    @GET
    @Path("/keyspace")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets a list of keyspaces")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns a list of keyspaces.", response = List.class)
                  })
    public Response getKeyspace() {
        List<String> names = new ArrayList<String>();

        ResultSet resultSet = cassandraService.execute("select * from system_schema.keyspaces;");
        for (Row row : resultSet){
            names.add(row.getString("keyspace_name"));
        }


        return Response.ok(names).build();
    }

    @GET
    @Path("/table")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets a list of tables")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns a list of tables.", response = List.class)
                  })
    public Response getKeyspace(@QueryParam("keyspace") String keyspace) {
        List<String> names = new ArrayList<String>();

        ResultSet resultSet = cassandraService.execute("select * from system_schema.tables where keyspace_name='"+keyspace+"';");
        for (Row row : resultSet){
            names.add(row.getString("table_name"));
        }

        return Response.ok(names).build();
    }

    @GET
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets a list of tables")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns a list of tables.", response = List.class)
                  })
    public Response getData(@QueryParam("keyspace") String keyspace, @QueryParam("table") String table) {
        Map<String,Object> result = new HashMap<String,Object>();
        List<List<String>> metadata = new ArrayList<List<String>>();
        List<String> metadataClass = new ArrayList<String>();
        List<List<String>> rows = new ArrayList<List<String>>();

        ResultSet resultSet = cassandraService.execute("select * from "+keyspace+"."+table+" limit 20;");
        List<ColumnDefinitions.Definition>  columns = resultSet.getColumnDefinitions().asList();
        for(ColumnDefinitions.Definition column : columns){
            metadata.add(Lists.newArrayList(column.getName(),column.getType().toString()));
            metadataClass.add(column.getType().toString());
        }

        for (Row row : resultSet) {
            List<String> rowStr = new ArrayList<String>();

            for (int i = 0; i < metadata.size(); i++) {

                if(metadataClass.get(i).equals("int")) {
                    rowStr.add(row.getInt(i)+"");
                }else if(metadataClass.get(i).equals("double")) {
                    rowStr.add(row.getDouble(i) + "");
                }else if(metadataClass.get(i).equals("float")) {
                    rowStr.add(row.getFloat(i) + "");
                }else if(metadataClass.get(i).equals("boolean")){
                    rowStr.add(row.getBool(i)+"");
                }else if(metadataClass.get(i).equals("text") || metadataClass.get(i).equals("varchar")){
                    rowStr.add(row.getString(i));
                }

            }

            rows.add(rowStr);
        }

        result.put("data",rows);
        result.put("metadata",metadata);

        return Response.ok(result).build();
    }
}
