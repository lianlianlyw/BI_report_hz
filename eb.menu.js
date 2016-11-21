;
var ebNavi = {};
var now_user;
/*
*   2014/7/20
*   左侧树形菜单tab(后台/系统管理)
*   ztree实现
*/

ebNavi.manager = function(options){
	var setting = {
		view: {
			showLine: false,
			showIcon: false,
			selectedMulti: false,
			dblClickExpand: false,
			addDiyDom: addDiyDom,
			showIcon:false
		},
		data: {
			simpleData: {
				enable: true
			}
		},
		callback: {
			beforeClick: beforeClick
		}
	};
	
	var zNodes = [
					{ id:1, pId:0, name:"系统管理", open:true},
					{ id:11, pId:1, name:"用户管理",url:ctxn + "/manager/security/user.action",target:"_container"},
					{ id:12, pId:1, name:"角色管理",url:ctxn + "/manager/security/role.action",target:"_container"},
					{ id:13, pId:1, name:"权限管理",url:ctxn + "/manager/security/authority.action",target:"_container"},
					{ id:14, pId:1, name:"资源管理",url:ctxn + "/manager/security/resource.action",target:"_container"},
					{ id:15, pId:1, name:"组织机构管理",url:ctxn + "/manager/security/organization.action",target:"_container"},
					{ id:16, pId:1, name:"系统配置管理",url:ctxn + "/manager/security/sysconfig.action",target:"_container"},
					{ id:2, pId:0, name:"系统日志管理", open:true },
					{ id:21, pId:2, name:"系统维护日志",url:ctxn + "/manager/optlog/opt-log-mng.action",target:"_container"},
					{ id:22, pId:2, name:"访问日志统计",url:ctxn + "/manager/optlog/opt-log.action",target:"_container"},
					{ id:3, pId:0, name:"报表应用管理", open:true },
					{ id:31, pId:3, name:"用户订阅报表",url:ctxn + "/manager/report/user-subscribe.action",target:"_container"},
					{ id:32, pId:3, name:"用户收藏报表",url:ctxn + "/manager/report/user-collect.action",target:"_container"},
					{ id:33, pId:3, name:"反馈建议管理",url:ctxn + "/manager/user/feedback-manage.action",target:"_container"},
					{ id:4, pId:0, name:"任务管理", open:true },
					{ id:41, pId:4, name:"全部任务",url:ctxn + "/manager/task/task-manage.action",target:"_container"}
				];
	
	var treeObj = $("#"+options.treeNodeId);
	$.fn.zTree.init(treeObj, setting, zNodes);
	//zTree_Menu = $.fn.zTree.getZTreeObj(options.treeNodeId);
	//curMenu = zTree_Menu.getNodes()[0].children[0].children[0];
	//zTree_Menu.selectNode(curMenu);

	treeObj.hover(function () {
		if (!treeObj.hasClass("showIcon")) {
			treeObj.addClass("showIcon");
		}
	}, function() {
		treeObj.removeClass("showIcon");
	});
	
	
	
	function addDiyDom(treeId, treeNode){
		var spaceWidth = 5;
		var switchObj = $("#" + treeNode.tId + "_switch"), icoObj = $("#" + treeNode.tId + "_ico");
		switchObj.remove();
		icoObj.before(switchObj);
		
		if (treeNode.level > 1) {
			var spaceStr = "<span style='display: inline-block;width:" + (spaceWidth * treeNode.level) + "px'></span>";
			switchObj.before(spaceStr);
		}
	}
	
	function beforeClick(treeId, treeNode){
		if (treeNode.level == 0) {
			var zTree = $.fn.zTree.getZTreeObj(options.treeNodeId);
			zTree.expandNode(treeNode);
			return false;
		}
		return true;
	}
	
}


/*
*   2014/7/20
*   左侧树形菜单tab(后台/报表设计)
*   ztree实现
*/

ebNavi.reportMenu = function(options){
	var setting = {
		view: {
			showLine: false,
			showIcon: false,
			selectedMulti: false,
			addDiyDom: addDiyDom,
			showIcon:false
		},
		data: {
			simpleData: {
				enable: true
			}
		},
		callback: {
			beforeClick: beforeClick
		}
	};
	var treeObj = $("#"+options.treeNodeId);
	options.success = function(data) {     //ajax success回调函数
		var zNodes =  data.data;
		$(zNodes).each(function(i,n){
			//console.log(n);
			if(n.url){
				n.url = ctxn + n.url;
			}
		});
		//console.log(zNodes);
		var extNodeArr =[{id:100,pId:0,name:"报表元数据管理",open:true},
			{id:101,pId:100,name:"维度配置管理",url:ctxn + "/manager/metadata/dimension.action",target:"_container"},
			{id:102,pId:100,name:"指标配置管理",url:ctxn + "/manager/metadata/measure.action",target:"_container"},
			{id:103,pId:100,name:"优质书单排行榜参数配置",url:ctxn + "/manager/metadata/sheetrank-config.action",target:"_container"},
			{id:200,pId:0,name:"报表生成器",open:true},
			{id:201,pId:200,name:"报表生成器",url:ctxn + "/manager/report/report-manager.action?rid=null",target:"_container"}];
		//zNodes.push(extNodeArr);
		zNodes = zNodes.concat(extNodeArr);
		$.fn.zTree.init(treeObj, setting, zNodes);
	};
	
	$.ebAjax(options);
	
	function addDiyDom(treeId, treeNode){
		var spaceWidth = 5;
		var switchObj = $("#" + treeNode.tId + "_switch"), icoObj = $("#" + treeNode.tId + "_ico");
		switchObj.remove();
		icoObj.before(switchObj);
		
		if (treeNode.level > 1) {
			var spaceStr = "<span style='display: inline-block;width:" + (spaceWidth * treeNode.level) + "px'></span>";
			switchObj.before(spaceStr);
		}
	}
	
	function beforeClick(treeId, treeNode){
		if (treeNode.level == 0) {
			var zTree = $.fn.zTree.getZTreeObj(options.treeNodeId);
			zTree.expandNode(treeNode);
			return false;
		}
		return true;
	}
	
}



;

/*
*   2014/8/01
*   portal前端系统报表树形菜单(依据权限不同展示报表菜单不同)
*   ztree实现
*/
ebNavi.portalMenu = function(options){
	var setting = {
		view: {
			showLine: true,
			selectedMulti: false,
			dblClickExpand: false
		},
		data: {
			simpleData: {
				enable: true
			}
		},
		callback: {
			onClick: menuOnClick
			//beforeClick: beforeClick
		}
	};
	var treeObj = $("#"+options.treeNodeId);
	options.success = function(data) {     //ajax success回调函数
		var zNodes =  data.data;
		$.fn.zTree.init(treeObj, setting, zNodes);
		
		treeObj.hover(function () {
			if (!treeObj.hasClass("selected")) {
				treeObj.addClass("selected");
			}
		}, function() {
			treeObj.removeClass("selected");
		});
		
		
	};
	
	$.ebAjax(options);
	
	function menuOnClick(event, treeId, treeNode) {
		
    	//alert(treeNode.tId + ", " + treeNode.name);
	};
	
	
}


;
/*
*   2014/8/01
*   菜单中的搜索框默认文字显示效果
*   [input框鼠标离开时显示提示文字，鼠标点击时空白可输入状态]
*/
ebNavi.searchStyle = function(inputId){
	//navigation search
	var search_val = $("#"+inputId).val();
		$("#"+inputId).bind({
			focus: function(){
				if($(this).val() == search_val){
					$(this).val("");
				}
			},
			blur: function(){
				if($(this).val() == ""){
					$(this).val(search_val);
				}
			}
	   });
}









;
ebReportWidget = {};
/*  
*   2014/8/18
*   report权限树形结构
*   ztree实现
*/
ebReportWidget.authTree = function(options){
	var setting = {
		check: {
			enable: true,
			chkStyle: "checkbox",
			chkboxType: { "Y": "ps", "N": "ps" }
		},
		async: {
			enable: true,
			url: options.url,
			dataFilter: filter,
			otherParam: ["id",options.id]
		},
		data: {
			simpleData: {
				enable: true
			}
		},
		callback: {
			onAsyncSuccess: zTreeOnAsyncSuccess
		}
	};

	var treeObj = $("#"+options.treeNodeId);
	$.fn.zTree.init(treeObj, setting);
	
	//异常空数据情况处理
	function filter(treeId, parentNode, childNodes) {
		if (!childNodes) return null;
		for (var i=0, l=childNodes.length; i<l; i++) {
			childNodes[i].name = childNodes[i].name.replace(/\.n/g, '.');
		}
		//orgTreeData_json = childNodes;
		return childNodes;
	}
	
	//异步加载成功回调函数
	function zTreeOnAsyncSuccess(event, treeId, treeNode, msg) {
    	//var json_node = eval("(" + msg + ")");
		options.zTreeOnAsyncSuccess(event, treeId, treeNode, msg)
	};
}



;
/*  暂时废弃
*   2014/7/25
*   左侧树形菜单(前台)
*   ztree实现
*/
ebNavi.frontMenu = function(options){
	var setting = {
		view: {
			showLine: false,
			showIcon: false,
			selectedMulti: false,
			dblClickExpand: false,
			addDiyDom: addDiyDom
		},
		data: {
			simpleData: {
				enable: true
			}
		},
		callback: {
			beforeClick: beforeClick
		}
	};
	
	var zNodes = [
					{ id:1, pId:0, name:"web报表", open:true},
					{ id:11, pId:1, name:"运营日报",url:"/mrrs-web/portal/report/simpleReport.action",target:"_container"},
					{ id:12, pId:1, name:"KPI报表",url:"",target:"_container"},
					{ id:13, pId:1, name:"渠道分析报表",url:"",target:"_container"},
					{ id:2, pId:0, name:"用户中心", open:true },
					{ id:21, pId:2, name:"个人中心",url:"/mrrs-web/portal/userinfo/user-center.action",target:"_container"},
/*					{ id:22, pId:2, name:"我的订阅",url:"/mrrs-web/portal/userinfo/subscribe.action",target:"_container"},
					{ id:23, pId:2, name:"我的模板",url:"/mrrs-web/portal/userinfo/query-template.action",target:"_container"},*/
					{ id:25, pId:2, name:"个人信息",url:"",target:""},
					{ id:24, pId:2, name:"反馈建议",url:""}
				];
	
	var treeObj = $("#"+options.treeNodeId);
	$.fn.zTree.init(treeObj, setting, zNodes);
	//zTree_Menu = $.fn.zTree.getZTreeObj(options.treeNodeId);
	//curMenu = zTree_Menu.getNodes()[0].children[0].children[0];
	//zTree_Menu.selectNode(curMenu);

	treeObj.hover(function () {
		if (!treeObj.hasClass("showIcon")) {
			treeObj.addClass("showIcon");
		}
	}, function() {
		treeObj.removeClass("showIcon");
	});
	
	
	
	function addDiyDom(treeId, treeNode){
		var spaceWidth = 5;
		var switchObj = $("#" + treeNode.tId + "_switch"), icoObj = $("#" + treeNode.tId + "_ico");
		switchObj.remove();
		icoObj.before(switchObj);
		
		if (treeNode.level > 1) {
			var spaceStr = "<span style='display: inline-block;width:" + (spaceWidth * treeNode.level) + "px'></span>";
			switchObj.before(spaceStr);
		}
	}
	
	function beforeClick(treeId, treeNode){
		if (treeNode.level == 0) {
			var zTree = $.fn.zTree.getZTreeObj(options.treeNodeId);
			zTree.expandNode(treeNode);
			return false;
		}
		return true;
	}
}


/**意见反馈*/
function new_feedback(){
    if ( $("#new_feedback_view").length > 0 ){
		if($("#feedback_info").length >0){
			$("#feedback_info").text("");
		}
		if($("#input_text_area").length >0){
			$("#input_text_area").val("");
		}
    }
    else{
        var appendHtml = "<div id=\"new_feedback_view\" class=\"modal hide fade\" tabindex=\"-1\" role=\"dialog\" aria-labelledby=\"myModalLabel\" aria-hidden=\"true\" style=\"font-family:Microsoft YaHei,​SimSun;\">";
        appendHtml += "<div class=\"modal-header\">";
        appendHtml += "<button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-hidden=\"true\" style=\"float:right;\">×</button>";
        appendHtml += "<h3 id=\"myModalLabel\">意见反馈</h3>";
        appendHtml += "</div>";
        appendHtml += "<div class=\"modal-body\">";
//        appendHtml += "<span class=\"label label-info\">标题</span>";
//        appendHtml += "<textarea style=\"width:82%;height:20px;\" id=\"view_title\"></textarea><br>";
        appendHtml += "<span class=\"label label-info\">内容</span>";
        appendHtml += "<textarea style=\"width:82%;height:100px;margin-left:1%;\" id=\"input_text_area\"></textarea>";
        appendHtml += "</div>";
        appendHtml += "<h3 id=\"feedback_info\" style='margin-left:30%;color:#0A93CC;'></h3>"
        appendHtml += "<div class=\"modal-footer\">";
        appendHtml += "<button class=\"btn btn-primary\" onclick=\"add_view(false,'/mrrs-web')\">提交</button>";
        appendHtml += "<button class=\"btn\" data-dismiss=\"modal\" aria-hidden=\"true\">关闭</button>";
        appendHtml += "</div></div>";
        $("body").append(appendHtml);
    }


    var top = ($(window).height() - $("#new_feedback_view").height())/2;
    var left = ($(window).width())/2;
    var scrollTop = $(document).scrollTop();
    var scrollLeft = $(document).scrollLeft();
    $("#new_feedback_view").css( { position : 'absolute', 'top' : top + scrollTop, left : left } ).modal({

    });
}

function add_view(ifAsyn,ctx){
    if(now_user == undefined){
        now_user = "mitsuhide";
    }
    var creator = now_user;
    var title = $.trim($("#view_title").val());
    var content = $.trim($("#input_text_area").val());
//    if(title == ""){
//        $("#feedback_info").css("color","red");
//        $("#feedback_info").text("意见标题不能为空！重新输入！");
//        $("#feedback_info").css("color","#0A93CC");
//        return;
//    }
//    else if(title.length > 255){
//        $("#feedback_info").css("color","red");
//        $("#feedback_info").text("意见标题不能超过255个字符！重新输入！");
//        $("#feedback_info").css("color","#0A93CC");
//        return;
//    }
    if(content == ""){
        $("#feedback_info").css("color","red");
        $("#feedback_info").text("意见内容不能为空！重新输入！");
        $("#feedback_info").css("color","#0A93CC");
        return;
    }
    else if(content.length > 4000){
        $("#feedback_info").css("color","red");
        $("#feedback_info").text("意见内容不能超过4000个字符！重新输入！");
        $("#feedback_info").css("color","#0A93CC");
        return;
    }
    $.ajax({
        type: "POST",
        url:  ctx+"/portal/userinfo/feed-back!save.action",
//        data: {"title": title,"content":content,"creator":creator},
        data: {"content":content,"creator":creator},
        dataType: "json",
        success: function (data) {
            $("#feedback_info").text(data.success_info);
//            location.href=ctx+"/portal/main.action";
        }
    });
}

function feedback_info_callback(){
    setTimeout(function() {
        $( "#feedback_info_result" ).removeAttr( "style" ).fadeOut();
    }, 1000 );
}

function post_method(URL, PARAMS) {
    var temp = document.createElement("form");
    temp.action = URL;
    temp.method = "post";
    temp.target= "_container";
    temp.style.display = "none";
    for (var x in PARAMS) {
        var opt = document.createElement("textarea");
        opt.name = x;
        opt.value = PARAMS[x];
        // alert(opt.name)
        temp.appendChild(opt);
    }
    document.body.appendChild(temp);
    temp.submit();
    return temp;
}
