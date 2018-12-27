app.controller("baseController", function ($scope) {

    // 分页控件配置
    $scope.paginationConf = {
        currentPage: 1, //当前页
        totalItems: 10, //总记录数
        itemsPerPage: 10, //每页记录数
        perPageOptions: [5, 10, 15, 20, 30], //分页选项
        onChange: function () { //页面变更后触发的方法
            $scope.reloadList(); //重新加载
        }
    }


    $scope.reloadList = function () {
        // 切换页码
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }


    $scope.selectIds = []; // 复选框选中的集合

    // 跟新复选框集合
    $scope.updateSelection = function (event, id) {
        if (event.target.checked) {
            $scope.selectIds.push(id); // 添加要删除的id
        } else {
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index, 1); // (移除参数索引,移除个数)
        }
    }

    // 提取json字符串中的某个属性，拼接字符串返回
    $scope.jsonToString = function (jsonString, key) {
        var json = JSON.parse(jsonString);// 解析json字符串为json数组
        var value = "";
        for (var i = 0; i < json.length; i++) {
            if (i > 0) {
                value += ",";
            }
            value += json[i][key];
        }
        return value;
    }

    // 从集合中按照key查找对象中是否有keyValue
    $scope.searchObjectByKey = function (list, key, keyValue) {
        for (var i = 0; i < list.length; i++) {
            if (list[i][key] == keyValue) {
                return list[i];
            }
        }
        return null;
    }

});