app.service("brandService", function ($http) {
    this.findAll = function () {
        return $http.get("../brand/findAll.do");
    }

    this.findPage = function (pageNum, pageSize) {
        return $http.get(`../brand/findPage.do?pageNum=${pageNum}&pageSize=${pageSize}`);
    }

    this.save = function (entity) {
        return $http.post("../brand/save.do", entity);
    }

    this.findOne = function (id) {
        return $http.get("../brand/findOne.do?id=" + id);
    }

    this.del = function (selectIds) {
        return $http.get("../brand/delete.do?ids=" + selectIds);
    }

    this.search = function (pageNum, pageSize, searchEntity) {
        return $http.post(`../brand/search.do?pageNum=${pageNum}&pageSize=${pageSize}`, searchEntity);
    }


    //查询品牌下拉框数据
    this.selectOptionList = function () {
        return $http.get("../brand/selectOptionList.do");
    }
});