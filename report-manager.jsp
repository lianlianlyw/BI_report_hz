<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
		 import="org.springside.modules.security.springsecurity.SpringSecurityUtils"
%>
<%@include file="/common/taglibs.jsp"%>
<%
	request.setAttribute("loginName",SpringSecurityUtils.getCurrentUserName());
%>
<!DOCTYPE html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>报表配置管理--手机阅读BI报表系统</title>
	<link rel="stylesheet"  type="text/css" href="${ctx}/plugins/bootstrap/css/bootstrap.css" />
	<link rel="stylesheet"  type="text/css" href="${ctx}/plugins/ztree3/css/zTreeStyle/zTreeStyle.css" />
	<link rel="stylesheet"  type="text/css" href="${ctx}/styles/style.css" />
	<link rel="stylesheet"  type="text/css"  href="${ctx}/plugins/jquery-ui-1.9.2/themes/base/jquery-ui-1.10.3.custom.css" />
	<link rel="stylesheet"  type="text/css"  href="${ctx}/plugins/jqgrid/css/ui.jqgrid.css" />
	<link rel="stylesheet"  type="text/css"  href="${ctx}/styles/report.css" />
	<style type="text/css">
		.uploadifyQueueItem{
			display: inline;
		}
		.cancel{
			float: right;
			margin-right: 20px;
		}
	</style>
</head>
<body>
<div class="frame-content span1">
	<div class="formbar">
		<fieldset>
			<p class="line searchbox">
				<label class="lab" for="searchKey">搜索报表：</label>
				<input type="text" name="searchKey" id="searchKey" data-placeholder="报表名称" placeholder="报表名称"/>
			</p>
			<p class="line searchbox">
				<label class="lab" for="status">报表状态：</label>
				<select name="status" id="state" >
					<option value="" selected="selected">全部</option>
					<option value="1" >正式发布</option>
					<option value="0">草稿</option>
				</select>
			</p>
			<p class="line">
				<a id="btn-query" class="btn" style="font-size:12px;">查询</a>
			</p>
		</fieldset>
		<input id = "generateXml" type = "button" value = "配置" />

	</div>
	<div class="col_main">
		<div class="main_bd"></div>
	</div>

</div>
<!-- js文件放在页面西方，加快页面的渲染速度 -->
<script type="text/javascript" src="${ctx}/plugins/jquery/jquery-1.8.3.js" ></script>
<script type="text/javascript" src="${ctx}/plugins/template.js" ></script>
<script type="text/javascript" src="${ctx}/plugins/jqgrid/js/jquery.jqGrid.src.js" ></script>
<script type="text/javascript" src="${ctx}/plugins/jqgrid/js/i18n/grid.locale-en.js" ></script>
<script type="text/javascript" src="${ctx}/js/ebmrrs.template.js" ></script>
<script type="text/javascript" src="${ctx}/js/eb.framework.js" ></script>
<script type="text/javascript" src="${ctx}/js/eb.jqueryplugin.js" ></script>
<script type="text/javascript" src="${ctx}/js/eb.grid.js" ></script>
<script type="text/javascript" src="${ctx}/js/eb.tip.js" ></script>
<script type="text/javascript" src="${ctx}/js/manager/report/report.js" ></script>
<script type="text/javascript" src="${ctx}/plugins/ztree3/js/jquery.ztree.all-3.5.js" ></script>
<script type="text/javascript" src="${ctx}/plugins/uploadify/swfobject.js"></script>
<script type="text/javascript" src="${ctx}/plugins/uploadify/jquery.uploadify.v2.1.4.js"></script>


<script type="text/javascript">
	var URL = '${ctx}/manager/report/report-manager!';
	EBUPT.Variable.ctx = '${ctx}/';
	$(document).ready(function() {
		$('body').show();

		$('.frame-content').myplace(EBUPT.report);
		$('.main_bd').append(ebTemplate.gridContentT(EBUPT.report.manage));
		$('#toolbar').append(EBUPT.tpl.toolbarT);


		//默认表格展示全部数据
		var options = {
			$target: $('.content-main-grid'),
			id: 'report-manage',
			columns_url: '${ctx}/data/report/report_cfg.json',
			grid_url: URL + 'page.action?rid='+ ${param.rid},
			height: 280,
			shrinkToFit: false,
			grid_type: 'checkGrid'
		};

		queryData(options);
		bindEvent();
		EBUPT.Util.upload();

	});

	//自定义函数区
	function bindEvent(){
		//报表下线功能
		$(".stop").die().live("click",function(){
			var id = getSelectedRow();
			console.log(record.split(",")[1]);
			var dialog = new EBUPT.Util.Dialog( {
				type: 4,
				title: '报表发布配置',
				content: "是否确定下线?",
				detail: '',
				btnType: 1,
				hideCloseIcon : true,
				winSize : 1,
				extra : {autoClose:false},
			}, function(){
				window.location.href = URL + "offLine.action?id=" + record.split(",")[1];
			});
		});
		//报表发布功能
		$(".publish").die().live("click",function(){
			var id = getSelectedRow();
			if(id === null || id.length <= 0 ){
				EBUPT.Util.Dialog( {type : 5,content: "请选择所要发布的报表!",btnType : 3});
				return;
			}
			var formT = EBUPT.Util.render({
				tpl : EBUPT.tpl.formT,
				data : {
					data : ""
				}
			});
			var dialog = EBUPT.Util.Dialog( {
				type : 4,
				content: "是否正式发布该报表?",
				btnType : 6,
				buttons : {
					text1 : '发布',
					text2 : '取消'
				}
			}, function(){
				$.get(URL + "publish.action", { "id": id }, function(data){
					if(data.success){
						EBUPT.Util.Dialog( {type : 3,content: data.msg,btnType : 3});
						jQuery('#eb_grid_report-manage').trigger("reloadGrid");
					}else{
						EBUPT.Util.Dialog( {type : 6,content: data.msg,btnType : 3});
					}
				}, "json");
			});
		});
		//lyw
		/*$("#generateXml").die().live("click", function(){
		 jQuery('#generate_xml').jqGrid('setGridParam',{
		 url: URL + 'getNewDataReport.action',
		 //postData: {searchKey:$("#searchKey").val(),state:$("#state").val()}
		 }).trigger("reloadGrid");
		 });*/

		$("#generateXml").die().live("click",function(){
			$.post(URL + "getNewDataReport.action",
					function(data){
						console.log(data.success)
						var dialog = new EBUPT.Util.SuperDialog( {
							type: 1,
							title: '可配置报表信息',
							content: data.tableName,
							detail: '',
							btnType: 3,
							winSize : 2
						}, function(){
							var options = {
								$target: $('.content-main-grid'),
								id: 'new-report',
								columns_url: '${ctx}/data/report/report_new_data.json',
								grid_url: URL + "getNewDataReport.action",
								height: 280,
								shrinkToFit: false,
								grid_type: 'checkGrid'
							};
							ebGrid.ajaxJqGrid(options);
						});
						/*$.post(URL + "generateDeployFile.action",{"tableName":"总体运营日报表测试","tableCycleType":"1"},
								function(data){

								}, "json");*/
					}, "json");
		});


		//查看报表详情
		$(".look").die().live("click",function(){
			var id = getSelectedRow();
			if(id === null || id.length <= 0 ){
				EBUPT.Util.Dialog( {type : 5,content: "请先选择报表!",btnType : 3});
				return;
			}
			$.post(URL + "view.action", { "id": id },
					function(data){
						if(data.success){
							var content = EBUPT.Util.render({tpl : EBUPT.tpl.contentView,data : data});
							var dialog = new EBUPT.Util.SuperDialog( {
								type: 1,
								title: '报表配置信息详情',
								content: content,
								detail: '',
								btnType: 3,
								winSize : 2
							}, function(){

							});
						}else{
							EBUPT.Util.Dialog( {type : 6,content: "查看报表详情数据失败,请重试!",btnType : 3});
						}
					}, "json");
		});
		//更新报表基本信息
		$(".edit").unbind().bind("click",function(){
			var id = getSelectedRow();
			if(id === null || id.length <= 0 ){
				EBUPT.Util.Dialog( {type : 5,content: "请先选择报表!",btnType : 3});
				return;
			}
			$.get(URL + "getDraft.action", { "id": id },
					function(data){
						if(data.success){
							var content = EBUPT.Util.render({tpl : EBUPT.tpl.formT,data : {data:data}});
							$('#change_dir').die().live("click",function(){
								var dirDialog = EBUPT.Util.Dialog( {title: '报表发布目录树',type : 1,content: '',btnType : 1,hideCloseIcon : true,extra : {zIndex: 9999}});
								//加载tree
								$("#" + dirDialog.contentId).append('<ul id="orgTree" class="ztree"></ul>');
								EBUPT.Variable.treeSetting.async.url = EBUPT.Variable.ctx + "data/report/report_dir.json";
								$.fn.zTree.init($("#orgTree"), EBUPT.Variable.treeSetting);
							});
							var dialog = new EBUPT.Util.Dialog({
								type: 1,
								title: '编辑报表配置信息',
								content: content,
								detail: '',
								btnType: 1,
								winSize : 2
							}, function(){
								validateParamAndAjax(id, URL + "updateDraft.action","update") ;
								dialog.closeWindows();

							});
						}else{
							EBUPT.Util.Dialog( {type : 6,content: "获取报表详情数据失败,请重试!",btnType : 3});
						}
					}, "json");
		});

		//上传报表配置文件
		$('.new').unbind().bind("click",function(){
			EBUPT.Util.mask.create({clazz:'big_loading',tip:''});
			$("#newWin").css({
				"left" : ($(window).width() - $("#newWin").width()) / 2 + 25 + "px",
				"width" : "1100px",
				"height" : $(window).height()+ "px",
				"overflow" : "scroll"
			});
			$("#newWin").fadeIn("slow",function(){
				$("#btn1","#newWin").unbind().bind("click",function(){
					validateParamAndAjax($("#result").attr("value"), URL + "saveDraft.action","update") ;
				});
				$("#btn2","#newWin").unbind().bind("click",function(){
					$("#newWin").hide();
					EBUPT.Util.mask.destroy();
				});
			});
		});
		//删除权限设置
		$(".delete").unbind().bind("click",function() {
			var id = getSelectedRow();
			if(id === null || id.length <= 0 ){
				EBUPT.Util.Dialog( {type : 5,content: "请先选择报表!",btnType : 3});
				return;
			}
			EBUPT.Util.Dialog( {
				type : 4,
				content: "是否删除此报表?",
				btnType : 1,
			}, function(){
				$.get(URL + "delete.action", { "id": id }, function(data){
					if(data.success){
						EBUPT.Util.Dialog( {type : 3,content: data.msg,btnType : 3});
						jQuery('#eb_grid_report-manage').trigger("reloadGrid");
					}else{
						EBUPT.Util.Dialog( {type : 6,content: data.msg,btnType : 3});
					}
				}, "json");
			});
		});
		//报表权限设置
		$(".authSet").unbind().bind("click",function() {
			var id = getSelectedRow();
			if(id === null || id.length <= 0 ){
				EBUPT.Util.Dialog( {type : 5,content: "请先选择报表!",btnType : 3});
				return;
			}
			var content = EBUPT.Util.render({tpl : EBUPT.tpl.lineInput,data : ""});
			$("#addAuth").die().live("click",function(){
				$(this).parent().prepend(EBUPT.Util.render({tpl : EBUPT.tpl.inputT}));
			});
			var dialog = new EBUPT.Util.Dialog( {
				type: 1,
				title: getSelectedData("tablename")+'权限配置',
				content: content,
				detail: '',
				btnType: 1,
				winSize : 2,
				extra : {top :"150",left : "220",width : "800px",cont_Clazz : 'cont_Clazz'}
			}, function(){
				var nameChn,name,url;
				var auth = [];
				$(".ex_flash_compare").each(function(){
					nameChn = $(this).find("input").eq(0).val();
					name = $(this).find("input").eq(1).val();
					url = $(this).find("input").eq(2).val();
					console.log(nameChn.length);
					if(nameChn.length <= 0){
						$(this).find("input").eq(0).after('<span class="warn">请输入权限名称</span>');
						return;
					}
					if(name.length <= 0){
						$(this).find("input").eq(1).after('<span class="warn">请输入权限标识,以A_开头</span>');
						return;
					}
					if(url.length <= 0){
						$(this).find("input").eq(2).after('<span class="warn">请输入URL</span>');
						return;
					}
					auth.push(nameChn + "," + name + "," + url);
				});
				$.post(URL + "saveAuthority.action", { "id" : id,"auth": auth.join("|")}, function(data){
					if(data.success){
						EBUPT.Util.Dialog( {type : 3,content: data.msg,btnType : 3});
					}else{
						EBUPT.Util.Dialog( {type : 6,content: data.msg,btnType : 3});
					}
				}, "json");

			});
		});

		//查询触发
		$("#btn-query").bind("click", function(){
			jQuery('#eb_grid_report-manage').jqGrid('setGridParam',{
				url: URL + 'page.action',
				postData: {searchKey:$("#searchKey").val(),state:$("#state").val()}
			}).trigger("reloadGrid");
		});
	}
	function queryData(options) {
		options.gridCallback = function() {
			var _ids  = $('#eb_grid_'+options.id).jqGrid('getDataIDs');
			$(_ids).each(function(i, n) {
				var sub = $('#eb_grid_'+options.id).getCell(n,"enablesub");
				var state = $('#eb_grid_'+options.id).getCell(n,"state");

				var _html = '';
				if('0' == sub){
					sub = "否";
				}else if('1' == sub){
					sub = "是";
				}
				if( '0' == state ){//
					sub = "否";
					state = "待发布";
					_html = '<a class="eb-func-icon restart" href="javascript:;" val="1,'+n+'" title="发布"></a>';
				} else {
					if( '1' == state ){
						state = "已上线";
					}
					if( '2' == state ){
						state = "编辑待发布";
					}
					_html = '<a class="eb-func-icon stop" title="下线" href="javascript:;" val="0,'+n+'"></a>';
				}
				_html += '<a class="func update" href="javascript:;" val="'+ n + '"><img title="点击修改" src="${ctx}/styles/security/images/edit.gif"></a>';
				_html += '<a class="func del_a"><img title="点击删除" val="' + n + '" src="${ctx}/styles/security/images/delete.gif"></a>';
				_html += '<a class="func view" href="javascript:;" val='+ n +'><img title="查看报表详情" src="${ctx}/styles/security/images/detail.png"></a>';

				$('#eb_grid_'+options.id).jqGrid('setRowData', n, {enablesub : sub,state : state,func: _html});
			});
			$(".del_a").click(function() {
				var _img = $(this).children().eq(0);
				if(confirm('是否删除此报表')) {
					window.location = URL + "delete.action?&id=" + $(_img).attr("val");
				}
			});
		}
		ebGrid.ajaxJqGrid(options);	// 生成表格
	}
	//校验参数
	function validateParamAndAjax(id,ajaxUrl,opt){
		var sub = $.trim($("input:radio:checked").val());
		var desc = $.trim($("#desc").val());
		var dir = $.trim($("#dir").attr("data"));
		var url = $.trim($("#url").val());
		if(sub.length <= 0 ){
			$("#desc").focus().after('<span class="warn">必填项，请输入</span>');
			return;
		}
		if(desc.length <= 0 ){
			$("#desc").focus().after('<span class="warn">必填项，请输入</span>');
			return;
		}
		if(dir.length <= 0 ){
			$("#dir").after('<span class="warn">请选择报表发布目录</span>');
			return;
		}
		if(url.length <= 0 ){
			$("#url").focus().after('<span class="warn">请输入报表URL</span>');
			return;
		}
		var postData = {id : id , tableName : $("#result").attr("name") ,enableSub : sub, description: desc, publishDir: dir,publishUrl : url };
		$.post(ajaxUrl, postData,function(data){
			if(data.success){
				$("#newWin").hide();
				EBUPT.Util.mask.destroy();
				EBUPT.Util.Dialog( {type : 3,content: data.msg,btnType : 3});
				jQuery('#eb_grid_report-manage').trigger("reloadGrid");

			}else{
				EBUPT.Util.Dialog( {type : 6,content: data.msg,btnType : 3});
			}
		}, "json" );

		return true;
	}
	function getSelectedRow(){
		var id = $('#eb_grid_report-manage').jqGrid('getGridParam',"selrow");
		return id;
	}
	function getSelectedData(columnName){
		var id = $('#eb_grid_report-manage').jqGrid('getGridParam',"selrow");
		var rowData =$('#eb_grid_report-manage').jqGrid("getRowData",id);
		return rowData[columnName];
	}

</script>
<div id="newWin" class="float cf " style="width: 1100px;display: none; position : fixed; z-index: 2002;">
	<div class="float_header">
		<h3>
			<a id="closeIcon" class="close" href="javascript:void(0);" style="display: none;">×</a>
			上传报表配置文件
		</h3>
	</div>
	<div class="float_cont cf">
		<div id="dialog_content"  style="text-align: center;margin: 20px 0 0;">
			<input type="file" name="file" id="myfile" />
			<p>
				<a href="javascript:$('#myfile').uploadifyUpload();">开始上传</a>&nbsp;
				<a href="javascript:$('#myfile').uploadifyClearQueue();">取消上传</a>&nbsp;
			</p>
			<div></div>
			<div id="result" value=""></div>
		</div>
		<div id="fileQueue" style="text-align: center;"></div>
	</div>
	<div class="float_footer">
		<div class="form-action">
			<input id="btn1" class="btn btn-primary" type="button" value="确定">
			<input id="btn2" class="btn" type="button" value="取消">
		</div>
		<div class="form-message">
			<div id="tips" class=" help-inline"></div>
		</div>
	</div>
</div>
</body>
</html>
