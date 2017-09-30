define(['angular', 'plugin/cassandra-module/module-name', 'kylo-utils/LazyLoadUtil', 'constants/AccessConstants', 'kylo-common', 'kylo-services', 'kylo-opsmgr'], function (angular, moduleName, lazyLoadUtil, AccessConstants) {
    var module = angular.module(moduleName, []);


    module.config(['$stateProvider','$compileProvider',function ($stateProvider,$compileProvider) {
        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state('cassandraToggleModule',{
            url:'/cassandraToggleModule',
            params: {
            },
            views: {
            },
            resolve: {
            },
            data:{
            }
        });

    }]);
    return module;
});
