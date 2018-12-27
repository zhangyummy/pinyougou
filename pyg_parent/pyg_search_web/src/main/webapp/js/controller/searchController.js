app.controller('searchController', function ($scope, $location, searchService) {

    $scope.searchMap = {
        keywords: "",
        category: "",
        brand: "",
        spec: {},
        price: '',
        pageNum: 1,
        pageSize: 20,
        sortField: "",
        sort: ""
    };

    //搜索
    $scope.search = function () {
        $scope.searchMap.pageNum = parseInt($scope.searchMap.pageNum);
        searchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap = response;
            console.info($scope.resultMap);
            //构建分页条
            buildPageLabel();
        });
    }

    //添加搜索项方法
    $scope.addSearchItem = function (key, value) {
        if (key == "category" || key == "brand" || key == "price") {
            $scope.searchMap[key] = value;
        } else {
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();//执行查询
    }

    //移除搜索项方法
    $scope.removeSearchItem = function (key) {
        if (key == "category" || key == "brand" || key == "price") {
            $scope.searchMap[key] = "";
        } else {
            delete $scope.searchMap.spec[key];//删除此属性
        }
        $scope.search();//执行查询
    }

    //构建分页栏
    buildPageLabel = function () {
        $scope.pageLabel = [];
        var totalPage = $scope.resultMap.totalPage;//总页数
        var startPage = 1;
        //开始结束页码
        var endPage = totalPage;
        var pageNum = $scope.searchMap.pageNum;//当前页
        //点点是否显示
        $scope.startDot = false;
        $scope.endDot = false;
        //总页数大于5
        if (totalPage > 5) {
            if (pageNum <= 3) {//前5页
                endPage = 5;
                $scope.endDot = true;
            } else if (pageNum + 2 >= totalPage) {//后5页
                startPage = totalPage - 4;
                $scope.startDot = true;
            } else {//中间5页
                startPage = pageNum - 2;
                endPage = pageNum + 2;
                $scope.startDot = true;
                $scope.endDot = true;
            }
        }
        for (var i = startPage; i <= endPage; i++) {
            $scope.pageLabel.push(i);
        }
    }

    //根据页码搜索
    $scope.queryByPage = function (pageNum) {
        if (pageNum <= 0 || pageNum > $scope.resultMap.totalPage) {
            return;
        }
        $scope.searchMap.pageNum = pageNum;
        $scope.search();
    }

    //排序搜索
    $scope.sortQuery = function (sortField, sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;
        $scope.search();
    }

    //判断搜索的关键字是否为品牌
    $scope.keywordsIsBrand = function () {
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0) {
                return true;
            }
        }
        return false;
    }

    //接收首页传过来的参数并搜索
    $scope.searchFromIndex = function () {
        if ($location.search().keywords != null && $location.search().keywords != "undefined") {
            $scope.searchMap.keywords = $location.search().keywords;
            $scope.search();
        }
    }

});