package id.agen.zoloz_sdk

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import androidx.annotation.NonNull
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.ap.zoloz.hummer.api.*
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.File
import java.io.IOException

/** ZolozSdkPlugin */
class ZolozSdkPlugin: FlutterPlugin, MethodCallHandler,ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var activity: Activity
  private lateinit var context: Context
  private var mHandler: Handler? = null
  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "zoloz_sdk")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
    mHandler = Handler()

  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "startZoloz") {
      val args = call.arguments as HashMap<String, String>
      startZoloz(result,args)
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  private fun runOnIoThread(runnable: Runnable) {
    val thread = Thread(runnable)
    thread.start()
  }

  private fun startZoloz(@NonNull result: Result,args:HashMap<String, String>) {
    val uIConfig =  getFileFromAssets( ).path
    val time = System.currentTimeMillis()
    val initServer=args["init"]
    val checkServer=args["check"]
    runOnIoThread {
      val initRequest: String? = initRequest(initServer!!,"$time")
      val initResponse = JSON.parseObject(initRequest, InitResponse::class.java)
      val zlzFacade = ZLZFacade.getInstance()
      val request = ZLZRequest()
      request.zlzConfig = initResponse.clientCfg
      request.bizConfig[ZLZConstants.CONTEXT] = activity
      request.bizConfig[ZLZConstants.PUBLIC_KEY] = initResponse.rsaPubKey
      request.bizConfig[ZLZConstants.LOCALE] = "in-ID"
      request.bizConfig[ZLZConstants.CHAMELEON_CONFIG_PATH] = uIConfig
      mHandler!!.postAtFrontOfQueue {
        zlzFacade.start(request, object : IZLZCallback {
          override fun onCompleted(response: ZLZResponse) {
            runOnIoThread {
              val initCheck = initCheck("$time", initResponse.transactionId,checkServer!!)
              result.success(initCheck)
            }
          }
          override fun onInterrupted(response: ZLZResponse) {
            result.success(response.retCode)
          }
        })
      }
    }
  }

  @SuppressLint("SimpleDateFormat")
  private fun initRequest(requestUrl:String,time:String): String? {
    val request: IRequest = LocalRequest()
    val jsonObject = JSONObject()
    val metaInfo: String = ZLZFacade.getMetaInfo(context)
    jsonObject["metaInfo"]=metaInfo
    jsonObject["bizId"] = time
    jsonObject["userId"] = time
    val requestData: String = jsonObject.toString()
    return request.request(requestUrl, requestData)
  }

  private fun initCheck(bizId: String, transactionId: String,requestUrl:String): String? {
    val request: IRequest = LocalRequest()
    val jsonObject = JSONObject()
    jsonObject["bizId"] = bizId
    jsonObject["transactionId"] = transactionId
    val requestData: String = jsonObject.toString()
    return request.request(requestUrl, requestData)
  }
  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity

  }

  override fun onDetachedFromActivityForConfigChanges() {
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
  }

  override fun onDetachedFromActivity() {
  }

  @Throws(IOException::class)
  private fun getFileFromAssets(): File = File(context.cacheDir, "UIConfig.zip")
    .also {
      if (!it.exists()) {
        it.outputStream().use { cache ->
          activity.assets.open("UIConfig.zip").use { inputStream ->
            inputStream.copyTo(cache)
          }
        }
      }
    }
}
