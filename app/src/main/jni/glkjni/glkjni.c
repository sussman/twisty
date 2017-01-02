/* This file is a part of GlkJNI.
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

#ifdef ANDROID
#include <malloc.h>
#include <setjmp.h>
#include <android/log.h>
#endif

#include <stdlib.h>
#include <stdio.h>
#include <jni.h>
#include "glk.h"
#include "glkjni.h"
#include "jcall.h"
#include "gi_dispa.h"
#include "gi_blorb.h"

gidispatch_rock_t (*gli_register_obj)(void *obj, glui32 objclass) = NULL;
void (*gli_unregister_obj)(void *obj, glui32 objclass,
    gidispatch_rock_t objrock) = NULL;
gidispatch_rock_t (*gli_register_arr)(void *array, glui32 len,
    char *typecode) = NULL;
void (*gli_unregister_arr)(void *array, glui32 len, char *typecode,
    gidispatch_rock_t objrock) = NULL;

static giblorb_map_t *blorbmap = NULL;

void glkjni_c_shutdown(void)
{
    gli_register_obj = NULL;
    gli_unregister_obj = NULL;
    gli_register_arr = NULL;
    gli_unregister_arr = NULL;
    giblorb_destroy_map(blorbmap);
}

#ifdef ANDROID

jmp_buf jump_error;

void gli_exit()
{
    longjmp(jump_error, JMP_WHOOPS);
}

void gli_interrupted()
{
    longjmp(jump_error, JMP_INT);
}

void gli_fatal(char *msg)
{
    __android_log_write(ANDROID_LOG_ERROR, "glk", msg);
    gli_exit();
}

#else

void gli_exit()
{
    exit(EXIT_FAILURE);
}

void gli_interrupted()
{
    exit(EXIT_FAILURE);
}

void gli_fatal(char *msg)
{
    fputs(msg, stderr);
    gli_exit();
}

#endif

void *gli_malloc(size_t size)
{
    void *value = malloc(size);
    if (!value) {
        gli_fatal("GlkJNI: virtual memory exhausted\n");
    }
    return value;
}

void gidispatch_set_object_registry(
    gidispatch_rock_t (*regi)(void *obj, glui32 objclass),
    void (*unregi)(void *obj, glui32 objclass, gidispatch_rock_t objrock))
{
    winid_t win;
    strid_t str;
    frefid_t fref;
    schanid_t schan;

    gli_register_obj = regi;
    gli_unregister_obj = unregi;

    if (gli_register_obj) {
        /* It's now necessary to go through all existing objects, and register
            them. */
        for (win = glk_window_iterate(NULL, NULL);
            win;
            win = glk_window_iterate(win, NULL)) {
            gli_win_set_disprock(win);
        }
        for (str = glk_stream_iterate(NULL, NULL);
            str;
            str = glk_stream_iterate(str, NULL)) {
            gli_str_set_disprock(str);
        }
        for (fref = glk_fileref_iterate(NULL, NULL);
            fref;
            fref = glk_fileref_iterate(fref, NULL)) {
            gli_fref_set_disprock(fref);
        }
        for (schan = glk_schannel_iterate(NULL, NULL);
            schan;
            schan = glk_schannel_iterate(schan, NULL)) {
            gli_schan_set_disprock(schan);
        }
    }
}

void gidispatch_set_retained_registry(
    gidispatch_rock_t (*regi)(void *array, glui32 len, char *typecode),
    void (*unregi)(void *array, glui32 len, char *typecode,
        gidispatch_rock_t objrock))
{
    gli_register_arr = regi;
    gli_unregister_arr = unregi;
}

gidispatch_rock_t gidispatch_get_objrock(void *obj, glui32 objclass)
{
    switch (objclass) {
        case gidisp_Class_Window:
            return gli_win_get_disprock(obj);
        case gidisp_Class_Stream:
            return gli_str_get_disprock(obj);
        case gidisp_Class_Fileref:
            return gli_fref_get_disprock(obj);
        case gidisp_Class_Schannel:
            return gli_schan_get_disprock(obj);
        default: {
            gidispatch_rock_t dummy;
            dummy.num = 0;
            return dummy;
        }
    }
}

giblorb_err_t giblorb_set_resource_map(strid_t file)
{
    giblorb_err_t err;

    err = giblorb_create_map(file, &blorbmap);
    if (err) {
        blorbmap = NULL;
        return err;
    }

    return giblorb_err_None;
}

giblorb_map_t *giblorb_get_resource_map()
{
    return blorbmap;
}

jobject glkjni_get_blorb_resource(glui32 usage, glui32 resnum)
{
	giblorb_err_t err;
	giblorb_map_t *map;
	giblorb_result_t result;
	jobject jres;

	map = giblorb_get_resource_map();
	if (!map) {
		return NULL;
	}

	err = giblorb_load_resource(map, giblorb_method_FilePos, &result, usage,
            resnum);
	if (err != giblorb_err_None) {
	    return NULL;
	}

	jres = (*jni_env)->NewObject(STATIC_M(BLORBRES, NEW),
	        (jint)resnum, (jint)(result.data.startpos),
	        (jint)(result.length), (jint)(result.chunktype));
	if (jni_check_exc()) {
	    return NULL;
	}

	return jres;
}

void glkjni_set_story_path(char *storypath)
{
    jstring jpath;
    jobject jfile;
    jboolean exists;
    jfieldID fid;

    jpath = jni_jstrfromnative(storypath);
    if (!jpath) {
        return;
    }

    jfile = (*jni_env)->NewObject(STATIC_M(FILE, FROMSTRING), jpath);
    DELETE_LOCAL(jpath);
    if (jni_check_exc()) {
        return;
    }

    exists = (*jni_env)->CallBooleanMethod(INSTANCE_M(jfile, FILE_EXISTS));
    if (jni_check_exc()) {
        DELETE_LOCAL(jfile);
        return;
    }

    jpath = (*jni_env)->CallObjectMethod(
            INSTANCE_M(jfile, FILE_GETABSPATH));
    DELETE_LOCAL(jfile);
    if (jni_check_exc()) {
        DELETE_LOCAL(jpath);
        return;
    }

    fid = (*jni_env)->GetStaticFieldID(jni_env, JNI_CLASS(GLKFACTORY),
            "storyPath", "Ljava/lang/String;");
    if (!fid) {
        DELETE_LOCAL(jpath);
        return;
    }

    (*jni_env)->SetStaticObjectField(jni_env, JNI_CLASS(GLKFACTORY),
            fid, jpath);
    DELETE_LOCAL(jpath);
}
