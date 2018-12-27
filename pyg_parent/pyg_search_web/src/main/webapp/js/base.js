var app = angular.module("pyg", []);

//过滤器
/*
app.filter("trustHtml", ["$sce", function ($sce) {
    return function (data) {
        return $sce.trustAsHtml(data);
    }
}]);*/


app.filter("trustHtml", function ($sce) {
    return function (data) {
        return $sce.trustAsHtml(data);
    }
});
