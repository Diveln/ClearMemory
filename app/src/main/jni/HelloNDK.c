#include <com_david_ndkdemo_MainActivity.h>

JNIEXPORT jstring JNICALL Java_com_david_ndkdemo_MainActivity_getStringFromNative
(JNIEnv * evn, jobject obj)
{
return (*evn)->NewStringUTF(evn,"Hello NKD demo");
}