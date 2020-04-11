package com.chao.kotlincoroutines

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.util.Log
import android.view.View
import com.chao.kotlincoroutines.api.GitHubApi
import com.chao.kotlincoroutines.base.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import peak.chao.native.SignatureUtils


class MainActivity : BaseActivity() {
    var myJob: Job? = null

    override fun initData() {
        getSign()
        SignatureUtils.signatureVerify(this)
        val stringFromJNI = stringFromJNI()
        val encrypt = SignatureUtils.encrypt(stringFromJNI)
        val decrypt = SignatureUtils.decrypt(encrypt)
        showText.text = "原始字符串：$stringFromJNI\n加密后字符串：$encrypt\n解密后字符串：$decrypt"
        search.setOnClickListener {
            //启动一个协程
            myJob = launch {
                //因为还是在主线程,所以可以直接操作UI
                progress.visibility = View.VISIBLE
                /*因为Android规定网络请求必须在子线程,
                  所以这里我们通过withContext获取请求结果,
                  通过调度器Dispatcher切换到IO线程,
                  这个操作会挂起当前协程,但是不会阻塞当前线程*/
                val result = withContext(Dispatchers.IO) {
                    /*这里已经是在子线程了,所以使用的是excute()而不是enqueue()
                    execute()返回的是Response类型
                      withContext会把闭包最后一行代码的返回值返回出去
                      所以上面的result就是Response类型*/
                    GitHubApi.getInstance().getListRepos(searchText.text.toString()).execute()
                }
                //上面请求结束之后,又返回到了主线程
                //打一个log,用于一会儿测试看看activity关闭了,协程还会不会继续执行
                Log.d("mark", result.toString())
                progress.visibility = View.GONE
                if (result.isSuccessful) {
                    //因为返回到了主线程,所以可以直接操作UI
                    result.body()?.forEach {
                        //为了简单起见,我们只打印请求结果的一个字段
                        showText.append("\n${it.fullName}")
                    }
                }
            }
        }
        cancel.setOnClickListener {
            progress.visibility = View.GONE
            myJob?.cancel()
        }
    }

    override fun getLayout(): Int {
        return R.layout.activity_main
    }

    fun getSign() {
        var packageInfo: PackageInfo? = null
        try {
            packageInfo =
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        val signatures: Array<Signature> = packageInfo!!.signatures
        Log.e("TAG", signatures[0].toCharsString())
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("peakchao")
        }
    }
}
