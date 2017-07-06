var app = {
    meet: {}
};

(function (P) {
    var _this = null;
    _this = P.meet.push = {
        param: {
            skip: 0,
            limit: 47,
            currentPage: 1,
            hasmore: 0,
            changed: 0, //0 stands for no,1 stands for yes,  sequence changed or not
            id: 0,
            faceUrl: '',
            pushed: 0
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


            $('body').on('click', '.btn_logs', function () {
                //_this.orgUid();
                var uids = "";
                $("input[type=\"checkbox\"]:checked").each(function () {
                    //由于复选框一般选中的是多个,所以可以循环输出
                    uids += $(this).attr('data-id')+",";
                });


                var manUids = $('#input_uid_area').val();

                var resultManUids = '';

                if(!manUids){
                    manUids = '';
                }else{
                    manUids =  manUids.replace(new RegExp('，','gm'),',')
                    console.log(manUids);
                    var uidList = manUids.split(",");

                    for(var i in uidList){
                        if(parseInt(uidList[i]) >0){
                            resultManUids += parseInt(uidList[i])+",";
                        }
                    }
                }

                if (uids == "" && resultManUids == "") {
                    alert('请选择要推送的用户');
                    return;
                }
                if(confirm("确认推送?")){
                    _this.pushMessage(uids,resultManUids);
                }
            });
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
                html += _this.convertOperation(obj);
                html += "</div>";

            }
            $("#content").html(html);
            _this.showPage();
            _this.param.hasmore = hasmore;
        },

        pushMessage: function (uids,manUids) {
            console.log('pushMessage');
            var postData = {
                "id": _this.param.id,
                "uids": uids,
                "manUids":manUids
            };
            $.ajax({
                type: 'POST',
                contentType: 'application/json',
                url: '/push',
                dataType: "json",
                data: JSON.stringify(postData),
                success: function (data) {
                    if (data.success == 1) {
                        alert('推送成功');
                        _this.searchData(0)
                    } else {
                        alert(data.msg);
                    }
                }
            });
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
                        _this.render(data);
                        _this.inputUidArea();
                        _this.searchStatus();
                    } else {
                        alert(data.msg);
                    }
                }
            });
        },

        searchStatus: function () {

            $.ajax({
                type: 'GET',
                url: '/findStatus?objectId='+_this.param.id,
                dataType: "json",
                success: function (data) {
                    if (data.success == 1) {
                        var log = data["data"];
                        console.log(log)
                        if(log["forbidden"] == 1 || log["expired"] ==1){
                            _this.disabledComponent();
                        }
                    } else {
                        console.log(data.msg);
                    }
                }
            });
        },

        disabledComponent:function(){
            $(".checkbox").attr("disabled","disabled");
            $(".btn_logs").attr("disabled","disabled");
            $("#input_uid_area").attr("disabled","disabled");
            $("#input_uid_area").val('该约见已失效')
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
            html += "<input class=\"btn_logs\" type=\"button\"  value=\"推送\"" +
                " title=\"确认已选用户并推送约见\" />";
            html += "</div>";
            return html;
        }
        ,

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
                    info += "业务标签:[" + obj["my_goodat_key"] + "]\r\n";
                    info += "学校:["+obj["education"] +"]\r\n";
                    info += "personmark:["+obj["personmark"] +"]\r\n";
                    info += "profession:["+obj["profession"] +"]\r\n";
                    info += "worktrack:["+obj["worktrack"] +"]\r\n";
                    info += "travelwantgo:["+obj["travelwantgo"] +"]\r\n";
                    info += "travelgone:["+obj["travelgone"] +"]\r\n";
                    info += "moviedone:["+obj["moviedone"] +"]\r\n";
                    info += "moviewantdo:["+obj["moviewantdo"] +"]\r\n";
                    info += "bookdone:["+obj["bookdone"] +"]\r\n";
                    info += "bookwantdo:["+obj["bookwantdo"] +"]";

                    var picHtml = "<div class=\"tooltip\" title=\" " + info + ";\" >";
                    picHtml += "<img  style=\"max-width: 80px; height: 80px;margin-left: 5px;\" src=" + _this.param.faceUrl + obj["id"] + "/face.jpg />";
                    picHtml += "<input type=\"checkbox\" class=\"checkbox\" data-id=\"" + obj["id"] + "\"/>";
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
        orgUid: function () {
            var list = $('.checkbox');
            console.log(list);
        },
        inputUidArea:function(){
            $('.title').append('<div class="uid_area"><textarea id="input_uid_area" style="resize: none;background-color: #eeeeee;" placeholder="请输入要推送的uid，以英文“,”分隔" cols="25" rows="5"></textarea></div>')
        }

    };
})(app);