define(['angular', 'plugin/cassandra-table-data-module/module-name'], function (angular, moduleName) {

    var controller = function($state,$stateParams,$transition$,$http,$interval,$timeout){
        var self = this;

        this.keyspace = $stateParams.keyspace;
        this.table = $stateParams.table;

        this.cardTitle = "Data from "+this.keyspace+"."+this.table;
        this.pageName = "Data from "+this.keyspace+"."+this.table;
        this.viewType = 'list';

        this.data = [];
        this.metadata = [];

        fetchData(this.keyspace,this.table);

        function fetchData(keyspace,table){
            $http.get("/proxy/v1/cassandra/module/data?keyspace="+keyspace+"&table="+table).then(function(response){
                if(response.data){
                    console.log(response.data);
                    self.data = response.data.data;
                    self.metadata = response.data.metadata;

                }
            })
        }
    };



    angular.module(moduleName).controller('CassandraTableDataModuleController',['$state','$stateParams','$transition$','$http','$interval','$timeout',controller]);

});
