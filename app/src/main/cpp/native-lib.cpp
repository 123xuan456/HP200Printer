#include <jni.h>
#include <string>
#include "ierrors.h"
#include "iapi.h"
#include <stddef.h>

void *minst;

int main()
{
    int code, code1;
    const char * gsargv[15];
    int gsargc;
    gsargv[0] = "gs";	/* actual value doesn't matter */
    gsargv[1] = "-q";
    gsargv[2] = "-dNOPAUSE";
    gsargv[3] = "-dBATCH";
    gsargv[4] = "-dSAFER";
    gsargv[5] = "-sDEVICE=ps2write";
    gsargv[6] = "-sOutputFile=out.ps";
    gsargv[7] = "-dLanguageLevel";
    gsargv[8] = "-r600";
    gsargv[9] = "-dCompressFonts=false";
    gsargv[10] = "-dNoT3CCITT";
    gsargv[11] = "-dNOINTERPOLATE";
    gsargv[12] = "-c save pop";
    gsargv[13] = "-f";
    gsargv[14] = "HelloWorld.pdf";
    gsargc=15;

    code = gsapi_new_instance(&minst, NULL);
    if (code < 0)
	return 1;
    code = gsapi_set_arg_encoding(minst, GS_ARG_ENCODING_UTF8);
    if (code == 0)
        code = gsapi_init_with_args(minst, gsargc, gsargv);
    code1 = gsapi_exit(minst);
    if ((code == 0) || (code == gs_error_Quit))
	code = code1;

    gsapi_delete_instance(minst);

    if ((code == 0) || (code == gs_error_Quit))
	return 0;
    return 1;
}
// TODO 注意修改路径
extern "C" JNIEXPORT jstring JNICALL
Java_com_kangear_mtm_MainActivity1_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    main();
    return env->NewStringUTF(hello.c_str());
}
