package com.chao.kotlincoroutines.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class BaseActivity : AppCompatActivity(), CoroutineScope {

    //job用于控制协程,后面launch{}启动的协程,返回的job就是这个job对象
    private lateinit var job: Job

    //继承CoroutineScope必须初始化coroutineContext变量
    // 这个是标准写法,+其实是plus方法前面表示job,用于控制协程,后面是Dispatchers,指定启动的线程
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //在onCreate中初始化job
        job = Job()
        setContentView(getLayout())
        initData()
    }

    override fun onDestroy() {
        super.onDestroy()
        //当acitivity结束之后,我们不需要再请求网络了,结束当前协程
        job.cancel()
    }

    abstract fun initData()

    abstract fun getLayout(): Int
}