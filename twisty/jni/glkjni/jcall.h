/* This file is part of GlkJNI.
 * Copyright (c) 2009 Edward McCardell
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

#ifndef JCALL_H_
#define JCALL_H_

#include <jni.h>

void jni_jcall_init(char *glkpackage);
void jni_init_glk(int argc, char **argv);
void jni_exit_on_exc(void);
int jni_check_exc(void);
int jni_check_for_exc(int class_id);
void jni_no_mem(void);
jobject jni_new_global(jobject localref);
jobject jni_newbytebuffer(void *buf, jlong len);
jstring jni_jstrfromnative(char *str);
char *jni_file_getpath(jobject file);

typedef struct classcache_t {
    int id;
    char *name;
    jclass class;
} classcache_t;

enum ClassID {
    GLK_CLASS = 0,
    GLKFACTORY_CLASS,
    GLKWINDOW_CLASS,
    GLKCHANNEL_CLASS,
    BLORBRES_CLASS,
    ERROR_CLASS,
    UOE_CLASS,
    STRING_CLASS,
    FILE_CLASS,
    BUFFER_CLASS,
    BYTEBUFFER_CLASS,
    THREAD_CLASS,
    MAX_CLASS_ID
};

#define MAX_GLK_CLASS BLORBRES_CLASS

extern classcache_t jni_ccache[];

typedef struct methodcache_t {
    int class_id;
    int id;
    char *name;
    char *sig;
    jmethodID mid;
} methodcache_t;

extern methodcache_t jni_mcache[];

enum SMethodID {
#ifndef ANDROID
    GLKFACTORY_STARTUP_METHOD = 0,
    FILE_CREATETEMP_METHOD,
#else
    FILE_CREATETEMP_METHOD = 0,
#endif
    THREAD_INTERRUPTED_METHOD,
    MAX_SMETHOD_ID
};

enum IMethodID {
#ifndef ANDROID
    GLK_EXIT_METHOD = MAX_SMETHOD_ID,
    GLK_GESTALT_METHOD,
#else
    GLK_GESTALT_METHOD = MAX_SMETHOD_ID,
#endif
    GLK_WINDOWOPEN_METHOD,
    GLK_WINDOWCLOSE_METHOD,
    GLK_SETHINT_METHOD,
    GLK_CLEARHINT_METHOD,
    GLK_NAMEDFILE_METHOD,
    GLK_PROMPTFILE_METHOD,
    GLK_REQUESTTIMER_METHOD,
    GLK_CANCELTIMER_METHOD,
    GLK_IMAGEINFO_METHOD,
    GLK_CREATECHAN_METHOD,
    GLK_CHANNELDESTROY_METHOD,
    GLK_SOUNDHINT_METHOD,
    GLK_SELECT_METHOD,
    GLK_POLL_METHOD,

    GLKWINDOW_PRINT_METHOD,
    GLKWINDOW_STYLE_METHOD,
    GLKWINDOW_DISTINGUISH_METHOD,
    GLKWINDOW_MEASURESTYLE_METHOD,
    GLKWINDOW_CLEAR_METHOD,
    GLKWINDOW_CURSOR_METHOD,
    GLKWINDOW_SIZE_METHOD,
    GLKWINDOW_ARRANGE_METHOD,
    GLKWINDOW_REQUESTCHAR_METHOD,
    GLKWINDOW_CANCELCHAR_METHOD,
    GLKWINDOW_REQUESTLINE_METHOD,
    GLKWINDOW_REQUESTLINEUNI_METHOD,
    GLKWINDOW_CANCELLINE_METHOD,
    GLKWINDOW_REQUESTMOUSE_METHOD,
    GLKWINDOW_CANCELMOUSE_METHOD,
    GLKWINDOW_SETLINK_METHOD,
    GLKWINDOW_REQUESTLINK_METHOD,
    GLKWINDOW_CANCELLINK_METHOD,
    GLKWINDOW_DRAWINLINE_METHOD,
    GLKWINDOW_DRAWINLINESCALED_METHOD,
    GLKWINDOW_FLOWBREAK_METHOD,
    GLKWINDOW_DRAW_METHOD,
    GLKWINDOW_DRAWSCALED_METHOD,
    GLKWINDOW_SETBG_METHOD,
    GLKWINDOW_ERASERECT_METHOD,
    GLKWINDOW_FILLRECT_METHOD,

    GLKCHANNEL_VOLUME_METHOD,
    GLKCHANNEL_PLAY_METHOD,
    GLKCHANNEL_STOP_METHOD,

    BLORBRES_NEW_METHOD,

    STRING_FROMNATIVE_METHOD,
    STRING_FROMLATIN1_METHOD,
    STRING_GETBYTES_METHOD,
    FILE_FROMSTRING_METHOD,
    FILE_GETPATH_METHOD,
    FILE_GETABSPATH_METHOD,
    FILE_DELETEONEXIT_METHOD,
    FILE_DELETE_METHOD,
    FILE_EXISTS_METHOD,
    BYTEBUFFER_ASINTBUF_METHOD,

    MAX_IMETHOD_ID
};

extern JNIEnv *jni_env;

extern jobject glkobj;

#define DELETE_LOCAL(ref) \
        ((*jni_env)->DeleteLocalRef(jni_env, ref))

#define DELETE_GLOBAL(ref) \
        ((*jni_env)->DeleteGlobalRef(jni_env, ref))

#define JNI_CLASS(c) \
    (jni_ccache[c ## _CLASS].class)

#define INSTANCE_OF(obj, c) \
    ((*jni_env)->IsInstanceOf(jni_env, obj, jni_ccache[c].class))

#define GLK_M(m) \
    jni_env, glkobj, jni_mcache[GLK_ ## m ## _METHOD].mid

#define WIN_M(w, m) \
    jni_env, w, jni_mcache[GLKWINDOW_ ## m ## _METHOD].mid

#define STATIC_M(c, m) \
    jni_env, JNI_CLASS(c), jni_mcache[c ## _ ## m ## _METHOD].mid

#define INSTANCE_M(obj, cm) \
    jni_env, obj, jni_mcache[cm ## _METHOD].mid

#endif /* JCALL_H_ */
