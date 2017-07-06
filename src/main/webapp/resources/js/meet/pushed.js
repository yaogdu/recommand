var app = {
    meet: {}
};

(function (P) {
    var _this = null;
    _this = P.meet.pushed = {
        param: {
            skip: 0,
            limit: 48,
            currentPage: 1,
            hasmore: 0,
            changed: 0, //0 stands for no,1 stands for yes,  sequence changed or not
            id: 0,
            faceUrl: '',
            pushed: 1

        },
        init: function () {
            _this.param.faceUrl = $('#faceUrl').val();
            _this.param.id = $('#meetId').val();
            console.log(_this.param.id);
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
                    _this.param.id = meetId;
                } else {
                    _this.param.id = 0;
                }
                _this.param.skip = 0;
                _this.param.limit = 50;
                _this.param.currentPage = 1;
                _this.searchData(0);
            });

            $('#return_home').on('click',function(){
                location.href='/';
            });

            $('#sequence').on('change', function () {

                var meetId = $('#sequence').val();
                if (parseInt(meetId) > 0) {
                    _this.param.id = meetId;
                } else {
                    _this.param.id = 0;
                }

                _this.param.skip = 0;
                _this.param.limit = 50;
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

        },

        initSearch: function () {
            _this.param.skip = 0;
            _this.param.limit = 50;
            _this.param.id = 0;
        },

        render: function (data) {
            var hasmore = data.hasmore;//if there is any more feed after this request 1stands for yes

            var list = data.data;

            var html = "";

            for (var i = 0; i < list.length; i++) {
                var mapping = list[i];

                var obj = mapping.feed;

                var users = mapping.users;

                html += "<div class=\"element\">";
                html += _this.convertAvatar(obj);
                html += _this.convertTitle(obj);
                html += "<hr style=\"height: 1px;width: 105%;margin-left: -1px;\"/>";
                html += _this.convertList(users);
                html += "</div>";

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
                "id": _this.param.id,
                "pushed": _this.param.pushed
            };
            $.ajax({
                type: 'POST',
                contentType: 'application/json',
                url: '/list',
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

        convertList: function (users) {
            var html = "<div class=\"list_img\">";

            if (users.length > 0) {

                for (var j = 0; j < users.length; j++) {
                    var obj = users[j];
                    var info = "id:[" + obj["id"] + "] \r\n";
                    info += "姓名:[" + obj["name"] + "]\r\n";
                    info += "机遇号:[" + obj["sequence"] + "]\r\n";
                    info += "公司:[" + obj["company"] + "]\r\n";
                    info += "职位:[" + obj["post"] + "]\r\n";
                    info += "兴趣爱好:[" + obj["mytag"] + "]\r\n";
                    info += "业务标签:[" + obj["my_goodat_key"] + "]";

                    var picHtml = "<div class=\"tooltip\" title=\" " + info + ";\" >";
                    picHtml += "<img  style=\"max-width: 80px; height: 80px;margin-left: 5px;\" src=" + _this.param.faceUrl + obj["id"] + "/face.jpg />";
                    picHtml += "</div>";
                    html += picHtml;
                }
            } else {
                html += "暂时没有数据";
            }
            html += "</div>";
            return html;
        }
        ,


    };
})(app);