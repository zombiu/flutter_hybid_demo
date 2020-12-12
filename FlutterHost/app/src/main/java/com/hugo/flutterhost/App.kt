package com.hugo.flutterhost

import android.app.Application
import com.hugo.flutterhost.plugin.MoblinkPlugin2

import com.mob.moblink.MobLink;
class App :Application() {

    override fun onCreate() {
        super.onCreate()
        MobLink.setRestoreSceneListener(MoblinkPlugin2.SceneListener())
    }
}