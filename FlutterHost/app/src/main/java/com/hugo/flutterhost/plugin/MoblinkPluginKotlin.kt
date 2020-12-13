package com.hugo.flutterhost.plugin

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import com.mob.MobSDK
import com.mob.OperationCallback
import com.mob.PrivacyPolicy
import com.mob.PrivacyPolicy.OnPolicyListener
import com.mob.moblink.*
import com.mob.tools.utils.Hashon
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import java.util.*

/**
 * MoblinkPlugin
 */
class MoblinkPluginKotlin(private val activity: Activity) : MethodCallHandler, SceneRestorable {


    class SceneListener : RestoreSceneListener {
        override fun willRestoreScene(scene: Scene): Class<out Activity>? {
            Log.e("WWW", " willRestoreScene==" + Hashon().fromObject(scene))
            onReturnSceneDataMap = HashMap()
            onReturnSceneDataMap!!["path"] = scene.getPath()
            onReturnSceneDataMap!!["params"] = scene.getParams()
            Log.e(
                "WWW",
                " willRestoreScene[onReturnSceneDataMap]==" + Hashon().fromObject(
                    onReturnSceneDataMap
                )
            )
            ismEventSinkNotNull = if (null != mEventSink) {
                Log.e("WWW", " willRestoreScene[onReturnSceneDataMap]==开始回调了传递数据了")
                restoreScene()
                false
            } else {
                true
            }
            return null
        }

        override fun notFoundScene(scene: Scene) {
            //TODO 未找到处理scene的activity时回调
        }

        override fun completeRestore(scene: Scene) {
            // TODO 在"拉起"处理场景的Activity之后调用
        }
    }

    /**
     * 提供给java层传递数据到flutter层的方法
     */
    fun setEvent(data: Any?) {
        if (mEventSink != null) {
            mEventSink!!.success(data)
        } else {
            Log.e("WWW", " ===== FlutterEventChannel.eventSink 为空 需要检查一下 ===== ")
        }
    }

    private fun queryPrivacy() {
        // 异步方法
        MobSDK.getPrivacyPolicyAsync(MobSDK.POLICY_TYPE_URL, object : OnPolicyListener {
            override fun onComplete(data: PrivacyPolicy) {
                if (data != null) {
                    // 富文本内容
                    val text = data.content
                }
            }

            override fun onFailure(t: Throwable) {
                // 请求失败
                //Log.e(TAG, "隐私协议查询结果：失败 " + t);
            }
        })
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        MobLink.setActivityDelegate(activity, this@MoblinkPluginKotlin)
        when (call.method) {
            getMobId -> getMobId(call, result)
            restoreScene -> restoreScene(result)
            "getPlatformVersion" -> {
                val release = Build.VERSION.RELEASE
                result.success("Android $release")
            }
            "getPrivateContent" -> {
                val para = call.arguments as Int
                val content: String? = getPrivateContent(para)
                if (!TextUtils.isEmpty(content)) {
                    result.success(content)
                } else {
                    result.error("UNAVAILABLE", "not get private content", null)
                }
            }
            "submitPrivacyGrantResult" -> {
                val para = call.arguments as Boolean
                Log.e("WWW", " para==$para")
                submitPrivacyGrantResult(para, result)
            }
            "uploadPrivacyPermissionStatus" -> {
                Log.e("-->>", "uploadPrivacyPermissionStatus " + call.arguments)
                result.success(call.arguments)
            }
            "getPrivacyPolicy" -> {
                Log.e("-->>", "getPrivacyPolicy " + call.arguments)
                result.success(call.arguments)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun getMobId(call: MethodCall, result: MethodChannel.Result) {
        val map =
            call.arguments<HashMap<String, Any>>()
        val params =
            map["params"] as HashMap<String, Any>?
        val path = map["path"].toString()

        // 新建场景
        val s = Scene()
        s.path = path
        s.params = params

        // 请求场景ID
        MobLink.getMobID(s, object : ActionListener<String> {
            override fun onResult(o: String) {
                val resposon = HashMap<String, String>()
                resposon["mobid"] = o
                resposon["domain"] = ""
                result.success(resposon)
            }

            override fun onError(throwable: Throwable) {
                result.error(throwable.message.toString(), null, null)
            }
        })
    }

    private fun restoreScene(result: MethodChannel.Result?) {
        if (result != null) {
            if (onReturnSceneDataMap != null) {
                mEventSink!!.success(onReturnSceneDataMap)
                onReturnSceneDataMap = null
            }
            if (onReturnSceneDataMap != null && onReturnSceneDataMap!!.size > 0) {
                if (null != mEventSink) {
                    mEventSink!!.success(onReturnSceneDataMap)
                } else {
                    Handler().postDelayed({
                        if (null != mEventSink) {
                            mEventSink?.success(onReturnSceneDataMap)
                        }
                    }, 1000)
                }
            }
        }
    }

    /**
     * 获取隐私协议的内容
     */
    private fun getPrivateContent(i: Int): String? {
        var text: String? = null
        MobSDK.getPrivacyPolicyAsync(i, object : OnPolicyListener {
            override fun onComplete(data: PrivacyPolicy) {
                if (data != null) {
                    // 富文本内容
                    text = data.content
                    Log.e("WWW", " 隐私协议内容==$text")
                }
            }

            override fun onFailure(t: Throwable) {
                // 请求失败
                Log.e("-->>onFailure", "隐私协议查询结果：失败 " + t);
            }
        })
        return text
    }

    /**
     * 同意隐私协议
     */
    private fun submitPrivacyGrantResult(granted: Boolean, result: MethodChannel.Result) {
        MobSDK.submitPolicyGrantResult(granted, object : OperationCallback<Void>() {
            override fun onComplete(data: Void?) {
                Log.e("WWW", "隐私协议授权结果提交：成功 $data")
                result.success("true")
            }

            override fun onFailure(t: Throwable) {
                Log.e("WWW", "隐私协议授权结果提交：失败: $t")
                result.error(t.message, "提交失败", "failed")
            }
        })
    }

    override fun onReturnSceneData(scene: Scene) {
        Log.e("WWW", " onReturnSceneData==" + Hashon().fromObject(scene))
        onReturnSceneDataMap =
            HashMap()
        onReturnSceneDataMap!!["path"] = scene.getPath()
        onReturnSceneDataMap!!["params"] = scene.getParams()
    }

    companion object {
        private const val getMobId = "getMobId"
        private const val restoreScene = "restoreScene"
        private const val EventChannel = "JAVA_TO_FLUTTER"
        const val METHOD_CHANNEL = "private.flutter.io/method_channel"
        const val METHOD_CHANNEL_SUBMIT_PRIVATE = "private.flutter.io/method_channel_submit_private"
        private var mEventSink: EventSink? = null
        private var ismEventSinkNotNull = false
        private var onReturnSceneDataMap: HashMap<String, Any>? = null

        /**
         * Plugin registration.
         */
        fun registerWith(flutterEngine: FlutterEngine, activity: Activity) {
            Log.e("WWW", " registerWith[注册过来回传监听的回掉]==")
            var moblinkPluginKotlin = MoblinkPluginKotlin(activity)
            val dartExecutor = flutterEngine.dartExecutor
            val channel = MethodChannel(dartExecutor, "com.yoozoo.mob/moblink")
            channel.setMethodCallHandler(moblinkPluginKotlin)
            //        MoblinkPlugin2.activity = activity;
            val eventChannel =
                EventChannel(dartExecutor, EventChannel)
            eventChannel.setStreamHandler(object : EventChannel.StreamHandler {
                override fun onListen(o: Any, eventSink: EventSink) {
                    Log.e("WWW", " registerWith(onListen)[接受到回掉]==")
                    mEventSink = eventSink
                    if (ismEventSinkNotNull) {
                        Log.e("WWW", " registerWith[onListen]==开始回调了传递数据了")
                        ismEventSinkNotNull = false
                        restoreScene()
                    }
                }

                override fun onCancel(o: Any) {}
            })

            val privacyChannel = MethodChannel(dartExecutor, METHOD_CHANNEL)
            privacyChannel.setMethodCallHandler(moblinkPluginKotlin::onMethodCall)
            val submitPrivacyChannel = MethodChannel(dartExecutor, METHOD_CHANNEL_SUBMIT_PRIVATE)
            submitPrivacyChannel.setMethodCallHandler(moblinkPluginKotlin::onMethodCall)
        }

        private fun restoreScene() {
            if (mEventSink != null) {
                if (onReturnSceneDataMap != null) {
                    mEventSink!!.success(onReturnSceneDataMap)
                    onReturnSceneDataMap = null
                }
            }
        }
    }

    init {
        //场景还原监听
        MobLink.setRestoreSceneListener(SceneListener())
    }
}