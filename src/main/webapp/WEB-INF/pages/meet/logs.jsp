<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
    <title>约见列表</title>
    <link rel="shortcut icon" type="image/ico" href="../../../resources/img/noc.gif">
    <link href="../../../resources/css/lib/pagebar.css" rel="stylesheet" type="text/css"/>
    <link href="../../../resources/css/lib/tooltipster.css" rel="stylesheet" type="text/css"/>
    <link href="../../../resources/css/lib/tooltipster-noir.css" rel="stylesheet" type="text/css"/>
    <link href="../../../resources/css/meet/logs.css" rel="stylesheet" type="text/css"/>
</head>
<body>
<div class="container">
    <input type="hidden" id="faceUrl" value="${faceUrl}"/>
    <div class="search"><input id="sequence" class="input" type="text" placeholder="请输入约见ID" name=""
                               value=""/><input
            type="button" id="search" class="btn_blue" value="查询"/></div>
    <div class="content" id="content">
        <div class="element">
            <%--<div class="title">标题。。。。<div class="time">2015年10月10日 19:00:00</div></div>--%>
            <%--<div class="detail">详细内容.....详细内容.....详细内容.....详细内容.....详细内容.....详细内容.....详细内容.....详细内容.....详细内容.....详细内容.....详细内容.....</div>--%>
            <%--<div class="img">--%>
            <%--<img src="http://pic14.nipic.com/20110522/7411759_164157418126_2.jpg"/>--%>
            <%--<img src="http://pic14.nipic.com/20110522/7411759_164157418126_2.jpg"/>--%>
            <%--<img src="http://pic14.nipic.com/20110522/7411759_164157418126_2.jpg"/>--%>

            <%--</div>--%>
            <%--</div>--%>

            <%--<hr style="height: 5px;width: 100%;"/>--%>

            <%--<div class="element">--%>
            <%--<div class="title">标题。。。。<div class="time">2015年10月10日 19:00:00</div></div>--%>
            <%--<div class="detail">详细内容.....详细内容.....详细内容.....详细内容.....详细内容.....详细内容.....详细内容.....详细内容.....详细内容.....详细内容.....详细内容.....</div>--%>
            <%--<div class="img">--%>
            <%--<img src="http://pic14.nipic.com/20110522/7411759_164157418126_2.jpg"/>--%>
            <%--<img src="http://pic14.nipic.com/20110522/7411759_164157418126_2.jpg"/>--%>
            <%--<img src="http://pic14.nipic.com/20110522/7411759_164157418126_2.jpg"/>--%>

            <%--</div>--%>
            <%--</div>--%>

        </div>


    </div>
</div>
<div id="pagebar">
    <a href="javascript:void(0);" id="prev">上一页</a>
    <span id="go_pg_num">1</span>
    <a href="javascript:void(0);" id="next">下一页</a>
</div>

<script type="text/javascript" src="../../../resources/js/lib/jquery.js"></script>
<script type="text/javascript" src="../../../resources/js/lib/jquery.tooltipster.min.js"></script>
<script type="text/javascript" src="../../../resources/js/meet/logs.js"></script>
<script type="text/javascript">

    $(document).ready(function () {
        app.meet.logs.init();
        $('.tooltip').tooltipster({
            animation: 'fade',
            delay: 200,
            theme: 'tooltipster-noir',
            touchDevices: false,
            trigger: 'hover',
            contentAsHTML:true
        });

    });

</script>
</body>
</html>

