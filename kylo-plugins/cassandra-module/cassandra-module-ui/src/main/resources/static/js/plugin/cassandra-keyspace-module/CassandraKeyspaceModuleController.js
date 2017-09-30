define(['angular', 'plugin/cassandra-keyspace-module/module-name'], function (angular, moduleName) {

    var controller = function($state,$transition$,$http,$interval,$timeout){
        var self = this;

        this.cardTitle = "Keyspaces";
        this.pageName = 'Keyspaces';
        this.viewType = 'list';

        this.keyspaces = [];


        fetchKeyspaces();

        this.showTable = function(keyspace){
            $state.go("cassandraTableModule",{keyspace:keyspace});
        }

        function fetchKeyspaces(){
            $http.get("/proxy/v1/cassandra/module/keyspace").then(function(response){
                if(response.data){
                    self.keyspaces = response.data;
                }
            })
        }
    };

    angular.module(moduleName).controller('CassandraKeyspaceModuleController',['$state','$transition$','$http','$interval','$timeout',controller]);

});
