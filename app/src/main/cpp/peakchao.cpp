#include <jni.h>
#include <string>
#include <android/log.h>

#include "aes_utils.h"
#include "tools.h"

//校验签名
static int is_verify = 0;
static char *PACKAGE_NAME = "com.chao.kotlincoroutines";
static char *APP_SIGNATURE = "308202e4308201cc020101300d06092a864886f70d010105050030373116301406035504030c0d416e64726f69642044656275673110300e060355040a0c07416e64726f6964310b30090603550406130255533020170d3230303332333133313130325a180f32303530303331363133313130325a30373116301406035504030c0d416e64726f69642044656275673110300e060355040a0c07416e64726f6964310b300906035504061302555330820122300d06092a864886f70d01010105000382010f003082010a02820101008359797f80dfd097eec1e3062bfde5241aeea1917e88ab66b9ab0e8ab097e00b8ac2916ca78c2f5a13e61f75abfcd31e66a8a63947ddc44b7378fe3994e3e9f1771dcab83e0041b330b726501da810adaffefc939700fab7c0cab04f6d9c918ac5e1b786cde3f9ac80dbaf2b34ec939505f2494faea7e0472a8c3a23733366bfba01a8c75d7acf977eec0efa105a7b5e60e71b931b13a68176750f73511c4dff5ca9bb764ab4c6db1d1d1964dd264e0d31e985f7499f56f997a1c787666d64f881e759301408e65ff320af9fc1b939fccf06aa8d25c05da5de8813150f0d7c3ca799c5937de34096a44ff73381f20ab33e1a9ebea55376594983fd3e2228257f0203010001300d06092a864886f70d0101050500038201010074ac7ac689800d812465ef9fa72cbb320c1ded1fe13974f624a4ce48e920a5848c4a2fec89831bf81c2cb68f26f3ae30f168785bd6a4948fcae69661311a615f82fdbd79b71a3e78319d97f9acc0f305c71ea26b5ca01f47f1b7268d6e4d3c8f73dc4e9ad146a759f455c27351f6c4c958d054a1db12513ed13b7197c04ba6f5e6a6504a8ec347b032334e862d90d51b2221a0967d7abdf3d9df792d739629a1eeceb73aee731f001dc9a8ab21769f17b7557150ef4794e24a94632316e1b1a34118cc73ce9e4b2758b1844417422a3a6dd942326fe1d2dea80041b29e1b878953e85c7291535b46b5f7dfe20a5eebfd3eb2e6e151f125a4dafb0ffc0a657d0a";
using namespace std;

extern "C" JNIEXPORT jstring JNICALL
Java_com_chao_kotlincoroutines_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    string hello = "签名校验成功!";
    if (is_verify == 0) {
        hello = "签名校验失败!";
    }
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_peak_chao_native_SignatureUtils_signatureVerify(JNIEnv *env, jobject thiz, jobject context) {
    // 1. 获取包名
    jclass j_clz = env->GetObjectClass(context);
    jmethodID j_mid = env->GetMethodID(j_clz, "getPackageName", "()Ljava/lang/String;");
    jstring j_package_name = (jstring) env->CallObjectMethod(context, j_mid);
    // 2 . 比对包名是否一样
    const char *c_package_name = env->GetStringUTFChars(j_package_name, NULL);
    if (strcmp(c_package_name, PACKAGE_NAME) != 0) {
        return;
    }
    // 3. 获取签名
    // 3.1 获取 PackageManager
    j_mid = env->GetMethodID(j_clz, "getPackageManager", "()Landroid/content/pm/PackageManager;");
    jobject pack_manager = env->CallObjectMethod(context, j_mid);
    // 3.2 获取 PackageInfo
    j_clz = env->GetObjectClass(pack_manager);
    j_mid = env->GetMethodID(j_clz, "getPackageInfo",
                             "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    //后两个代表构造函数中的参数,0x00000040看 PackageManager.GET_SIGNATURES源码就可以知道了0x00000040==PackageManager.GET_SIGNATURES
    jobject package_info = env->CallObjectMethod(pack_manager, j_mid, j_package_name, 0x00000040);
    // 3.3 获取 signatures 数组
    j_clz = env->GetObjectClass(package_info);
    jfieldID j_fid = env->GetFieldID(j_clz, "signatures", "[Landroid/content/pm/Signature;");
    jobjectArray signatures = (jobjectArray) env->GetObjectField(package_info, j_fid);
    // 3.4 获取 signatures[0]
    jobject signatures_first = env->GetObjectArrayElement(signatures, 0);
    // 3.5 调用 signatures[0].toCharsString();
    j_clz = env->GetObjectClass(signatures_first);
    j_mid = env->GetMethodID(j_clz, "toCharsString", "()Ljava/lang/String;");
    jstring j_signature_str = (jstring) env->CallObjectMethod(signatures_first, j_mid);
    const char *c_signature_str = env->GetStringUTFChars(j_signature_str, NULL);
    // 4. 比对签名是否一样
    if (strcmp(c_signature_str, APP_SIGNATURE) != 0) {
        __android_log_print(ANDROID_LOG_ERROR, "JNI_TAG", "签名校验失败: %s", c_signature_str);
        return;
    }
    __android_log_print(ANDROID_LOG_ERROR, "JNI_TAG", "签名校验成功: %s", c_signature_str);
    // 签名认证成功
    is_verify = 1;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_peak_chao_native_SignatureUtils_encrypt(JNIEnv *env, jobject thiz, jstring str_) {
    if (str_ == nullptr) return nullptr;

    const char *str = env->GetStringUTFChars(str_, JNI_FALSE);
    char *result = AES_128_CBC_PKCS5_Encrypt(str);

    env->ReleaseStringUTFChars(str_, str);

    jstring jResult = getJString(env, result);
    free(result);

    return jResult;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_peak_chao_native_SignatureUtils_decrypt(JNIEnv *env, jobject thiz, jstring str_) {
    if (str_ == nullptr) return nullptr;

    const char *str = env->GetStringUTFChars(str_, JNI_FALSE);
    char *result = AES_128_CBC_PKCS5_Decrypt(str);

    env->ReleaseStringUTFChars(str_, str);

    jstring jResult = getJString(env, result);
    free(result);

    return jResult;
}

