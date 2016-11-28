;
if ((typeof EBUPT) == "undefined") {
    var EBUPT = {};
};
EBUPT.Util = EBUPT.Util || {};
EBUPT.tpl = EBUPT.tpl || {};
EBUPT.Dialog = EBUPT.Dialog || {};
EBUPT.Variable = EBUPT.Variable || {};

EBUPT.report = {	
	navis: [
		{name: '报表管理平台'},
		{name: '报表管理', help: '报表管理及发布'}
	]
};
EBUPT.report.manage = {
	id: 'report-manage'
};
EBUPT.page = {
	initList: function(option){
		EBUPT.Util.T(option);
	}
}

/**
 * 页面js框架工具类
 * T: 模板渲染
 * sprintf:输出方法
 * @param {Object} container
 * @param {Object} source
 * @param {Object} data
 */
EBUPT.Util = {
	T : function(option){
		var c = option.container,
			d = option.data || {},
			t = option.tpl,
			callback = option.callback;
		
		if(!c ||!t)return ;
		var defaults = {};
        var data = $.extend(true,defaults,d);
		c.append(template.compile(t)(data));
		callback && callback();
	},
	render : function(option){
		var d = option.data || {},
			t = option.tpl,
			callback = option.callback;
		
		if(!t)return ;
		var defaults = {};
        var data = $.extend(true,defaults,d);
		return template.compile(t)(data);
	},
	sprintf: function(){
        var e, t = this,
            n, r, i = arguments.length;
        if (i < 1) {
            return l
        }
        e = 0;
        while (e < i) {
            t = t.replace(/%s/, "{#" + e+++"#}")
        }
        t.replace("%s", ""), e = 0;
        while ((n = arguments[e]) !== undefined) {
            r = new RegExp("{#" + e + "#}", "g"), t = t.replace(r, n), e++
        }
        return t
	},
	/**
	 * @description 全屏遮罩层管理器
	 * @example EBUPT.Util.mask.create();
	 */
	mask : {
		self : '',
		isIE6 : $.browser.msie && $.browser.version < 7,
		create : function(option) {
			var width = this.width(),height = this.height();
			if(this.self && this.self.parent().length) {
				return;
			}
			$(window).bind('resize.overlay', this.resize);
			return (this.self = (this.self || $('<div><div class="'+option.clazz+'">'+option.tip+'...</div>&nbsp;</div>').css({
				height : '100%',
				left : 0,
				position : 'absolute',
				top : 0,
				width : '100%',
				background : '#000',
				'opacity' : 0.5,
				'z-index' : 2001
			})).appendTo('body').css({
				width : width,
				height : height
			}),
			$('.'+option.clazz).css({
					//'margin' :  '100px 650px',
					'margin-left': 'auto',
    				'margin-right': 'auto',
					'margin-top' :  '70px',
					'height': '34px',
				    'line-height': '34px',
				    'text-align': 'center',
				    'width': '164px'
				}));
			
		},
		destroy : function() {
			if(this.self && !this.self.parent().length) {
				return;
			}
			$([document, window]).unbind('resize.overlay');
			this.self.animate({
				opacity : 'hide'
			}, function() {
				$(this).remove().show();
			});
		},
        resize: function() {
            var _mask = EBUPT.Util.mask;
            _mask.self.css({
                width: 0,height: 0
            }).css({
                width: _mask.width(),height: _mask.height()
            });
        },
		height : function() {
			var scrollHeight, offsetHeight;
			if(this.isIE6) {
				scrollHeight = Math.max(document.documentElement.scrollHeight, document.body.scrollHeight);
				offsetHeight = Math.max(document.documentElement.offsetHeight, document.body.offsetHeight);
				if(scrollHeight < offsetHeight) {
					return $(window).height() + 'px';
				} else {
					return scrollHeight + 'px';
				}
			} else {
				return $(document).height() + 'px';
			}
		},
		width : function() {
			var scrollWidth, offsetWidth;
			if(this.isIE6) {
				scrollWidth = Math.max(document.documentElement.scrollWidth, document.body.scrollWidth);
				offsetWidth = Math.max(document.documentElement.offsetWidth, document.body.offsetWidth);
				if(scrollWidth < offsetWidth) {
					return $(window).width() + 'px';
				} else {
					return scrollWidth + 'px';
				}
			} else {
				return $(document).width() + 'px';
			}
		}
	},
	/**
 * @description 弹出窗口
 * @param {Object} json 配置数组
 * @param {Object} callback 回调函数
 * @expample  var dialog = new EBUPT.Util.Dialog( {
				 type: 1,
				 title: '选择渠道',
				 content: 'aaaaaaaaaaa',
				 detail: '',
				 btnType: 1,
				 extra: {top: 250},
				 winSize : 2
				 }, function(){});
 */
  Dialog : function(json, callback) {
	var defaults = {
		type : 1, //对话框类型，1：通用类型，接收html内容,	2：控件，
		//			3：纯文本 -- 提示信息，绿色，	4：纯文本 -- 警告信息，橙色

		//			5：纯文本 -- 警告信息，红色		6：纯文本 -- 错误信息，红色
		showHeader : true,
		title : '温馨提示',
		hideCloseIcon : false, //是否显示右上角的关闭图标
		content : '',
		detail : '',
		tips : '',
		btnType : 1, //按钮类型，1：确定，取消  2：是，否  3，确定  false，4：继续，false，5：不显示按钮，6，自定义按钮
		buttons : {
			text1 : '',
			text2 : ''
		},
		winSize : 1, //窗体大小，1：小窗体，样式为min，2：大窗体，样式为 mid ,3 超小 样式为mirco
		extra : {//扩展信息，如控制对话框宽度，显示层次，位置等
			top : '',
			left : '',
			width : '',
			zIndex : 2002,
			heatmap : '', //是否是热区图页面，兼容旧版
			noPrompt : '', //是否显示“下次不再显示”复选框，待实现
			autoMask : true,
			autoDestory : true,
			autoClose : true,
			cont_Clazz : ''
		}
	}; !json.extra && (json.extra = defaults.extra);
	var opts = $.extend(true, defaults, json);
	var self = this;
	self.dialogId = '';

	var create = function() {

		var extra = opts.extra;
		var zIndex = extra['zIndex'];

		while($('#fwin_dialog_fs_100' + zIndex)[0]) {
			zIndex++;
		}
		var message_id = 'fs_100' + zIndex;
		var dialogId = 'fwin_dialog_' + message_id;
		var contentId = dialogId + '_content';
		var closeIconId = dialogId + '_closeIcon';
		var tipsId = dialogId + '_tips';
		var btnId1 = dialogId + '_btn1', btnId2 = dialogId + '_btn2', btnContainer1 = dialogId + '_btnCtn1';
		self.dialogId = dialogId;
		self.btnId1 = btnId1;
		self.btnContainer1 = btnContainer1;
		self.contentId = contentId;
		self.callback = callback;

		var dialogPosition = ($.browser.msie && $.browser.version < 7) ? 'absolute' : 'fixed';
		extra['heatmap'] && ( dialogPosition = 'absolute');
		//热区图
		var btnText1, btnText2, tips = opts.tips, btnType = opts.btnType;
		var typeList = {
			1 : ['确定', '取消'],
			2 : ['是', '否'],
			3 : ['确定', ''],
			4 : ['继续', ''],
			5 : ['', '']
		};
		if(btnType != 6) {
			btnType = typeList[btnType] ? btnType : 1;
			//默认取第一个
			btnText1 = typeList[btnType][0];
			btnText2 = typeList[btnType][1];
		} else {
			btnText1 = opts.buttons.text1;
			btnText2 = opts.buttons.text2;
		}

		//footer
		var footerHtml = '';
		if(tips || btnText1 || btnText2) {
			footerHtml = '	<div class="float_footer"> ';
			if(btnText1 || btnText2) {
				footerHtml += '		<div class="form-action"> ';
				btnText1 && function() {
					var rawHtml = '<input type="button" id="' + btnId1 + '"  value="' + btnText1 + '"  class="btn  btn-primary"/> '; 
                    footerHtml += rawHtml;
				}();
				btnText2 && (footerHtml += '			<input type="button" id="' + btnId2 + '" value="' + btnText2 + '"  class="btn"/> ');
				footerHtml += '</div>';
			}
            footerHtml += '			<div class="form-message"> ' + '				<div id="' + tipsId + '" class=" help-inline">' + tips + '</div> ' + '			</div> ';
			footerHtml += '</div>';
		}

		var winSizeClass = {1: 'min', 2: 'mid',3 : 'mirco'}[opts.winSize] || 'min';
		var contentClass = {1: '', 2: 'form-horizontal', 3: 'confirm success', 4: 'confirm attent', 5: 'confirm warn', 6: 'confirm error'}[opts.type] || '';
		var headerHtml = '';
		var cont_Clazz = extra['cont_Clazz'];
		if(opts.showHeader){
			headerHtml =  '<div class="float_header"> ' 
            + '			<h3><a id="' + closeIconId + '" href="javascript:void(0);" class="close">&times;</a>' + opts.title + '</h3>' 
            + '		</div>' ;
		}
		var strHtml = 
            ' <div id="' + dialogId + '" style="position: ' + dialogPosition + '; z-index: ' + zIndex + '" class="float cf ' + winSizeClass + '">' 
            + headerHtml 
            + '		<div class="float_cont cf ' + cont_Clazz + '"> ' 
            + '			<div class="' + contentClass + '" id="' + contentId + '"> ' + '			</div> ' 
            + '		</div> ' 
            + footerHtml 
            + '	</div> ';

		if(!$('#' + dialogId)[0]) {
			$(strHtml).appendTo("body");
		}

		//填充内容
		var content = opts.content;
		var cssInfo = {3: 'success', 4:'attent', 5:'warn', 6:'error'}[opts.type] || '';
		cssInfo && ( content = 
              ' 	<i class="icon-confirm"></i> ' 
            + '	    <div class="confirm-cont"> ' 
            + '			<h4>' + opts.content + '</h4> ' 
            + '			<p>' + opts.detail + '</p> ' 
            + '		</div> ');

		$('#' + contentId).html(content);

		$("#" + dialogId).show();

		//处理对话框宽度
		var dialogWidth = extra['width'] ? parseInt(extra['width']) + 'px' : '';
		$('#' + contentId).css({
			"width" : dialogWidth
		});
		$('#' + dialogId).css({
			"width" : dialogWidth
		});
		//处理对话框位置
		dialogLeft = extra['left'] || ($(window).width() - $('#' + dialogId).width()) / 2;
		dialogTop = extra['top'] || ($(window).height() - $('#' + dialogId).height()) / 2 - 100;
		$("#" + dialogId).css({
			"top" : dialogTop + "px",
			"left" : dialogLeft + "px"
		});

		//点击回调函数
		$('#' + btnId1).click(function() {
			buttonClick('btn1');
		});
		$('#' + btnId2).click(function() {
			buttonClick('btn2');
		});
		$('#' + closeIconId).click(function() {
			buttonClick('btnClose');
		});

		opts.hideCloseIcon && $('#' + closeIconId).css('display', 'none');

		var noPrompt = (extra && typeof (extra['noPrompt']) != 'undefined') ? extra['noPrompt'] : false;
		if(noPrompt) {
			$('#' + 'promptOff').html('<input type="checkbox" id="noDataPromptOff" name="noDataPromptOff" value="1" style="position:relative;top:2px;"/> 不再提醒 ');
		}
		
		opts.extra.autoMask && EBUPT.Util.mask.create({clazz:'big_loading',tip:''});
		//自动遮盖

		// 解决IE6select控件bug
		var hidIframeId = "frm_100_" + dialogId;
		//如果已经存在，那么删除
		if($("#" + hidIframeId)) {
			$("#" + hidIframeId).remove();
		}
		hidIframe = "<iframe id=\"" + hidIframeId + "\"></iframe>";
		$(hidIframe).appendTo("body");
		zIndex = parseInt(zIndex);
		zIndex--;
		$("#" + hidIframeId).css({
			"width" : dialogWidth,
			"height" : $("#" + dialogId).height(),
			"position" : dialogPosition,
			"top" : $("#" + dialogId).css("top"),
			"left" : $("#" + dialogId).css("left"),
			"z-index" : zIndex,
			"scrolling" : "no",
			"border" : "0"
		});
	}, buttonClick = function(btnName) {
		//仅支持第一个按钮的点击调用回调函数
		(btnName == 'btn1' && self.callback) ? function() {
			self.callback();
			if(opts.extra.autoClose) {
				self.hideWindows();
			}
		}() : self.hideWindows();
	};

	this.hideWindows = function() {
		var dialogId = self.dialogId;
		$("#" + dialogId).hide();
		$("#" + dialogId).remove();
		$("#frm_100_" + dialogId).remove();
		//解决IE浏览器下a标签不向上冒泡的问题
		if($("div[id^='calendar_']")) {
			$("div[id^='calendar_']").css('display', 'none');
		}
		
		opts.extra.autoMask && opts.extra.autoDestory && EBUPT.Util.mask.destroy();
		//从集合中清除指定对话框
		return false;
	};
	
	this.changeContent = function(content, hdl, extra){
		$('#'+self.contentId).html(content);
		self.callback = hdl;
		extra.btnText1 && $('#'+self.btnId1).val(extra.btnText1);	
		extra.initHdl &&  extra.initHdl();	
	};

	this.closeWindows = this.hideWindows; 
    this.showTips = function(msg) {
		var tipsId = self.dialogId + '_tips';
        if (msg.length > 50){
            msg = '服务器繁忙，请刷新重试';
        }

		$("#" + tipsId).html(msg);
	}, this.clearTips = function() {
		var tipsId = self.dialogId + '_tips';
		$("#" + tipsId).html('');
	}
	create();
	return this;
},
	uploadify : function(){
		EBUPT.Util.Dialog({
			 type: 1,
			 title: '上传报表配置文件',
			 content: EBUPT.Util.render({tpl : $("#newWin").html(),callback : EBUPT.Util.upload()}),
			 detail: '',
			 btnType: 1,
			 hideCloseIcon : true,
			 winSize : 2,
			 extra : {autoClose:false},
			 }, function(){
	//					 window.location.href = URL + "offLine.action?id=" + record.split(",")[1];
		});
		$("#startUpload").die().live("click",function(){
			console.log(1);
			$("#file").uploadifyUpload();
		});
		$("#cancelUpload").die().live("click",function(){
			$("#file").uploadifyClearQueue();
		});
	},
	SuperDialog : function(json, callback) {
	var defaults = {
		type : 1, //对话框类型，1：通用类型，接收html内容,	2：控件，
		//			3：纯文本 -- 提示信息，绿色，	4：纯文本 -- 警告信息，橙色

		//			5：纯文本 -- 警告信息，红色		6：纯文本 -- 错误信息，红色
		showHeader : true,
		title : '温馨提示',
		hideCloseIcon : false, //是否显示右上角的关闭图标
		content : '',
		detail : '',
		tips : '',
		btnType : 1, //按钮类型，1：确定，取消  2：是，否  3，确定  false，4：继续，false，5：不显示按钮，6，自定义按钮
		buttons : {
			text1 : '',
			text2 : ''
		},
		winSize : 1, //窗体大小，1：小窗体，样式为min，2：大窗体，样式为 mid ,3 超小 样式为mirco
		extra : {//扩展信息，如控制对话框宽度，显示层次，位置等
			top : '',
			left : '',
			width : '',
			zIndex : 2002,
			heatmap : '', //是否是热区图页面，兼容旧版
			noPrompt : '', //是否显示“下次不再显示”复选框，待实现
			autoMask : true,
			autoDestory : true,
			autoClose : true
		}
	}; !json.extra && (json.extra = defaults.extra);
	var opts = $.extend(true, defaults, json);
	var self = this;
	self.dialogId = '';

	var create = function() {

		var extra = opts.extra;
		var zIndex = extra['zIndex'];

		while($('#fwin_dialog_fs_100' + zIndex)[0]) {
			zIndex++;
		}
		var message_id = 'fs_100' + zIndex;
		var dialogId = 'fwin_dialog_' + message_id;
		var contentId = dialogId + '_content';
		var closeIconId = dialogId + '_closeIcon';
		var tipsId = dialogId + '_tips';
		var btnId1 = dialogId + '_btn1', btnId2 = dialogId + '_btn2', btnContainer1 = dialogId + '_btnCtn1';
		self.dialogId = dialogId;
		self.btnId1 = btnId1;
		self.btnContainer1 = btnContainer1;
		self.contentId = contentId;
		self.callback = callback;

		var dialogPosition = ($.browser.msie && $.browser.version < 7) ? 'absolute' : 'fixed';
		extra['heatmap'] && ( dialogPosition = 'absolute');
		//热区图
		var btnText1, btnText2, tips = opts.tips, btnType = opts.btnType;
		var typeList = {
			1 : ['确定', '取消'],
			2 : ['是', '否'],
			3 : ['确定', ''],
			4 : ['继续', ''],
			5 : ['', '']
		};
		if(btnType != 6) {
			btnType = typeList[btnType] ? btnType : 1;
			//默认取第一个
			btnText1 = typeList[btnType][0];
			btnText2 = typeList[btnType][1];
		} else {
			btnText1 = opts.buttons.text1;
			btnText2 = opts.buttons.text2;
		}

		//footer
		var footerHtml = '';
		if(tips || btnText1 || btnText2) {
			footerHtml = '	<div class="float_footer"> ';
			if(btnText1 || btnText2) {
				footerHtml += '		<div class="form-action"> ';
				btnText1 && function() {
					var rawHtml = '<input type="button" id="' + btnId1 + '"  value="' + btnText1 + '"  class="btn  btn-primary"/> '; 
                    footerHtml += rawHtml;
				}();
				btnText2 && (footerHtml += '			<input type="button" id="' + btnId2 + '" value="' + btnText2 + '"  class="btn"/> ');
				footerHtml += '</div>';
			}
            footerHtml += '			<div class="form-message"> ' + '				<div id="' + tipsId + '" class=" help-inline">' + tips + '</div> ' + '			</div> ';
			footerHtml += '</div>';
		}

		var winSizeClass = {1: 'min', 2: 'mid',3 : 'mirco'}[opts.winSize] || 'min';
		var contentClass = {1: '', 2: 'form-horizontal', 3: 'confirm success', 4: 'confirm attent', 5: 'confirm warn', 6: 'confirm error'}[opts.type] || '';
		var headerHtml = '';
		if(opts.showHeader){
			headerHtml =  '<div class="float_header"> ' 
            + '			<h3><a id="' + closeIconId + '" href="javascript:void(0);" class="close">&times;</a>' + opts.title + '</h3>' 
            + '		</div>' ;
		}
		var strHtml = 
            ' <div id="' + dialogId + '" style="position: ' + dialogPosition + '; z-index: ' + zIndex + '" class="float cf ' + winSizeClass + '">' 
            + headerHtml 
            + '		<div class="float_cont cf"> ' 
            + '			<div class="' + contentClass + '" id="' + contentId + '"> ' + '			</div> ' 
            + '		</div> ' 
            + footerHtml 
            + '	</div> ';

		if(!$('#' + dialogId)[0]) {
			$(strHtml).appendTo("body");
		}

		//填充内容
		var content = opts.content;
		var cssInfo = {3: 'success', 4:'attent', 5:'warn', 6:'error'}[opts.type] || '';
		cssInfo && ( content = 
              ' 	<i class="icon-confirm"></i> ' 
            + '	    <div class="confirm-cont"> ' 
            + '			<h4>' + opts.content + '</h4> ' 
            + '			<p>' + opts.detail + '</p> ' 
            + '		</div> ');

		$('#' + contentId).html(content);

		$("#" + dialogId).show();

		//处理对话框宽度
		var dialogWidth = extra['width'] ? parseInt(extra['width']) + 'px' : '';
		$('#' + contentId).css({
			"width" : dialogWidth
		});
		$('#' + dialogId).css({
			"width" : dialogWidth
		});
		//处理对话框位置
		dialogLeft = extra['left'] || ($(window).width() - $('#' + dialogId).width()) / 2 -200;
		dialogTop = extra['top'] || ($(window).height() - $('#' + dialogId).height()) / 2;
		$("#" + dialogId).css({
			"left" : dialogLeft + "px",
			"width" : "1100px",
			"height" : $(window).height()+ "px",
			"overflow" : "scroll"
		});

		//点击回调函数
		$('#' + btnId1).click(function() {
			buttonClick('btn1');
		});
		$('#' + btnId2).click(function() {
			buttonClick('btn2');
		});
		$('#' + closeIconId).click(function() {
			buttonClick('btnClose');
		});

		opts.hideCloseIcon && $('#' + closeIconId).css('display', 'none');

		var noPrompt = (extra && typeof (extra['noPrompt']) != 'undefined') ? extra['noPrompt'] : false;
		if(noPrompt) {
			$('#' + 'promptOff').html('<input type="checkbox" id="noDataPromptOff" name="noDataPromptOff" value="1" style="position:relative;top:2px;"/> 不再提醒 ');
		}
		
		opts.extra.autoMask && EBUPT.Util.mask.create({clazz:'big_loading',tip:''});
		//自动遮盖

		// 解决IE6select控件bug
		var hidIframeId = "frm_100_" + dialogId;
		//如果已经存在，那么删除
		if($("#" + hidIframeId)) {
			$("#" + hidIframeId).remove();
		}
		hidIframe = "<iframe id=\"" + hidIframeId + "\"></iframe>";
		$(hidIframe).appendTo("body");
		zIndex = parseInt(zIndex);
		zIndex--;
		$("#" + hidIframeId).css({
			"width" : dialogWidth,
			"height" : $("#" + dialogId).height(),
			"position" : dialogPosition,
			"top" : $("#" + dialogId).css("top"),
			"left" : $("#" + dialogId).css("left"),
			"z-index" : zIndex,
			"scrolling" : "no",
			"border" : "0"
		});
	}, buttonClick = function(btnName) {
		//仅支持第一个按钮的点击调用回调函数
		(btnName == 'btn1' && self.callback) ? function() {
			self.callback();
			if(opts.extra.autoClose) {
				self.hideWindows();
			}
		}() : self.hideWindows();
	};

	this.hideWindows = function() {
		var dialogId = self.dialogId;
		$("#" + dialogId).hide();
		$("#" + dialogId).remove();
		$("#frm_100_" + dialogId).remove();
		//解决IE浏览器下a标签不向上冒泡的问题
		if($("div[id^='calendar_']")) {
			$("div[id^='calendar_']").css('display', 'none');
		}
		
		opts.extra.autoMask && opts.extra.autoDestory && EBUPT.Util.mask.destroy();
		//从集合中清除指定对话框
		return false;
	};
	
	this.changeContent = function(content, hdl, extra){
		$('#'+self.contentId).html(content);
		self.callback = hdl;
		extra.btnText1 && $('#'+self.btnId1).val(extra.btnText1);	
		extra.initHdl &&  extra.initHdl();	
	};

	this.closeWindows = this.hideWindows; 
    this.showTips = function(msg) {
		var tipsId = self.dialogId + '_tips';
        if (msg.length > 50){
            msg = '服务器繁忙，请刷新重试';
        }

		$("#" + tipsId).html(msg);
	}, this.clearTips = function() {
		var tipsId = self.dialogId + '_tips';
		$("#" + tipsId).html('');
	}
	create();
	return this;
},
	uploadify : function(){
		EBUPT.Util.Dialog({
			 type: 1,
			 title: '上传报表配置文件',
			 content: EBUPT.Util.render({tpl : $("#newWin").html(),callback : EBUPT.Util.upload()}),
			 detail: '',
			 btnType: 1,
			 hideCloseIcon : true,
			 winSize : 2,
			 extra : {autoClose:false},
			 }, function(){
	//					 window.location.href = URL + "offLine.action?id=" + record.split(",")[1];
		});
		$("#startUpload").die().live("click",function(){
			console.log(1);
			$("#file").uploadifyUpload();
		});
		$("#cancelUpload").die().live("click",function(){
			$("#file").uploadifyClearQueue();
		});
	},
	upload : function(){
		$("#myfile").uploadify({
			'uploader'       : EBUPT.Variable.ctx + 'plugins/uploadify/uploadify.swf', //是组件自带的flash，用于打开选取本地文件的按钮
			'script'         : URL + 'uploadFile.action',//处理上传的路径，这里使用Struts2是XXX.action
			'cancelImg'      : EBUPT.Variable.ctx  + 'plugins/uploadify/cancel.png',//取消上传文件的按钮图片，就是个叉叉
			'folder'         : 'uploads',//上传文件的目录
			'fileDataName'   : 'file',//和input的name属性值保持一致就好，Struts2就能处理了
			'queueID'        : 'fileQueue',
			'auto'           : false,//是否选取文件后自动上传
			'multi'          : true,//是否支持多文件上传
			'simUploadLimit' : 1,//每次最大上传文件数
			'buttonText'     : '请选择要上传的文件',//按钮上的文字
			'displayData'    : 'percentage',//有speed和percentage两种，一个显示速度，一个显示完成百分比
//			'fileDesc'       : '支持格式:.xml', //如果配置了以下的'fileExt'属性，那么这个属性是必须的
//			'fileExt'        : '.xml',//允许的格式
			'onComplete'     : function (event, queueID, fileObj, response, data){
				setInterval(function(){$("#result").empty();},2000);//两秒后删除显示的上传成功结果
				var rslt = JSON.parse(response);//显示上传成功结果
				$("#result").html(rslt.desc);
				//$("#dialog_content").empty();
				if(rslt.success){
					$("#result").attr("value",rslt.id);
					$("#result").attr("name",rslt.name);
					$("#dialog_content").append(EBUPT.Util.render({tpl : EBUPT.tpl.contentView,data : rslt}));
					$("#dialog_content").append(EBUPT.Util.render({
							tpl : EBUPT.tpl.formT,
							data : {data : ""
					}
					}));
					$('#change_dir').bind("click",function(){
							var dirDialog = EBUPT.Util.Dialog( {title: '报表发布目录树',type : 1,content: '',btnType : 1,hideCloseIcon : true,extra : {zIndex: 9999}});
							//加载tree
							$("#" + dirDialog.contentId).append('<ul id="orgTree" class="ztree"></ul>');
							EBUPT.Variable.treeSetting.async.url = EBUPT.Variable.ctx + "data/report/report_dir.json";
							$.fn.zTree.init($("#orgTree"), EBUPT.Variable.treeSetting);
					});
//					var dialog = new EBUPT.Util.Dialog( {
//						 type: 1,
//						 title: '报表发布配置',
//						 content: formT,
//						 detail: '',
//						 btnType: 6,
//						 buttons : {
//							text1 : '完成',
//							text2 : '取消'
//						},
//						 hideCloseIcon : true,
//						 winSize : 2,
//						 extra : {autoClose:false},
//						 }, function(){
//							 validateParamAndAjax(record,URL + "saveDraft.action",null) ;
//							 dialog.closeWindows();
//							 //发布确认成功后
//							 //EBUPT.Util.Dialog( {type : 3,content: "报表发布成功",btnType : 3});
//						 });
				}else{
					
				}
			}
		});
	},
	tips : {
			
	       show : function(e,t){
				var i = {
		            errMsg: "系统发生错误，请稍后重试",
		            sucMsg: "操作成功",
		            delay: 3000
		        };
				$(".JS_TIPS").remove();
		        var tips = $(template.compile('<div class="JS_TIPS page_tips <%=type%>" id="rTips_' + (new Date).getTime() + '"><div class="inner"><%=msg%></div></div>')({
						type: e || "error",
						msg: t
					})).appendTo("body").fadeIn();
		     	tips.delay(i.delay).fadeOut();
			     
			        
//			    r.err = function (e, t) {
//			        o("error", e || i.errMsg, t);
//			    }, r.suc = function (e, t) {
//			        o("success", e || i.sucMsg, t);
//			    };
	       }
	
	   
	}
}
/*
 对话框常量
 对话框类型，1：通用类型，接收html内容,	2：控件，
 3：纯文本 -- 提示信息，绿色，	4：纯文本 -- 警告信息，橙色
 5：纯文本 -- 警告信息，红色		6：纯文本 -- 错误信息，红色
 */
EBUPT.Dialog.DIALOG_TYPE = {
	COMMON : 1,
	CONTROL : 2,
	TEXT_INFO : 3,
	TEXT_WARN : 4,
	TEXT_WARN_RED : 5,
	TEXT_ERROR : 6
};
/*
 对话框常量
 按钮类型，1：确定，取消  2：是，否  3，确定  false，4：继续，false，5：不显示按钮，6，自定义按钮
 */
EBUPT.Dialog.BUTTON_TYPE = {
	OK_CANCEL : 1,
	YES_NO : 2,
	OK : 3,
	CONTINUE : 4,
	NONE : 5,
	CUSTOMIZE : 6
};
/*
 对话框常量
 对话框尺寸，1：小窗体，2：大窗体
 */
EBUPT.Dialog.WIN_SIZE = {
	MIN : 1, //小窗体
	MID : 2
};
EBUPT.tpl = {
	reportList : '<ul id="listContainer" class="message_list">'+
		'{if list.length <= 0}' +
	'<p class="empty_tips">暂无报表信息</p>\n{else}' +
	'{each list as item}' +
		'<li class="message_item {if item.has_reply}replyed{/if}" id="msgListItem{item.id}" data-id="{item.id}" data-name="{item.name}">' +
	'{if (item.fakeid != uin)}' +
		'<div class="message_opr"> <a class="icon20_common reply_gray2 rapid" title="快捷回复" data-tofakeid="{item.fakeid}" data-id="{item.id}" href="javascript:;">快捷回复</a>' +
		'<a href="javascript:;" data-id="{item.id}" data-name="{item.nick_name}" data-tofakeid="{item.fakeid}" class="icon20_common reply_gray js_reply" title="查看回复">查看回复</a>' +
		'<a href="javascript:;" class="js_star icon16_common {if (item.is_starred_msg != 1)}star_gray{else}star_orange{/if}" action="{action}" idx="{item.id}" starred="{item.is_starred_msg}" title="{if (item.is_starred_msg != 1) }收藏消息{else}取消收藏{/if}">取消收藏</a>\n    ' +
	'{if (item.type!= 1 && item.type != 10 && item.type != 4) }\n           ' +
		'<a href="/cmrwx/web/img/{fakename}" class="icon18_common download_gray" target="_blank" idx="{item.id}" title="下载">下载</a>\n            {/if}\n            ' +
	'<div class="message_info">' +
		'<div class="message_status">{if item.msg_status == 2}<em class="tips"><i>●</i>客服回复</em>{/if}<em class="tips"><i>●</i>已回复</em></div>\n            ' +
		'<div class="message_time">{timeFormat item}</div>\n            ' +
	'<div class="user_info">' +
	'{if (item.fakeid != uin)}' +
		'<a href="{id2singleURL item}" target="_blank" data-fakeid="{item.fakeid}" class="remark_name">{if item.remark_name}{=item.remark_name}{else}{=item.nick_name.emoji()}{/if}</a>\n                ' +
	'{else}' +
		'<span data-fakeid="{item.fakeid}" class="remark_name">{if item.remark_name}{=item.remark_name}{else}{=item.nick_name.emoji()}{/if}</span>{/if}' +
	' <span class="nickname" data-fakeid="{item.fakeid}">{if item.remark_name}(<strong>{=item.nick_name.emoji()}</strong>){/if}</span>{if (item.fakeid != uin)} {/if} ' +
	' {if (item.fakeid != uin)}<a target="_blank" href="{id2singleURL item}" class="avatar" data-fakeid="{item.fakeid}"><img src="{item.headurl}" data-fakeid="{item.fakeid}"></a>' +
	'{else}<span class="avatar" data-fakeid="{item.fakeid}"><img src="{item.headurl}" data-fakeid="{item.fakeid}"></span>{/if}</div></div>' +
	'<div class="message_content {if item.type == 1}text{/if}"><div id="wxMsg{item.id}" data-id="{item.id}" class="wxMsg">{mediaInit item}</div></div></div></li>'+
	'</ul>',
	contentView : '<div class="content">'+
					'<h4 class="tags_title"><%=reportData.name%></h4>'+
					'<div class="tags_select">'+
						'<table>'+
							'<tbody>'+
							    '<% if(reportData.type =="0"){%>'+
							    	'<% if(reportData.drill.length >0){%>'+
							    		'<% for(var i = 0;i < reportData.drill.length; i ++){%>'+
							    				'<tr>'+
													'<td style="border:1px solid #d8d8d8;">'+
														'<h5>'+
														'<label>'+
																'维度组合：' +
																'<% for(var j = 0;j < (reportData.drill)[i].dimension.length; j ++){%>'+
																	'<%=((reportData.drill[i]).dimension)[j].nameChn%> '+
																	 '<% if(j != (reportData.drill)[i].dimension.length - 1){%>'+
																	 	'+'+
																	 '<%}%>'+
																'<%}%>'+
														'</label>'+
														'</h5>'+
														'<div class="inputs measure">'+
															'<% for(var j = 0;j < (reportData.drill)[i].measure.length; j ++){%>'+
																'<label>'+
																	'<span class="el_pay"><%=((reportData.drill[i]).measure)[j].showName%></span>'+
																'</label>'+
															'<% } %>'+
														'</div>'+
													'</td>'+
												'</tr>'+
							    		'<% } %>'+
							    	'<% } %>'+
								'<% } else%>'+
								'<% if(reportData.type =="1"){%>'+
										'<tr>'+
											'<td style="border:1px solid #d8d8d8;">'+
											'<h5>'+
											'<label>'+
												'指标'+
											'</label>'+
											'</h5>'+
											'<div class="inputs measure">'+
												'<% for(var i = 0;i < reportData.base.measure.length; i ++){%>'+
													'<label>'+
														'<span class="el_pay"><%=(reportData.base.measure)[i].showName%></span>'+
													'</label>'+
												'<% } %>'+
											'</div>'+
										'</td>'+
										'</tr>'+
										'<tr>'+
											'<td style="border:1px solid #d8d8d8;">'+
											'<h5>'+
											'<label>'+
												'维度'+
											'</label>'+
											'</h5>'+
											'<div class="inputs">'+
												'<% for(var i = 0;i < reportData.base.dimension.length; i ++){%>'+
													'<label>'+
														'<span class="el_pay"><%=(reportData.base.dimension)[i].nameChn%></span>'+
													'</label>'+
												'<% } %>'+
											'</div>'+
											
										'</td>'+
										'</tr>'+
									
								'<% } %>'+
							 	'<% if(reportData.auth){%>'+	
									'<tr>'+
										'<td style="border:1px solid #d8d8d8;">'+
											'<h5>'+
											'<label>'+
												'报表权限项'+
											'</label>'+
											'</h5>'+
											'<div class="inputs measure">'+
												'<% for(var i = 0;i < reportData.auth.length; i ++){%>'+
													'<label>'+
														'<span class="el_pay"><%=reportData.auth[i][0]%></span>'+
													'</label>'+
												'<%}%>'+	
											'</div>'+
										'</td>'+
									'</tr>'+
								'<%}%>'+	
							'</tbody>'+
						'</table>'+
					'</div>'+
				'</div>',
		formT :
					'<div class="frm_cont">'+
						'<ul>'+
							'<li>'+
								'<span class="frm_info">'+
								'<em>*</em>是否可订阅：</span>'+
									'<input name="subcribe" type="radio" style="min-width:30px;" value="1" <%if(data.subsribe == 1){%>checked<%}%>>是</input>'+
									'<input name="subcribe" type="radio" style="min-width:30px;"value="0" <%if(data.subsribe == 0){%>checked<%}%>>否</input>'+
							'</li>'+
							'<li>'+
								'<span class="frm_info">'+
								'<em>*</em>报表描述：</span>'+
								'<textarea id="desc" rows="1" name="desc" style="width:240px;" ><%if(data.desc){%><%=data.desc%><%}%></textarea> '+
							'</li>'+
							'<li>'+
								'<span class="frm_info">'+
								'<em>*</em>报表发布目录：</span>'+
								'<input id="dir" class="ipt_text" type="text" name="dir" data="<%if(data.dir){%><%=data.dir%><%}%>" value="<%if(data.dirname){%><%=data.dirname%><%}%>">'+
								'&nbsp&nbsp&nbsp&nbsp<a href="javascript:;" id="change_dir">更改目录</a>'+
							'</li>'+
							'<li>'+
								'<span class="frm_info">'+
								'<em>*</em>报表URL：</span>'+
								'<input id="url" class="ipt_text" type="text" name="url" value="<%if(data.url){%><%=data.url%><%}%>">'+
							'</li>'+
						'</ul>'+
					'</div>',
		/*uploadify : '<div id="fileQueue"></div>'+
						'<div class="jx-select-file">'+
						'<span class="jx-select-file-btn">'+
							'<input id="upload" class="file-select-proxy" type="file" multiple="" name="file" title="请选择要上传的文件">'+
						'</span>'+
					    '</div>'+
						'<p class="line">'+
							'<a href="javascript:;" id="startUpload">开始上传</a>&nbsp;'+
							'<a href="javascript:;"id="cancelUpload">取消上传</a>&nbsp;'+
						'</p>'+
						'<div></div>'+
					'<div id="result"></div>'+
				'</div>',*/
		
		lineInput : '<div style="text-align: center;" >'+
					'<div id="authValue" class="ex_flash_compare clearfix" >'+
						'<input class="ex_input" action-type="hotkey" placeholder="输入权限名称" >'+
						'<input class="ex_input" action-type="hotkey" placeholder="输入权限标识" >'+
						'<input class="ex_input" action-type="hotkey" placeholder="输入权限URL" style="width:350px;">'+
					'</div>' + 
					'<a href="javascript:;" id="addAuth"style="margin:10px;">添加权限项</a>&nbsp;'+
					'</div>' ,

		inputR :   '<div style="text-align: center;">' +
							'<input id="selectReport" placeholder="请选择报表"/>' +
							'<input id = "generateReport" type = "button" value = "生成配置文件"/>' +
							'<lable id = "generateMessage" style = "color: red;"></lable>' +
							'<lable id = "reportId" style = "display:none;" ></lable>'+
						'</div>' +
						'<div>' +
							'<lable id = "newReport"></lable>' +
						'</div>'+
						'<div style="text-align: center;">'+
							'<label>报表描述</label>'+
							'<input id = "reportDescription" type = "text"/>'+
						'</div>'+
						'<div style="text-align: center;">'+
							'<label>报表发布目录</label>'+
							'<input id = "dir" type = "text" />' +
                            '<a href="javascript:;" id="choose_dir">选择目录</a>'+
						'</div>'+
						'<div style="text-align: center;">'+
							'<label id = "submitMessage" style = "color:red;"></label>'+
						'</div>',

		inputT : '<div id="authValue" class="ex_flash_compare clearfix" >'+
						'<input class="ex_input" action-type="hotkey" placeholder="输入权限名称" >'+
						'<input class="ex_input" action-type="hotkey" placeholder="输入权限标识" >'+
						'<input class="ex_input" action-type="hotkey" placeholder="输入权限URL" style="width:350px;">'+
					'</div>' ,
		inputDescT : '<div style="text-align: center;" >'+
					 '<div id="queryConditionDesc" class="ex_flash_compare clearfix" >'+
					    '<lable class="ex_lable">查询模板备注：</lable>' +
						'<textarea id="query_desc"  placeholder="输入查询模板备注信息" ></textarea>' +
					'</div>',
		toolbarT : '<div class="bar-group">'+
				'<div class="item">' +
					'<span id="generateXml" class="y-btn y-btn-gray new show" data-cn="generate">'+
						'<i class="icon icon-plus"></i>'+
						'<span class="labels">配置</span>'+
					'</span>'+
					/*'<span id="tbNew" class="y-btn y-btn-gray new show" data-cn="new">'+
						'<i class="icon icon-plus"></i>'+
						'<span class="labels">新建</span>'+
					'</span>'+*/
					'<span id="tbEdit" class="y-btn y-btn-gray edit show" data-cn="edit">'+
						'<i class="icon icon-edit"></i>'+
						'<span class="labels">编辑</span>'+
					'</span>'+
					'<span id="tbLook" class="y-btn y-btn-gray look show" data-cn="look">'+
						'<i class="icon  icon-eye-open"></i>'+
						'<span class="labels">查看</span>'+
					'</span>'+
					'<span id="tbPublish" class="y-btn y-btn-gray publish show" data-cn="publish">'+
						'<i class="icon icon-share-alt"></i>'+
						'<span class="labels">发布</span>'+
					'</span>'+
					'<span id="tbAuthSet" class="y-btn y-btn-gray authSet show" data-cn="authSet">'+
						'<i class="icon icon-hand-up"></i>'+
						'<span class="labels">权限设置</span>'+
					'</span>'+
					'<span id="tbDelete" class="y-btn y-btn-gray delete show" data-cn="delete">'+
						'<i class="icon icon-trash"></i>'+
						'<span class="labels">删除</span>'+
					'</span>'+
				'</div>'+
			'</div>'
					
		
}

EBUPT.Variable = {
	root : EBUPT.Variable.ctx,
	/**
	 * 报表发布目录树参数
	 */
	treeSetting : {
		check: {
				enable: true,
				chkStyle: "radio",
				radioType: "all"
		},
		async: {
			enable: true,
			url:  EBUPT.Variable.ctx + "data/report/report_dir.json",
			dataFilter: filter	
		},
		view: {
//			addHoverDom: addHoverDom,
//			removeHoverDom: removeHoverDom,
			selectedMulti: false
		},
		edit: {
//			enable: true,
//			editNameSelectAll: true,
//			showRemoveBtn: showRemoveBtn,
//			showRenameBtn: showRenameBtn
		},
		data: {
			simpleData: {
				enable: true
			}
		},
		callback: {
			beforeRemove: beforeRemove,
			beforeRename: beforeRename,
			onRemove: org_onRemove,
			onRename: org_onRename,
			onClick: org_onClick,
			onCheck: onCheck
		}
}
}

//异常空数据情况处理
function filter(treeId, parentNode, childNodes) {
	if (!childNodes) return null;
	for (var i=0, l=childNodes.length; i<l; i++) {
		childNodes[i].name = childNodes[i].name.replace(/\.n/g, '.');
	}
	//orgTreeData_json = childNodes;
	return childNodes;
}

//处理事件
var log, className = "dark";
//禁止所有拖拽动作
function beforeDrag(treeId, treeNodes) {
	return false;
}

//删除前提示确认
function beforeRemove(treeId, treeNode) {
	className = (className === "dark" ? "":"dark");
	showLog("[ "+getTime()+" beforeRemove ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name);
	var zTree = $.fn.zTree.getZTreeObj("orgTree");
	zTree.selectNode(treeNode);
	return confirm("确认删除 节点 -- " + treeNode.name + " 吗？(如果有子节点的子节点也一并被删除)");
}
//删除时同时传递节点及其所有子节点id
function org_onRemove(e, treeId, treeNode) {
	showLog("[ "+getTime()+" onRemove ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name);
	var ids = [];
	ids = getChildren(ids,treeNode);
	
	$.ebAjax({
		url: '${ctx}/manager/security/organization!delete.action',         //DB delete if success->reload
		postData:{
			ids: ids						
		},
		success : function(){
			alert("节点及其所有子节点删除成功 ！")
			var zTree = $.fn.zTree.getZTreeObj("orgTree");
			treeObj.reAsyncChildNodes(treeNode.id, "refresh");
		},
		error : function(){
			alert("节点删除失败！");
		}
	});
}

//递归获取当前节点的所有子节点
function getChildren(ids,treeNode){
	 ids.push(treeNode.id);
	 if (treeNode.isParent){
		for(var obj in treeNode.children){
			getChildren(ids,treeNode.children[obj]);
		}
	 }
	 return ids;
}

//修改重命名时判空提示
function beforeRename(treeId, treeNode, newName, isCancel) {
	className = (className === "dark" ? "":"dark");
	showLog((isCancel ? "<span style='color:red'>":"") + "[ "+getTime()+" beforeRename ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name + (isCancel ? "</span>":""));
	if (newName.length == 0) {
		alert("节点名称不能为空.");
		var zTree = $.fn.zTree.getZTreeObj("orgTree");
		setTimeout(function(){zTree.editName(treeNode)}, 10);
		return false;
	}
	return true;
}
function org_onRename(e, treeId, treeNode, isCancel) {
	showLog((isCancel ? "<span style='color:red'>":"") + "[ "+getTime()+" onRename ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name + (isCancel ? "</span>":""));
	$("#orgName").val(treeNode.name); 
}

//************************click点击时带出右边frame名称**************************************************
function org_onClick(event, treeId, treeNode, clickFlag) {
	showLog("[ "+getTime()+" onClick ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name);
	if(treeNode.level == 0){
		$("#orgName").val("根节点信息不能修改！");
		$("#email").attr("readonly","readonly");
		$("#orgDesc").attr("readonly","readonly");
		$("#isFinal").attr("readonly","readonly");
	}else{
		$("#orgName").val(treeNode.name);
		$("#email").val(treeNode.email);
		$("#orgDesc").val(treeNode.orgDesc);
		if(treeNode.isFinal=="N"){
			$("#isFinal option[value='N']").attr("selected","selected");
		}else{
			$("#isFinal option[value='Y']").attr("selected","selected");
		}
		$("#id").val(treeNode.id);
		$("#parentId").val(treeNode.pId);
		$("#orgLevel").val(treeNode.level);
		$("#email").removeAttr("readonly");
		$("#orgDesc").removeAttr("readonly");
		$("#isFinal").removeAttr("readonly");
		
	
		//异步获取节点组织详细信息
		//var ajax_options = {
		//		url:'${ctx}/manager/security/organization!getOrgData.action',
		//		name:'userRole',
		//		postData:{
		//			id:treeNode.id							
		//		}				
		//};
		//getOrgDetail(ajax_options);
	}
}

function getOrgDetail(options){
	options.success = function(data) {
		var org_detail ="";
		if(data.success){
			org_detail = data.data;
	//		$("#orgName").val(org_detail[0].orgName);
			$("#email").val(org_detail[0].email);
			$("#orgDesc").val(org_detail[0].orgDesc);
			if(org_detail[0].isFinal=="N"){
				$("#isFinal option[value='N']").attr("selected","selected");
				//$('#isSubOrg option:eq(2)').attr('selected','selected'); 
			}else{
				$("#isFinal option[value='Y']").attr("selected","selected");
			}
			$("#id").val(org_detail[0].orgId);
			$("#parentId").val(org_detail[0].parentId);
			$("#orgLevel").val(org_detail[0].orgLevel);
		}
	};
	$.ebAjax(options)
}

//******************************************************************************************************
//除root节点外其他都显示删改按钮
function showRemoveBtn(treeId, treeNode) {
	 if(treeNode.level == 0){
		return false;
	 }else{
		return true;
	 }
}
function showRenameBtn(treeId, treeNode) {
	if(treeNode.level == 0){
		return false;
	}else{
		return true;
	}
}
function showLog(str) {
	if (!log) log = $("#log");
	log.append("<li class='"+className+"'>"+str+"</li>");
	if(log.children("li").length > 8) {
		log.get(0).removeChild(log.children("li")[0]);
	}
}
function getTime() {
	var now= new Date(),
	h=now.getHours(),
	m=now.getMinutes(),
	s=now.getSeconds(),
	ms=now.getMilliseconds();
	return (h+":"+m+":"+s+ " " +ms);
}

//控制是否有新增按钮
var newCount = 1;
function addHoverDom(treeId, treeNode) {
	if(treeNode.isFinal == "N"){  
		var sObj = $("#" + treeNode.tId + "_span");
		if (treeNode.editNameFlag || $("#addBtn_"+treeNode.tId).length>0) return;
		var addStr = "<span class='button add' id='addBtn_" + treeNode.tId
			+ "' title='add node' onfocus='this.blur();'></span>";
		sObj.after(addStr);
		var btn = $("#addBtn_"+treeNode.tId);
		if (btn) btn.bind("click", function(){
			var zTree = $.fn.zTree.getZTreeObj("orgTree");
			zTree.addNodes(treeNode, {id:(100 + newCount), pId:treeNode.id, name:"new node" + (newCount++)});
			$("#orgName").val(treeNode.name);
			$("#parentId").val(treeNode.pId);
			$("#orgLevel").val(treeNode.level);
			
			return false;
		});
	}else{      //该节点是叶子节点不能创建子节点->Y
		return false;
	}
};
function removeHoverDom(treeId, treeNode) {
	$("#addBtn_"+treeNode.tId).unbind().remove();
};
function selectAll() {
	var zTree = $.fn.zTree.getZTreeObj("orgTree");
	zTree.setting.edit.editNameSelectAll =  $("#selectAll").attr("checked");
}
function onCheck(e, treeId, treeNode) {
		var zTree = $.fn.zTree.getZTreeObj("orgTree"),
		nodes = zTree.getCheckedNodes(true),
		v = "",n="";
		for (var i=0, l=nodes.length; i<l; i++) {
			v += nodes[i].name + ",";
			n += nodes[i].id + ",";
		}
		if (v.length > 0 ) v = v.substring(0, v.length-1);
		if (n.length > 0 ) n = n.substring(0, n.length-1);
		$("#dir").attr("value", v);
		$("#dir").attr("data",n);
		
	}


ebTemplate.gridContentT = template.compile(
    '<div class="content-table" <% if ( id ) { %> id="<%=id %>"<% } %> >' +
        '<div id="toolbar" class="cmd-panel"></div>'+
        '<div class="content-main-grid clearfix" <% if ( id ) { %> id="content-<%=id %>"<% } %>> '+ 
        '<% if ( columns ) { %>'+
	        '<div class="row-fluid show-grid">' +
		        '<% for(var i=0;i<columns.length;i++){ %>'+
		            '<div class="<%=columns[i].clazz %>" style="width: <%=columns[i].width %>px;" ></div>' + 
		        '<% } %>'+
		    '</div>'+
		'<% } %>'+
        '</div>'+
    '</div>'
);
