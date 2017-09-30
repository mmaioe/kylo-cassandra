define(['angular', 'plugin/cassandra-table-module/module-name'], function (angular, moduleName) {

    var controller = function($state,$stateParams,$transition$,$http,$interval,$timeout){
        var self = this;

        this.keyspace = $stateParams.keyspace;
        this.tables = [];

        this.cardTitle = "Tables";
        this.pageName = 'Tables';
        this.viewType = 'list';

        this.showData = function(table){
            $state.go("cassandraTableDataModule",{keyspace:self.keyspace,table:table});
        }

        fetchTable(this.keyspace);

        function fetchTable(keyspace){
            $http.get("/proxy/v1/cassandra/module/table?keyspace="+keyspace).then(function(response){
                if(response.data){
                    self.tables = response.data;
                }
            })
        }
    };



    angular.module(moduleName).controller('CassandraTableModuleController',['$state','$stateParams','$transition$','$http','$interval','$timeout',controller]);

});
