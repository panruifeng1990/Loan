$(function(){
    //入口页
	$(".qiang-btn").on("click",function(){
        if (l == 0) {
            popFn("请检查您的定位是否开启定位必须在杭州哦~");
            LocationCompetence();
            return;
        }

        if(login != 1){
            loginIn();
            return;
        }

        if(bindP != 1 && token != ''){
            bindPhone();
            return;
        }

        if (brk == 1) {
            popFn("抱歉，杭州经纪人不允许参与此活动！");
            return;
        }
        window.location.href = url;
	});
	
	/************************ 经纪人编号页 ************************/
	
    //经纪人编号
	$(".bianhao-box").height($(window).height());
	
    //扫描弹窗
    $(".commit-btn").click(function(){
        var token = $('#token').val();
        var activityId = $('#activityId').val();
        var deviceId = $('#deviceId').val();
        var location = $('#location').val();
        var smdeviceId = $('#smdeviceId').val();
        var numVal = $('#number').val();
        var telephone = $('#telephone').val();
        var phoneCode = $('#phoneCode').val();
        var sign = $('#sign').val();

        if(numVal != ''){
        	$("body").append(popEl);
            $.ajax({
                type: "post",
                url: 'drawcinemaprize',
                data: {num:numVal,telephone:telephone,phoneCode:phoneCode,activity:activityId,token:token,video_id:deviceId,location:location,smdeviceId:smdeviceId,sign:sign},
                dataType: "json",
                success: function (data) {
                    if(data.code==200){
                        popFn("扫描成功<br/>请通知您的客户去兑换电影票吧");
                    }else{
                        if(data.message=="wrongNum"){
                            popFn("扫描失败<br/>您输入的员工编号有误");
                        } else if (data.message == "hasReceive") {
                            popFn("扫描失败<br/>您扫描的二维码已经领取过奖品");
                        } else if (data.message == "outTime") {
                            popFn("扫描失败<br/>每日核销时间：7:00~22:00");
                        } else if (data.message == "brkLimitN") {
                            popFn("扫描失败<br/>您当前核销的数量已经达到上限");
                        }else {
                            popFn("活动已经结束<br/>请关注我爱我家的其他活动");
                        }
                    }
                }
            });
        }
    });

	/***** 验证码 *****/
	var NUMCODE = "";
	var ERROR_NEWS = "";

	function trim(str) {
//        console.log(str);
        return str.replace(/(^\s*)|(\s*$)/g, "");
    }
	
	//点击发送验证码按钮
	function checkPhone() {//验证手机号
        return true;
    }
	
	var codePop = '<div class="pop-mask">'+
				    '<div class="pop-box">'+
				        '<div class="pop-con">'+
				            '<h3>请输入图片验证码</h3>'+
				            '<div class="code-con">'+
				                '<div class="code-form">'+
				                    '<p class="code-inp"><span><input type="text" placeholder="" id="code"></span></p>'+
				                    '<a class="code-img"><span><img id="code-img" src="" alt="验证码" title="点击刷新"></span></a>'+
				                '</div>'+
				                '<p class="code-error hide">验证码错误</p>'+
				            '</div>'+
				            '<a class="code-btn">确认</a>'+
				            '<a class="code-close"></a>'+
				        '</div>'+
				    '</div>'+
				'</div>';
	
    $(".yzm").click(function () {
        var numVal = $('#number').val();
        $.ajax({
            type: "post",
            url: 'ajaxgetbroker',
            data: {num:numVal},
            dataType: "json",
            success: function (data) {
                if(data.code==200){
                    $("body").append(codePop);
                    $('#telephone').val(data.data.res);
                    codeFn(data.data.res);
                    $('#code-img').attr("src", "/event/validatecodereceipt?" + Math.random());
                }else{
                    popFn("扫描失败<br/>您输入的员工编号有误");
                }
            }
        });

        //验证验证码是否正确
        function codeFn(brokerPhone){
	        $(".code-btn").on("click", function () {
	            var code = trim($('#code').val());
	            if ("" == code) {
	                popFn("请填写验证码");
	                return false;
	            }
	            $.getJSON(
	                '/event/ajaxvalidatecodeverify',
	                {'code': code.toLowerCase()},
	                function (r) {
	                    if (r.ok == 1) {
	                        time();
	                        $(".pop-mask").remove();
	                        var phoneNew = brokerPhone.substr(0,3) + '****' + brokerPhone.substr(7);
	                        $(".yifa-phone").html("验证码已发送到您<span>" + phoneNew + "</span>的手机上");
	                    } else {
	                        $('.code-error').removeClass('hide');
	                    }
	                }
	            );
	        });
	        
	        $(".code-close").on("click",function(){
	            $(".pop-mask").remove();
	        });
	
	        $('.code-img').on('click', function () {
	            $('#code-img').attr("src", "/event/validatecodereceipt?" + Math.random());
	        });
        }

        //执行发送短信验证码ajax
        // time();
    });
    
	//发送手机短信验证码
    var wait = 60;

    function time() {
        if (wait == 0) {
            $('#hsend').empty();
            $('#hsend').html('重新发送');

            //将重新发送短信验证码的a标签恢复成 正常可点击状态
            $("#hsend").removeClass("daojishi");
            wait = 60;
        } else if (wait == 60) {
            var telephone = $('#telephone').val();
            $.ajax({
                type: "get",
                url: "/event/ajaxphonecode",
                data: {'telephone': telephone},
                dataType: "json",
                success: function (data) {
                    if (data.code != 200) { //发送失败的情况
                        $(".errBox").css("display", "block");
                        $('#errorMessage').html(data.message);
                        return false;
                    } else if (data.code == 200) {  //发送成功的情况
                        NUMCODE = data.num;
                        $(".errBox").css("display", "block");
                        $('#errorMessage').html('短信验证码已成功发送!');
                        //1秒后关闭..
                        setTimeout(function () {
                            $(".errBox").css("display", "none");
                            $('#errorMessage').html("");
                        }, 1100);

                        $('#hsend').empty();
                        $('#hsend').removeClass('msend');
                        $('#hsend').html('<em class="prompt-1">' + wait + '</em>秒后发送');
                        wait--;

                        //把超链接变成不可点击效果,避免重复点击的时候多次同时执行相同方法,导致倒计时时间变得加速起来  ==>实际测试好使
						$("#hsend").addClass("daojishi");

                        setTimeout(function () {
                            time()
                        }, 1000);
                        return false;
                    } else {
                        //$('#telephoneErr').html('发送验证码不成功,请联系管理员');
                        $(".errBox").css("display", "block");
                        $('#errorMessage').html(data.message);
                        ERROR_NEWS = data.message;
                    }
                }
            });
        } else {
            setTimeout(function () {
                $('#hsend').empty();
                $('#hsend').removeClass('msend');
                wait--;
                $('#hsend').html('<em class="prompt-1">' + wait + '</em>秒后发送');
                time()
            }, 900);
        }
    }

    /************************ 分享页 ************************/
	var u = navigator.userAgent.toLocaleLowerCase();
    var isIOS = !!u.match(/(iphone|ipod|ipad|mac)/i);
    var isAndroid = u.indexOf('android') > -1 || u.indexOf('linux') > -1;

    if(isIOS){
        $(".btn-link").on("click",function(){
            window.location.href = "https://itunes.apple.com/cn/app/wo-ai-wo-jia/id1004322269?mt=8";
        });
    }
    if(isAndroid){
        $(".btn-link").on("click",function(){
            window.location.href = "http://res.5i5j.com/cache/android/20190328/app-online-release_417_31_guanwang_sign.apk";
        });
    }

    //弹窗
    var popEl = '<div class="saomiao-popup">'+
				'<i class="loading"></i>'+
				'<p></p>'+
				'</div>';
				
    function popFn(conTxt){
    	$("body").append(popEl);
		$(".loading").hide();
	    $(".saomiao-popup p").html(conTxt);
	    
	    setTimeout(function(){
	        $(".saomiao-popup").remove();
	        $("#number").val("");
	    },1000);
	}
    
});