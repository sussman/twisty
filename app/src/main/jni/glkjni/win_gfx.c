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

#include <stdio.h>
#include <jni.h>
#include "glk.h"
#include "glkjni.h"
#include "gi_blorb.h"
#include "jcall.h"
#include "window.h"

glui32 glk_image_get_info(glui32 image, glui32 *width, glui32 *height)
{
    jboolean res = FALSE;
    jintArray jdim;
    jint *dim = NULL;
    int wid = 0;
    int hgt = 0;
    jobject jres;

    if ((jint)image < 0) {
        gli_strict_warning("image_get_info: resource number too large");
        return FALSE;
    }

    jres = glkjni_get_blorb_resource(giblorb_ID_Pict, image);
    if (!jres) {
        return FALSE;
    }

    jdim = (*jni_env)->NewIntArray(jni_env, 2);
    if (!jdim) {
        jni_no_mem();
    }

    res = (*jni_env)->CallBooleanMethod(GLK_M(IMAGEINFO),
            jres, jdim);
    if (jni_check_exc()) {
        goto done;
    }

    dim = (*jni_env)->GetIntArrayElements(jni_env, jdim, NULL);
    if (!dim) {
        goto done;
    }
    wid = dim[0];
    hgt = dim[1];
    (*jni_env)->ReleaseIntArrayElements(jni_env, jdim, dim, JNI_ABORT);

done:
    DELETE_LOCAL(jres);
    DELETE_LOCAL(jdim);
    if (width) {
        *width = wid;
    }
    if (height) {
        *height = hgt;
    }
    return res;
}

glui32 glk_image_draw(window_t *win, glui32 image, glsi32 val1,
        glsi32 val2)
{
    jboolean res;
    jobject jres;

    if ((jint)image < 0) {
        gli_strict_warning("image_draw: resource number too large");
        return FALSE;
    }

    jres = glkjni_get_blorb_resource(giblorb_ID_Pict, image);
    if (!jres) {
        return FALSE;
    }

    switch (win->type) {
    case wintype_TextBuffer:
        if (val1 < imagealign_InlineUp || val1 > imagealign_MarginRight) {
            gli_strict_warning("image_draw: invalid alignment");
            DELETE_LOCAL(jres);
            return FALSE;
        }
        res = (*jni_env)->CallBooleanMethod(
                WIN_M(win->jwin, DRAWINLINE),
                jres, (jint)val1);
        break;
    case wintype_Graphics:
        res = (*jni_env)->CallBooleanMethod(
                WIN_M(win->jwin, DRAW),
                jres, (jint)val1, (jint)val2);
        break;
    default:
        res = FALSE;
        break;
    }

    if (jni_check_exc()) {
        res = FALSE;
    }
    DELETE_LOCAL(jres);

    return res;
}

glui32 glk_image_draw_scaled(window_t *win, glui32 image,
    glsi32 val1, glsi32 val2, glui32 width, glui32 height)
{
    jboolean res;
    jobject jres;

    if ((jint)image < 0) {
        gli_strict_warning("image_draw_scaled: resource number too large");
        return FALSE;
    }
    if (width == 0 || height == 0) {
        return FALSE;
    }

    jres = glkjni_get_blorb_resource(giblorb_ID_Pict, image);
    if (!jres) {
        return FALSE;
    }

    switch (win->type) {
    case wintype_TextBuffer:
        if (val1 < imagealign_InlineUp || val1 > imagealign_MarginRight) {
            gli_strict_warning("image_draw: invalid alignment");
            DELETE_LOCAL(jres);
            return FALSE;
        }
        res = (*jni_env)->CallBooleanMethod(
                WIN_M(win->jwin, DRAWINLINESCALED),
                jres, (jint)val1, (jint)width, (jint)height);
        break;
    case wintype_Graphics:
        res = (*jni_env)->CallBooleanMethod(
                WIN_M(win->jwin, DRAWSCALED),
                jres, (jint)val1, (jint)val2,
                (jint)width, (jint)height);
        break;
    default:
        res = FALSE;
        break;
    }

    if (jni_check_exc()) {
        res = FALSE;
    }
    DELETE_LOCAL(jres);

    return res;
}

void glk_window_flow_break(window_t *win)
{
    if (win->type != wintype_TextBuffer) {
        return;
    }
    (*jni_env)->CallVoidMethod(WIN_M(win->jwin, FLOWBREAK));
    jni_check_exc();
}

void glk_window_erase_rect(window_t *win,
    glsi32 left, glsi32 top, glui32 width, glui32 height)
{
    if (win->type != wintype_Graphics) {
        return;
    }
    if (width == 0 || height == 0) {
        return;
    }

    (*jni_env)->CallVoidMethod(WIN_M(win->jwin, ERASERECT),
            (jint)left, (jint)top, (jint)width, (jint)height);
    jni_check_exc();
}

void glk_window_fill_rect(window_t *win, glui32 color,
    glsi32 left, glsi32 top, glui32 width, glui32 height)
{
    if (win->type != wintype_Graphics) {
        return;
    }
    if (width == 0 || height == 0) {
        return;
    }
    if (color & 0xFF000000) {
        gli_strict_warning("window_fill_rect: invalid color");
        return;
    }

    (*jni_env)->CallVoidMethod(WIN_M(win->jwin, FILLRECT),
            (jint)color, (jint)left, (jint)top, (jint)width, (jint)height);
    jni_check_exc();
}

void glk_window_set_background_color(window_t *win, glui32 color)
{
    if (win->type != wintype_Graphics) {
        return;
    }
    if (color & 0xFF000000) {
        gli_strict_warning("window_set_background_color: invalid color");
        return;
    }

    (*jni_env)->CallVoidMethod(WIN_M(win->jwin, SETBG), (jint)color);
    jni_check_exc();
}
