package com.hugo.flutterhost

import android.os.Bundle
import com.hugo.flutterhost.plugin.MoblinkPlugin2
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity : FlutterActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MoblinkPlugin2.registerWith(flutterEngine,this)
  }
    override fun getDartEntrypointFunctionName(): String {
        return super.getDartEntrypointFunctionName()
    }
}