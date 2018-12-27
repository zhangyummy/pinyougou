app.controller('contentController', function ($scope, contentService) {

    //广告集合
    $scope.contentList = [];

    //根据分类 ID 查询广告列表
    $scope.findByCategoryId = function (categoryId) {
        contentService.findByCategoryId(categoryId).success(function (response) {
            $scope.contentList[categoryId] = response;
        });
    }

    //搜索跳转到search_web
    $scope.search = function () {
        location.href = "http://localhost:9104/search.html#?keywords=" + $scope.keywords;
    }


});