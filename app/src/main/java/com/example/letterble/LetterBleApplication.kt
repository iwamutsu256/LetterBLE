package com.example.letterble

import android.app.Application
import com.example.letterble.di.AppContainer
import com.example.letterble.di.DefaultAppContainer

class LetterBleApplication : Application() {
    // アプリ全体で使う依存関係の入口。
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // Application 起点で依存関係を一度だけ組み立てる。
        appContainer = DefaultAppContainer(this)
    }
}
