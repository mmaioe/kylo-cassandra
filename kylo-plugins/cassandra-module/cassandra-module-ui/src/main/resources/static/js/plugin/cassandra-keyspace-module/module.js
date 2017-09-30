define(['angular', 'plugin/cassandra-keyspace-module/module-name', 'kylo-utils/LazyLoadUtil', 'constants/AccessConstants', 'kylo-common', 'kylo-services', 'kylo-opsmgr'], function (angular, moduleName, lazyLoadUtil, AccessConstants) {
    var module = angular.module(moduleName, []);


    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state('cassandraKeyspaceModule',{
            url:'/cassandraKeyspaceModule',
            params: {
            },
            views: {
                'content': {
                    templateUrl: 'js/plugin/cassandra-keyspace-module/cassandra-keyspace-module.html',
                    controller:"CassandraKeyspaceModuleController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['plugin/cassandra-keyspace-module/CassandraKeyspaceModuleController'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Keyspace',
                module:moduleName,
                permissions:[]
            }
        });


        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,['plugin/cassandra-keyspace-module/module-require']);
        }

        function lazyLoad(){
            return lazyLoadUtil.lazyLoad(['plugin/cassandra-keyspace-module/module-require']);
        }

    }]);
    return module;

});
