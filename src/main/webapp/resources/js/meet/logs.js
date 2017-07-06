var app = {
    meet: {}
};

(function (P) {
    var _this = null;
    _this = P.meet.logs = {
        param: {
            skip: 0,
            limit: 10,
            currentPage: 1,
            hasmore: 0,
            changed: 0, //0 stands for no,1 stands for yes,  sequence changed or not
            meetId: 0,
            faceUrl: ''

        },
        init: function () {
            _this.param.faceUrl = $('#faceUrl').val();
            _this.initEvent();
        },
        initEvent: function () {
            $('#search').on('click', function () {
                //var sequence = $('#sequence').val();
                //if (parseInt(sequence) > 0) {
                //    console.log(parseInt(sequence));
                //    _this.search(parseInt(sequence));
                //}
                var meetId = $('#sequence').val();
                if (parseInt(meetId) > 0) {
                    _this.param.meetId = meetId;
                } else {
                    _this.param.meetId = 0;
                }
                _this.param.skip = 0;
                _this.param.limit = 10;
                _this.param.currentPage = 1;
                _this.searchData(0);
            });

            $('#sequence').on('change', function () {

                var meetId = $('#sequence').val();
                if (parseInt(meetId) > 0) {
                    _this.param.meetId = meetId;
                } else {
                    _this.param.meetId = 0;
                }

                _this.param.skip = 0;
                _this.param.limit = 10;
                _this.param.currentPage = 1;
                console.log('changed');
            });

            $('#prev').on('click', function () {
                _this.prevPage();
            });

            $('#next').on('click', function () {
                _this.nextPage();
            });
            _this.searchData(0);


            $('body').on('click', '.btn_logs', function () {
                location.href = '/pushed/' + $(this).attr('data-id');
            });

            $('body').on('click', '.btn_go_push', function () {
                location.href = '/push/' + $(this).attr('data-id');
            });

            $('body').on('click', '.btn_go_view', function () {
                location.href = '/viewed/' + $(this).attr('data-id');
            });
        },

        initSearch: function () {
            _this.param.skip = 0;
            _this.param.limit = 10;
            _this.param.meetId = 0;
        },

        render: function (data) {
            var hasmore = data.hasmore;//if there is any more feed after this request 1stands for yes

            var list = data.data;

            var html = "";

            for (var i = 0; i < list.length; i++) {
                var obj = list[i];
                html += "<div class=\"element\">";
                html += _this.convertAvatar(obj);
                html += _this.convertTitle(obj);
                html += _this.convertOperation(obj);
                html += "</div>";
                html += "<hr style=\"height: 1px;width: 100%;margin-left: -1px;\"/>";
            }
            $("#content").html(html);
            _this.showPage();
            _this.param.hasmore = hasmore;

        },

        searchData: function (meetId) {
            console.log('searchData');
            var postData = {
                "limit": _this.param.limit,
                "skip": _this.param.skip,
                "id": _this.param.meetId
            };
            $.ajax({
                type: 'POST',
                contentType: 'application/json',
                url: '/logsList',
                dataType: "json",
                data: JSON.stringify(postData),
                success: function (data) {
                    if (data.success == 1) {
                        _this.render(data)
                    } else {
                        alert(data.msg);
                    }
                }
            });
        },

        convertPage: function () {
            if (_this.param.hasmore == 1) {//如果有更多

            }
        },


        prevPage: function () {
            if (_this.param.skip == 0) {
                return;
            } else {
                _this.param.skip = _this.param.skip - _this.param.limit;
                _this.param.currentPage = _this.param.currentPage - 1;
                _this.searchData(0);
            }

        },

        nextPage: function () {
            if (_this.param.hasmore == 1) {
                _this.param.skip = _this.param.skip + _this.param.limit;
                _this.param.currentPage = _this.param.currentPage + 1;
                _this.searchData(0);
            } else {
                return;
            }

        },

        showPage: function () {
            $('#go_pg_num').html(_this.param.currentPage);
        },

        convertTitle: function (obj) {
            var title = obj["title"];
            var uid = obj["id"];
            var category = obj["category"];
            var attribute = obj["attribute"]
            var brief = obj["brief"];
            return "<div class=\"title\">" + title + "[" + uid + "]<div class=\"time\"> 运营标签: " + category + "<br/> 用户标签 :" + attribute + "<br/> 自动提取:" + brief + " <br/></div></div>";
        },

        convertAvatar: function (obj) {
            var pic = obj["pic"];
            var html = "";
            var picHtml = "<div>";
            picHtml += "<img src=" + pic + " />";
            picHtml += "</div>";
            html += picHtml;
            return html;
        },

        convertOperation: function (obj) {
            var html = "<div class='operation'>";
            html += "<input class=\"btn_go_push\" type=\"button\" data-id=\"" + obj["id"] + "\"" +
                " value=\"去推送\" title=\"查看系统计算出的未推送用户列表\" /> "
            html += "<input class=\"btn_logs\" type=\"button\" data-id=\"" + obj["id"] + "\" value=\"查看推送记录\"" +
                " title=\"查看该约见已经推送的用户列表\" />";

            html += "<input class=\"btn_go_view\" type=\"button\" data-id=\"" + obj["id"] + "\"" +
                " value=\"查看点击记录\" title=\"查看已点击用户列表\" /> "
            html += "</div>";
            return html;
        }
        ,


    };
})(app);