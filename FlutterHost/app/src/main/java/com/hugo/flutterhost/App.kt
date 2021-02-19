package com.hugo.flutterhost

import android.app.Application
import com.hugo.flutterhost.plugin.MoblinkPlugin2

import com.mob.moblink.MobLink;
class App :Application() {

    override fun onCreate() {
        super.onCreate()
        //MobSDK.init(this, "您的Mob-AppKey", "您的Mob-AppSecret")
        MobLink.setRestoreSceneListener(MoblinkPlugin2.SceneListener())
    }
}