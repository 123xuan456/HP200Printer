#include <jni.h>
#include <string>
#include <stddef.h>
#include <android/log.h>
#include <malloc.h>
#include <cstdlib>

#include "ierrors.h"
#include "iapi.h"


#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, "hello-libs::", __VA_ARGS__))

#ifdef __cplusplus
extern "C" {
#endif


void *minst;

/* stdio functions */
static int GSDLLCALL
gsdll_stdin(void *instance, char *buf, int len)
{
    int ch;
    int count = 0;
    while (count < len) {
        ch = fgetc(stdin);
        if (ch == EOF)
            return 0;
        *buf++ = ch;
        count++;
        if (ch == '\n')
            break;
    }
    return count;
}

static int GSDLLCALL
gsdll_stdout(void *instance, const char *str, int len)
{
    LOGI("gsdll_stdout:%s", str);
    fwrite(str, 1, len, stdout);
    fflush(stdout);
    return len;
}

static int GSDLLCALL
gsdll_stderr(void *instance, const char *str, int len)
{
    LOGI("gsdll_stderr:%s", str);
    fwrite(str, 1, len, stderr);
    fflush(stderr);
    return len;
}

int main()
{
    int code, code1;
    const char * gsargv[2];
    int gsargc;
    gsargv[0] = "gs";	/* actual value doesn't matter */
    gsargv[1] = "-h";
    gsargc=2;

    code = gsapi_new_instance(&minst, NULL);
    if (code < 0)
        return 1;
    if (minst == NULL)
        return 1;

    gsapi_set_stdio(minst, gsdll_stdin, gsdll_stdout, gsdll_stderr);

    code = gsapi_set_arg_encoding(minst, GS_ARG_ENCODING_UTF8);
    if (code == 0)
        code = gsapi_init_with_args(minst, gsargc, (char **)gsargv);
    code1 = gsapi_exit(minst);
    if ((code == 0) || (code == gs_error_Quit))
        code = code1;

    gsapi_delete_instance(minst);

    if ((code == 0) || (code == gs_error_Quit))
        return 0;
    return 1;
}

int pdf2ps(const char* pdfPath, const char* psPath)
{
    char OutputFile[1024] = {0}; // 名字路径最长长度
    int code, code1;
    const char * gsargv[15];
    int gsargc;
    //TODO: 这里要判断psPath的长度;
    sprintf(OutputFile, "-sOutputFile=%s", psPath);
    gsargv[0] = "gs";	/* actual value doesn't matter */
    gsargv[1] = "-q";
    gsargv[2] = "-dNOPAUSE";
    gsargv[3] = "-dBATCH";
    gsargv[4] = "-dSAFER";
    gsargv[5] = "-sDEVICE=ps2write";
    gsargv[6] = OutputFile;
    gsargv[7] = "-dLanguageLevel";
    gsargv[8] = "-r600";
    gsargv[9] = "-dCompressFonts=false";
    gsargv[10] = "-dNoT3CCITT";
    gsargv[11] = "-dNOINTERPOLATE";
    gsargv[12] = "-c save pop";
    gsargv[13] = "-f";
    gsargv[14] = pdfPath;// "/sdcard/test.pdf";
    gsargc=15;

    code = gsapi_new_instance(&minst, NULL);
    if (code < 0)
        return 1;
    if (minst == NULL)
        return 1;

    gsapi_set_stdio(minst, gsdll_stdin, gsdll_stdout, gsdll_stderr);

    code = gsapi_set_arg_encoding(minst, GS_ARG_ENCODING_UTF8);
    if (code == 0)
        code = gsapi_init_with_args(minst, gsargc, (char **)gsargv);
    code1 = gsapi_exit(minst);
    if ((code == 0) || (code == gs_error_Quit))
        code = code1;

    gsapi_delete_instance(minst);

    if ((code == 0) || (code == gs_error_Quit))
        return 0;
    return 1;
}

int pdf2pcl3gui(const char* pdfPath, const char* psPath)
{
    char OutputFile[1024] = {0}; // 名字路径最长长度
    int code, code1;
    const char * gsargv[16];
    int gsargc;
    //TODO: 这里要判断psPath的长度;
    sprintf(OutputFile, "-sOutputFile=%s", psPath);
    gsargv[0] = "gs";	/* actual value doesn't matter */
    gsargv[1] = "-dIjsUseOutputFD";
    gsargv[2] = "-sDEVICE=pcl3";
    gsargv[3] = "-sSubdevice=unspec";
    gsargv[4] = "-sPJLLanguage=PCL3GUI";
    gsargv[5] = "-dOnlyCRD";
    gsargv[6] = OutputFile;
    gsargv[7] = "-r600";
    gsargv[8] = "-sColourModel=CMYK";
    gsargv[9] = "-sPrintQuality=draft";
    gsargv[10] = "-sMedium=plain";
    gsargv[11] = "-sPAPERSIZE=a4";
    gsargv[12] = "-dNOPAUSE";
    gsargv[13] = "-dSAFER";
    gsargv[14] = pdfPath;// "/sdcard/test.pdf";
    gsargv[15] = "-c quit";
    gsargc=16;

    code = gsapi_new_instance(&minst, NULL);
    if (code < 0)
        return 1;
    if (minst == NULL)
        return 1;

    gsapi_set_stdio(minst, gsdll_stdin, gsdll_stdout, gsdll_stderr);

    code = gsapi_set_arg_encoding(minst, GS_ARG_ENCODING_UTF8);
    if (code == 0)
        code = gsapi_init_with_args(minst, gsargc, (char **)gsargv);
    code1 = gsapi_exit(minst);
    if ((code == 0) || (code == gs_error_Quit))
        code = code1;

    gsapi_delete_instance(minst);

    if ((code == 0) || (code == gs_error_Quit))
        return 0;
    return 1;
}

int pdf2pcl3gui3(int pageNumber, const char* pdfPath, const char* psPath)
{
    char OutputFile[1024] = {0}; // 名字路径最长长度
    char firstPage[20] = {0};
    char lastPage[20] = {0};
    int code, code1;
    const char * gsargv[18];
    int gsargc;
    //TODO: 这里要判断psPath的长度;
    sprintf(OutputFile, "-sOutputFile=%s", psPath);
    sprintf(firstPage, "-dFirstPage=%d", pageNumber);
    sprintf(lastPage, "-dLastPage=%d", pageNumber);
    gsargv[0] = "gs";	/* actual value doesn't matter */
    gsargv[1] = "-dIjsUseOutputFD";
    gsargv[2] = "-sDEVICE=pcl3";
    gsargv[3] = "-sSubdevice=unspec";
    gsargv[4] = "-sPJLLanguage=PCL3GUI";
    gsargv[5] = "-dOnlyCRD";
    gsargv[6] = OutputFile;
    gsargv[7] = "-r600";
    gsargv[8] = "-sColourModel=CMYK";
    gsargv[9] = "-sPrintQuality=draft";
    gsargv[10] = "-sMedium=plain";
    gsargv[11] = "-sPAPERSIZE=a4";
    gsargv[12] = "-dNOPAUSE";
    gsargv[13] = "-dSAFER";
    gsargv[14] = firstPage;
    gsargv[15] = lastPage;
    gsargv[16] = pdfPath;// "/sdcard/test.pdf";
    gsargv[17] = "-c quit";
    gsargc=18;

    code = gsapi_new_instance(&minst, NULL);
    if (code < 0)
        return 1;
    if (minst == NULL)
        return 1;

    gsapi_set_stdio(minst, gsdll_stdin, gsdll_stdout, gsdll_stderr);

    code = gsapi_set_arg_encoding(minst, GS_ARG_ENCODING_UTF8);
    if (code == 0)
        code = gsapi_init_with_args(minst, gsargc, (char **)gsargv);
    code1 = gsapi_exit(minst);
    if ((code == 0) || (code == gs_error_Quit))
        code = code1;

    gsapi_delete_instance(minst);

    if ((code == 0) || (code == gs_error_Quit))
        return 0;
    return 1;
}


int pdf2pcl3gui2(const char* pdfPath, const char* psPath)
{
    char OutputFile[1024] = {0}; // 名字路径最长长度
    int code, code1;
    const char * gsargv[19];
    int gsargc;
    //TODO: 这里要判断psPath的长度;
    // TODO 注意修改路径
    sprintf(OutputFile, "-sOutputFile=%s", "/data/data/com.kangear.mtm/cache/fuck.bin");
    gsargv[0] = "gs";	/* actual value doesn't matter */
    gsargv[1] = "-q";
    gsargv[2] = "-dBATCH";
    gsargv[3] = "-dPARANOIDSAFER";
    gsargv[4] = "-dQUIET";
    gsargv[5] = "-dNOPAUSE";
    gsargv[6] = "-dNOINTERPOLATE";
    gsargv[7] = "-sDEVICE=pcl3";
    gsargv[8] = "-sSubdevice=hpdj";
    gsargv[9] = "-r300x300";
    gsargv[10] = "-dDEVICEWIDTHPOINTS=595";
    gsargv[11] = "-dDEVICEHEIGHTPOINTS=842";
    gsargv[12] = "-sMedium=0";
    gsargv[13] = "-dMediaPosition=1";
    gsargv[14] = "-sIntensityRendering=halftones";
    gsargv[15] = "-dDITHERPPI=60";
    gsargv[16] = "-dMaxBitmap=8388608";
    // TODO 注意修改路径
    gsargv[17] = "-sOutputFile=/data/data/com.kangear.mtm/cache/fuck.bin";
    gsargv[18] = pdfPath;// "/sdcard/test.pdf";
    gsargc=19;

    code = gsapi_new_instance(&minst, NULL);
    if (code < 0)
        return 1;
    if (minst == NULL)
        return 1;

    gsapi_set_stdio(minst, gsdll_stdin, gsdll_stdout, gsdll_stderr);

    code = gsapi_set_arg_encoding(minst, GS_ARG_ENCODING_UTF8);
    if (code == 0)
        code = gsapi_init_with_args(minst, gsargc, (char **)gsargv);
    code1 = gsapi_exit(minst);
    if ((code == 0) || (code == gs_error_Quit))
        code = code1;

    gsapi_delete_instance(minst);

    if ((code == 0) || (code == gs_error_Quit))
        return 0;
    return 1;
}


#ifdef __cplusplus
}
#endif
// TODO 注意修改路径
extern "C" JNIEXPORT jstring JNICALL
Java_com_kangear_mtm_Jni_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
//    int ret = main();

    char * p;
    if((p = getenv("USER")))
        LOGI("USER =%sn",p);
    setenv("USER","test", 1);
    LOGI("USER=%s\n", getenv("USE"));
    unsetenv("USER");
    LOGI("USER=%s\n",getenv("USER"));

//    LOGI("FUCK RET=%d\n", ret);
    return env->NewStringUTF(hello.c_str());
}

// TODO 注意修改路径
extern "C" JNIEXPORT jstring JNICALL
Java_com_kangear_mtm_Jni_pdf2ps(
        JNIEnv *env,
        jobject /* this */,
        jstring pdfPath,
        jstring psPath) {
    std::string hello = "Hello from C++";
    const char *pdfPathChar = env->GetStringUTFChars(pdfPath, JNI_FALSE);
    const char *psPathChar = env->GetStringUTFChars(psPath, JNI_FALSE);
    int ret = pdf2ps(pdfPathChar, psPathChar);
    LOGI("FUCK RET=%d\n", ret);
    env->ReleaseStringUTFChars(pdfPath, pdfPathChar);
    env->ReleaseStringUTFChars(psPath, psPathChar);
    return env->NewStringUTF(hello.c_str());
}
// TODO 注意修改路径
extern "C" JNIEXPORT jstring JNICALL
Java_com_kangear_mtm_Jni_pdf2pcl3gui(
        JNIEnv *env,
        jobject /* this */,
        jstring pdfPath,
        jstring psPath) {
    std::string hello = "Hello from C++";
    const char *pdfPathChar = env->GetStringUTFChars(pdfPath, JNI_FALSE);
    const char *psPathChar = env->GetStringUTFChars(psPath, JNI_FALSE);
    int ret = pdf2pcl3gui(pdfPathChar, psPathChar);
    LOGI("FUCK RET=%d\n", ret);
    env->ReleaseStringUTFChars(pdfPath, pdfPathChar);
    env->ReleaseStringUTFChars(psPath, psPathChar);
    return env->NewStringUTF(hello.c_str());
}

// TODO 注意修改路径
extern "C" JNIEXPORT jstring JNICALL
Java_com_kangear_mtm_Jni_pdf2pcl3guiSinglePage(
        JNIEnv *env,
        jobject /* this */,
        jstring pdfPath,
        jstring psPath,
        jint pageNumber) {
    std::string hello = "Hello from C++";
    const char *pdfPathChar = env->GetStringUTFChars(pdfPath, JNI_FALSE);
    const char *psPathChar = env->GetStringUTFChars(psPath, JNI_FALSE);
    int ret = pdf2pcl3gui3((int)pageNumber, pdfPathChar, psPathChar);
    LOGI("FUCK RET=%d\n", ret);
    env->ReleaseStringUTFChars(pdfPath, pdfPathChar);
    env->ReleaseStringUTFChars(psPath, psPathChar);
    return env->NewStringUTF(hello.c_str());
}

