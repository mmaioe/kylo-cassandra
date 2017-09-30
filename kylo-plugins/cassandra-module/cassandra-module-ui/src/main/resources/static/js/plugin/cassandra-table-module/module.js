define(['angular', 'plugin/cassandra-table-module/module-name', 'kylo-utils/LazyLoadUtil', 'constants/AccessConstants', 'kylo-common', 'kylo-services', 'kylo-opsmgr'], function (angular, moduleName, lazyLoadUtil, AccessConstants) {
    var module = angular.module(moduleName, []);


    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state('cassandraTableModule',{
            url:'/cassandraTableModule',
            params: {
                keyspace: null
            },
            views: {
                'content': {
                    templateUrl: 'js/plugin/cassandra-table-module/cassandra-table-module.html',
                    controller:"CassandraTableModuleController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: lazyLoadController(['plugin/cassandra-table-module/CassandraTableModuleController'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Table Page',
                module:moduleName,
                permissions:[]
            }
        });


        function lazyLoadController(path){
            return lazyLoadUtil.lazyLoadController(path,['plugin/cassandra-table-module/module-require']);
        }

        function lazyLoad(){
            return lazyLoadUtil.lazyLoad(['plugin/cassandra-table-module/module-require']);
        }




    }]);
    return module;







});
