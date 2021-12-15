package com.wxwx.flutter_alibc
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import com.alibaba.alibclogin.AlibcLogin
import com.alibaba.alibcprotocol.callback.AlibcLoginCallback
import com.alibaba.alibcprotocol.callback.AlibcTradeCallback
import com.alibaba.alibcprotocol.param.AlibcBizParams
import com.alibaba.alibcprotocol.param.AlibcDegradeType
import com.alibaba.alibcprotocol.param.AlibcShowParams
import com.alibaba.alibcprotocol.param.AlibcTaokeParams
import com.baichuan.nb_trade.AlibcTrade
import com.baichuan.nb_trade.callback.AlibcTradeInitCallback
import com.baichuan.nb_trade.core.AlibcTradeSDK
import com.wxwx.flutter_alibc.web.WebViewActivity
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.util.*

class FlutterAlibcHandle(var methodChannel: MethodChannel?){
    var activity:Activity? = null
    fun disposed(){
        this.methodChannel = null
        this.activity = null
    }

    /**
     * 初始化阿里百川
     * @param call
     * @param result
     */
    fun initAlibc(result: MethodChannel.Result){
        AlibcTradeSDK.asyncInit(activity!!.application, HashMap(), object : AlibcTradeInitCallback {
            override fun onSuccess() {
                result.success(PluginResponse.success(null).toMap())
            }

            override fun onFailure(code: Int, msg: String?) {
                result.success(PluginResponse(code.toString(), msg, null).toMap())
            }
        })
    }

    /**
     * 登陆淘宝
     * @param result
     */
    fun loginTaoBao(){
        val alibcLogin = AlibcLogin.getInstance()
        if(alibcLogin.isLogin){
            methodChannel!!.invokeMethod("AlibcTaobaoLogin", PluginResponse.success(alibcLogin.userInfo).toMap())
            return
        }
        alibcLogin.showLogin(object : AlibcLoginCallback {
            override fun onSuccess(p0: String?, p1: String?) {
                methodChannel!!.invokeMethod("AlibcTaobaoLogin", PluginResponse.success(alibcLogin.userInfo).toMap())
            }

            override fun onFailure(code: Int, msg: String?) {
                // code：错误码  msg： 错误信息
                methodChannel!!.invokeMethod("AlibcTaobaoLogin", PluginResponse(code.toString(), msg, null).toMap())
            }
        })
    }

    /**
     * 登出
     * @param result
     */
    fun logoutTaoBao(result: MethodChannel.Result){
        val alibcLogin = AlibcLogin.getInstance()
        alibcLogin.logout(object : AlibcLoginCallback {
            override fun onSuccess(p1: String?, p2: String?) {
                result.success(PluginResponse.success(null).toMap())
            }

            override fun onFailure(code: Int, msg: String?) {
                result.success(PluginResponse(code.toString(), msg, null).toMap())
            }
        })
    }

    /**
     * 淘宝授权登陆  获取access_token
     *  官方说明文档 {https://open.taobao.com/doc.htm?docId=118&docType=1}
     * @param call
     * @param result
     */
    fun taoKeLogin(call: MethodCall){
        val map = call.arguments as HashMap<String, Any>
        val url = call.argument<String>("url")
        WebViewActivity.callBack = object : WebViewActivity.Callback {
            override fun success(accessToken: String?) {
                val resMap: HashMap<String, Any?> = HashMap()
                resMap.put("accessToken", accessToken)
                methodChannel!!.invokeMethod("AlibcTaokeLogin", PluginResponse.success(resMap).toMap())
            }

            override fun failed(errorMsg: String?) {
                val code = -1
                methodChannel!!.invokeMethod("AlibcTaokeLogin", PluginResponse(code.toString(), errorMsg, null).toMap())
            }
        }
        val intent = Intent(activity!!, WebViewActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("url", url)
        intent.putExtra("arguments", map)
        activity!!.startActivity(intent)
    }

    /**
     * 淘宝授权登陆  获取code 授权码
     *  官方说明文档 {https://open.taobao.com/doc.htm?docId=118&docType=1}
     * @param call
     * @param result
     */
    fun taoKeLoginForCode(call: MethodCall){
        val map = call.arguments as HashMap<*, *>
        val url = call.argument<String>("url")
        WebViewActivity.callBack = object : WebViewActivity.Callback {
            override fun success(accessToken: String?) {
                val resMap: HashMap<String, Any?> = HashMap<String, Any?>()
                resMap.put("code", accessToken)
                methodChannel!!.invokeMethod("AlibcTaokeLoginForCode", PluginResponse.success(resMap).toMap())
            }

            override fun failed(errorMsg: String?) {
                var code = -1
                methodChannel!!.invokeMethod("AlibcTaokeLoginForCode", PluginResponse(code.toString(), errorMsg, null).toMap())
            }
        }
        val intent = Intent(activity!!, WebViewActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("url", url)
        intent.putExtra("arguments", map)
        activity!!.startActivity(intent)
    }

    /**
     * 通过URL方式打开淘宝
     * @param call
     * @param result
     */
    fun openByUrl(call: MethodCall, result: MethodChannel.Result){
        val showParams = AlibcShowParams()
        var taokeParams: AlibcTaokeParams? = AlibcTaokeParams("", "", "")

        showParams.backUrl = call.argument(PluginConstants.key_BackUrl)

        if (call.argument<Any?>(PluginConstants.key_OpenType) != null) {
            showParams.openType = PluginUtil.getOpenType("" + call.argument<Any>(PluginConstants.key_OpenType))
        }
        if (call.argument<Any?>(PluginConstants.key_ClientType) != null) {
            showParams.clientType = PluginUtil.getClientType("" + call.argument<Any>(PluginConstants.key_ClientType))
        }
        if (call.argument<Any?>("taokeParams") != null) {
            taokeParams = PluginUtil.getTaokeParams(call.argument("taokeParams")!!)
        }
        if ("false" == call.argument("isNeedCustomNativeFailMode")) {
            showParams.degradeType = AlibcDegradeType.NONE
        } else if (call.argument(PluginConstants.key_NativeFailMode) != null) {
            showParams.degradeType = PluginUtil.getFailModeType("" + call.argument<Any>(PluginConstants.key_NativeFailMode))
        }

        val trackParams: Map<String, String> = HashMap()
        val url = call.argument("url")
        AlibcTrade.openByUrl(activity, url, showParams, taokeParams, trackParams, object : AlibcTradeCallback {
            override fun onSuccess(p0: Int) {
                val results = HashMap<String, Any>()
                results.put("code", p0.toString());
                methodChannel!!.invokeMethod("AlibcOpenURL", PluginResponse.success(results).toMap())
            }

            override fun onFailure(p0: Int, p1: String?) {
                methodChannel!!.invokeMethod("AlibcOpenURL", PluginResponse(p0.toString(), p1, null).toMap())
            }
        })
    }

    /**
     * 打开商店
     * @param call
     * @param result
     */
    fun openShop(call: MethodCall, result: MethodChannel.Result) {
        val bizParams = AlibcBizParams()
        bizParams.shopId = call.argument("shopId")
        openByBizCode(bizParams, "shop", "AlibcOpenShop",call, result)
    }

    /**
     * 打开购物车
     * @param result
     */
    fun openCart(call: MethodCall, result: MethodChannel.Result) {
        openByBizCode(AlibcBizParams(), "cart", "AlibcOpenCar", call, result)
    }

    /**
     * 打开商品详情
     * @param call   call.argument["itemID"]  详情id
     * @param result
     */
    fun openItemDetail(call: MethodCall, result: MethodChannel.Result) {
        val bizParams = AlibcBizParams()
        bizParams.id = call.argument("itemID")
        openByBizCode(bizParams, "detail", "AlibcOpenDetail",call, result)
    }

    private fun openByBizCode(bizParams: AlibcBizParams, type: String, methodName: String, call: MethodCall, result: MethodChannel.Result) {
        val showParams = AlibcShowParams()
        var taokeParams: AlibcTaokeParams? = AlibcTaokeParams("", "", "")
        showParams.backUrl = call.argument(PluginConstants.key_BackUrl)
        if (call.argument<Any?>(PluginConstants.key_OpenType) != null) {
            showParams.openType = PluginUtil.getOpenType("" + call.argument<Any>(PluginConstants.key_OpenType))
        }
        if (call.argument<Any?>(PluginConstants.key_ClientType) != null) {
            showParams.clientType = PluginUtil.getClientType("" + call.argument<Any>(PluginConstants.key_ClientType))
        }
        if (call.argument<Any?>("taokeParams") != null) {
            taokeParams = PluginUtil.getTaokeParams(call.argument<Map<String?, Any?>>("taokeParams")!!)
        }
        if ("false" == call.argument<Any>("isNeedCustomNativeFailMode")) {
            showParams.degradeType = AlibcDegradeType.NONE
        } else if (call.argument<Any?>(PluginConstants.key_NativeFailMode) != null) {
            showParams.degradeType = PluginUtil.getFailModeType("" + call.argument<Any>(PluginConstants.key_NativeFailMode))
        }
        val trackParams: Map<String, String> = HashMap()
        AlibcTrade.openByCode(activity, type, bizParams, showParams, taokeParams, trackParams, object : AlibcTradeCallback {
            override fun onSuccess(p0: Int) {
                val results = HashMap<String, Any>()
                results.put("code", p0.toString());
                methodChannel!!.invokeMethod("AlibcOpenURL", PluginResponse.success(results).toMap())
            }

            override fun onFailure(code: Int, msg: String?) {
                methodChannel!!.invokeMethod(methodName, PluginResponse(code.toString(), msg, null).toMap())
            }

        })
    }

    /**
     * 设置淘客打点策略 是否异步
     * @param call
     */
    fun syncForTaoke(call: MethodCall) {
    }

    fun useAlipayNative(call: MethodCall) {
    }
}