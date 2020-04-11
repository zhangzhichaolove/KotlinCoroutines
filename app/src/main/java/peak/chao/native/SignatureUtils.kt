package peak.chao.native

import android.content.Context

object SignatureUtils {

    init {
        System.loadLibrary("peakchao")
    }

    /**
     * 校验签名
     * @param context
     * @return
     */
    external fun signatureVerify(context: Context)

    // AES加密, CBC, PKCS5Padding
    external fun encrypt(str: String?): String?

    // AES解密, CBC, PKCS5Padding
    external fun decrypt(str: String?): String?

}

//class SignatureUtils private constructor() {
//
//    /**
//     * 校验签名
//     * @param context
//     * @return
//     */
//    external fun signatureVerify(context: Context)
//
//    // AES加密, CBC, PKCS5Padding
//    external fun encrypt(str: String?): String?
//
//    // AES解密, CBC, PKCS5Padding
//    external fun decrypt(str: String?): String?
//
//    private object SingletonHolder {
//        val holder= SignatureUtils()
//    }
//
//    companion object {
//
//        val instance = SingletonHolder.holder
//
////        private var instance: SignatureUtils? = null
////            get() {
////                if (field == null) {
////                    field = SignatureUtils()
////                }
////                return field
////            }
////
////        @Synchronized
////        fun get(): SignatureUtils {
////            //这里不用getInstance作为为方法名，是因为在伴生对象声明时，内部已有getInstance方法，所以只能取其他名字
////            return instance!!
////        }
//
//
//        init {
//            System.loadLibrary("peakchao")
//        }
//
//    }
//}