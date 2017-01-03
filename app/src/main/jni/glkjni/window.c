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
#include "jcall.h"
#include "window.h"

/* The internal id counter for the next window. */
static glui32 gli_windowid = 1;

/* Linked list of all windows */
static window_t *gli_windowlist = NULL;

/* The topmost window. */
window_t *gli_rootwin = NULL;

static void gli_window_delete(window_t *win)
{
    DELETE_GLOBAL(win->jwin);
    win->jwin = NULL;
    win->key = NULL;

    if (win->text) {
        free(win->text->outbuf);
        free(win->text);
        win->text = NULL;
    }
}

void window_c_shutdown(void)
{
    window_t *curr, *next;

    curr = gli_windowlist;

    while (curr) {
        next = curr->next;
        gli_window_delete(curr);
        free(curr);
        curr = next;
    }

    gli_windowlist = NULL;
    gli_rootwin = NULL;
    gli_windowid = 1;
}

/* Tests that WINTYPE represents a text window. */
int gli_text_wintype(glui32 wintype)
{
    switch (wintype) {
    case wintype_TextBuffer:
    case wintype_TextGrid:
        return TRUE;

    default:
        return FALSE;
    }
}

/* Tests that WINTYPE represents an output window. */
static int gli_output_wintype(glui32 wintype)
{
    switch (wintype) {
    case wintype_TextBuffer:
    case wintype_TextGrid:
    case wintype_Graphics:
        return TRUE;

    default:
        return FALSE;
    }
}

/* Returns the dispatch rock for WIN. */
gidispatch_rock_t gli_win_get_disprock(window_t *win)
{
    return win->disprock;
}

/* Registers WIN with the dispatch layer. */
void gli_win_set_disprock(window_t *win)
{
    if (gli_register_obj) {
        win->disprock = (*gli_register_obj)(win, gidisp_Class_Window);
    } else {
        win->disprock.ptr = NULL;
    }
}

/*
 * Clears the pending event status of WIN after an input event. Returns
 * true if the event was of a requested type.
 */
int gli_process_window_event(window_t *win, glui32 type, glui32 val1)
{
    /*
     * For the most part, we trust the frontend to only generate events
     * that have been requested.
     *
     * The tests in this method which check for this are intended for the
     * following sequence of events:
     *
     * - frontend UI thread records an input event
     * - interpreter thread cancels that kind of event
     * - interpreter thread calls glk_select/glk_select_poll
     *
     * Maybe my notions of how a frontend might be designed with separate
     * UI and interpreter threads is wrong, but it seems to me that
     * no matter how carefully interaction between the two threads is
     * synchronized, there is no way for the UI thread, while recording
     * a valid input event, to see the future and know that kind of event
     * will be canceled by the interpreter thread prior to the next call to
     * glk_select.
     */
    switch (type) {
    case evtype_MouseInput:
        if (!win->mouse_request) {
            return 0;
        }
        win->mouse_request = FALSE;
        break;
    case evtype_CharInput:
        if (win->text) {
            if (!(win->text->kb_request & KB_CHAR_REQ)) {
                return 0;
            }
            win->text->kb_request = 0;
        }
        break;
    case evtype_LineInput:
        if (win->text) {
            if (!(win->text->kb_request & KB_LINE_REQ)) {
                return 0;
            }
            win->text->kb_request = 0;
            gli_echo_line_input(win, val1);
            gli_unregister_win_input(win);
        }
        break;
    case evtype_Hyperlink:
        if (win->text) {
            if (!win->text->link_request) {
                return 0;
            }
            win->text->link_request = FALSE;
        }
        break;
    default:
        break;
    }

    return 1;
}

void glk_set_window(window_t *win)
{
    if (!win) {
        gli_stream_set_current(NULL);
    }
    else {
        gli_stream_set_current(win->str);
    }
}

/* Flushes all window streams. */
void gli_windows_print()
{
    window_t *win;

    for (win = gli_windowlist; win; win = win->next) {
        if (win->text && win->text->outbuf_count) {
            gli_window_print(win);
        }
    }
}

winid_t glk_window_iterate(winid_t win, glui32 *rock)
{
    if (!win) {
        win = gli_windowlist;
    } else {
        win = win->next;
    }

    if (win) {
        if (rock) {
            *rock = win->rock;
        }
        return win;
    }

    if (rock) {
        *rock = 0;
    }
    return NULL;
}

winid_t glk_window_get_root()
{
    if (!gli_rootwin) {
        return NULL;
    }
    return gli_rootwin;
}

static window_t *gli_register_window(glui32 type, glui32 rock,
        jobject jwin)
{
    window_t *win = (window_t *)gli_malloc(sizeof(window_t));

    win->rock = rock;
    win->id = gli_windowid;
    gli_windowid += 1;
    win->type = type;
    win->parent = NULL;
    win->str = gli_stream_register(strtype_Window, FALSE, TRUE, 0, win);
    win->echostr = NULL;
    win->key = NULL;

    gli_win_set_disprock(win);

    win->prev = NULL;
    win->next = gli_windowlist;
    gli_windowlist = win;
    if (win->next) {
        win->next->prev = win;
    }

    win->jwin = (*jni_env)->NewGlobalRef(jni_env, jwin);
    if (!win->jwin) {
        jni_no_mem();
    }
    DELETE_LOCAL(jwin);

    win->mouse_request = FALSE;
    win->text = NULL;

    win->deletemark = FALSE;

    return win;
}

static void gli_unregister_window(window_t *win)
{
    window_t *prev, *next;

    if (win->str) {
        gli_stream_unregister(win->str);
        win->str = NULL;
    }
    win->echostr = NULL;

    if (gli_unregister_obj) {
        (*gli_unregister_obj)(win, gidisp_Class_Window, win->disprock);
    }

    prev = win->prev;
    next = win->next;
    win->prev = NULL;
    win->next = NULL;

    if (prev) {
        prev->next = next;
    } else {
        gli_windowlist = next;
    }
    if (next) {
        next->prev = prev;
    }

    gli_window_delete(win);

    free(win);
}

/*
 * Ensures that:
 * - splitwin and gli_rootwin are both null or both non-null;
 * - wintype is a type that can be opened;
 * - and method is a valid combination of winmethod_* constants.
 */
static int gli_window_open_validate(winid_t splitwin, glui32 method,
        glui32 wintype)
{
    switch (wintype) {
    /* We can open these types of windows. */
    case wintype_Blank:
    case wintype_TextGrid:
    case wintype_TextBuffer:
    case wintype_Graphics:
        break;

    /* We can't open a pair window. */
    case wintype_Pair:
        gli_strict_warning("window_open: cannot open pair window directly");
        return FALSE;

    /* We don't try to open unknown window types. */
    default:
        return FALSE;
    }

    if (!gli_rootwin) {
        if (splitwin) {
            gli_strict_warning("window_open: ref must be NULL");
            return FALSE;
        }
    } else {
        glui32 val;
        window_t *oldparent;

        if (!splitwin) {
            gli_strict_warning("window_open: ref must not be NULL");
            return FALSE;
        }

        val = (method & winmethod_DivisionMask);
        if (val != winmethod_Fixed && val != winmethod_Proportional) {
            gli_strict_warning("window_open: invalid method (not fixed or proportional)");
            return FALSE;
        }

        val = (method & winmethod_DirMask);
        if (val != winmethod_Above && val != winmethod_Below
            && val != winmethod_Left && val != winmethod_Right) {
            gli_strict_warning("window_open: invalid method (bad direction)");
            return FALSE;
        }

        oldparent = splitwin->parent;
        if (oldparent && oldparent->type != wintype_Pair) {
            gli_strict_warning("window_open: parent window is not Pair");
            return FALSE;
        }
    }

    return TRUE;
}

/*
 * Calls the Java layer to open a window and get references to the
 * Java objects for the newly created window and pair window.
 */
static int jni_open_window(winid_t splitwin, glui32 method, jint size,
        glui32 wintype, jobject *jnewwin, jobject *jpairwin)
{
    jobject jsplitwin = (gli_rootwin ? splitwin->jwin : NULL);
    jobjectArray jwins;

    jwins = (*jni_env)->NewObjectArray(
            jni_env, 2, JNI_CLASS(GLKWINDOW), NULL);
    if (!jwins) {
        jni_no_mem();
    }

    if ((method & winmethod_DivisionMask) == winmethod_Proportional) {
        if (size > 100) {
            size = 100;
        }
    }

    (*jni_env)->CallVoidMethod(
            GLK_M(WINDOWOPEN), jsplitwin, (jint)method, size,
            (jint)wintype, (jint)gli_windowid, jwins);
    jni_exit_on_exc();

    *jnewwin = (*jni_env)->GetObjectArrayElement(jni_env, jwins, 0);
    if (!(*jnewwin)) {
        goto whoops2;
    }
    if (splitwin) {
        *jpairwin = (*jni_env)->GetObjectArrayElement(jni_env, jwins, 1);
        if (!(*jpairwin)) {
            goto whoops1;
        }
    } else {
        *jpairwin = NULL;
    }

    DELETE_LOCAL(jwins);
    return TRUE;

whoops1:
    DELETE_LOCAL(*jnewwin);
whoops2:
    DELETE_LOCAL(jwins);
    return FALSE;
}

/*
 * Called after the Java layer has opened a window, to update the
 * C window structures.
 */
static winid_t gli_window_open(winid_t splitwin, glui32 method, jint size,
        glui32 wintype, glui32 rock, jobject jnewwin, jobject jpairwin)
{
    window_t *newwin, *pairwin;

    newwin = gli_register_window(wintype, rock, jnewwin);
    if (gli_text_wintype(wintype)) {
        gli_textwin_init(newwin);
    }

    if (!splitwin) {
        gli_rootwin = newwin;
    } else {
        window_t *oldparent;

        pairwin = gli_register_window(wintype_Pair, 0, jpairwin);
        pairwin->key = newwin;
        pairwin->split_method = method;
        pairwin->constraint = size;

        oldparent = splitwin->parent;
        splitwin->parent = pairwin;
        newwin->parent = pairwin;
        pairwin->parent = oldparent;
        if (!oldparent) {
            gli_rootwin = pairwin;
        }
    }

    return newwin;
}

winid_t glk_window_open(winid_t splitwin, glui32 method, glui32 size,
    glui32 wintype, glui32 rock)
{
    jobject jnewwin, jpairwin;

    if ((jint)size < 0) {
        gli_strict_warning("window_open: size too large");
        return NULL;
    }

    if (!gli_window_open_validate(splitwin, method, wintype)) {
        /* A warning has already been displayed. */
        return NULL;
    }

    if (!jni_open_window(splitwin, method, (jint)size, wintype,
            &jnewwin, &jpairwin)) {
        return NULL;
    }

    return gli_window_open(splitwin, method, (jint)size, wintype, rock,
            jnewwin, jpairwin);
}

/*
 * Called after the Java layer has closed a window, to update the
 * C window tree structure.
 */
static void gli_window_tree_close(window_t *win)
{
    if (win == gli_rootwin || win->parent == NULL) {
        gli_rootwin = NULL;
    } else {
        window_t *pairwin, *sibwin, *grandparwin;

        pairwin = win->parent;
        sibwin = glk_window_get_sibling(win);
        if (!sibwin) {
            gli_strict_warning("window_close: window tree is corrupted");
            return;
        }

        grandparwin = pairwin->parent;
        gli_unregister_window(pairwin);
        if (!grandparwin) {
            gli_rootwin = sibwin;
            sibwin->parent = NULL;
        } else {
            sibwin->parent = grandparwin;
        }
    }
}

/*
 * Called after the Java layer has closed a window, to remove entries
 * from the C window list.
 */
static void gli_window_list_close(window_t *win)
{
    window_t *dwin, *pwin, *temp;

    win->deletemark = TRUE;

    for (dwin = gli_windowlist; dwin; dwin = dwin->next) {
        /* Update pair windows whose key is being closed. */
        if (dwin->type == wintype_Pair && dwin->key == win) {
            dwin->key = NULL;
        }

        /* Mark descendants of the closed window. */
        for (pwin = dwin->parent; pwin; pwin = pwin->parent) {
            if (pwin->deletemark) {
                dwin->deletemark = TRUE;
                break;
            }
        }
    }

    /* Remove all marked windows from the window list. */
    dwin = gli_windowlist;

    while (dwin) {
        if (dwin->deletemark) {
            temp = dwin->next;
            gli_unregister_window(dwin);
            dwin = temp;
        } else {
            dwin = dwin->next;
        }
    }
}

void glk_window_close(window_t *win, stream_result_t *result)
{
    if (!win) {
        gli_strict_warning("window_close: invalid ref");
        return;
    }

    (*jni_env)->CallVoidMethod(GLK_M(WINDOWCLOSE), win->jwin);
    jni_check_exc();

    gli_stream_fill_result(win->str, result);

    gli_window_tree_close(win);
    gli_window_list_close(win);
}

/*
 * Turns off echoing for windows whose echo stream has been closed.
 */
void gli_windows_unechostr(strid_t str)
{
    window_t *win;

    for (win = gli_windowlist; win; win = win->next) {
        if (win->echostr == str) {
            win->echostr = NULL;
        }
    }
}

glui32 glk_window_get_rock(window_t *win)
{
    if (!win) {
        gli_strict_warning("window_get_rock: invalid ref.");
        return 0;
    }

    return win->rock;
}

glui32 glk_window_get_type(window_t *win)
{
    if (!win) {
        gli_strict_warning("window_get_type: invalid ref");
        return 0;
    }
    return win->type;
}

winid_t glk_window_get_parent(window_t *win)
{
    if (!win) {
        gli_strict_warning("window_get_parent: invalid ref");
        return 0;
    }

    return win->parent;
}

winid_t glk_window_get_sibling(window_t *win)
{
    window_t *owin;

    if (!win) {
        gli_strict_warning("window_get_sibling: invalid ref");
        return NULL;
    }
    if (!win->parent) {
        return NULL;
    }

    for (owin = gli_windowlist; owin; owin = owin->next) {
        if ((owin != win) && (owin->parent == win->parent)) {
            return owin;
        }
    }

    /* Should never get here. */
    return NULL;
}

strid_t glk_window_get_stream(window_t *win)
{
    if (!win) {
        gli_strict_warning("window_get_stream: invalid ref");
        return NULL;
    }
    return win->str;
}

strid_t glk_window_get_echo_stream(window_t *win)
{
    if (!win) {
        gli_strict_warning("window_get_echo_stream: invalid ref");
        return 0;
    }
    return win->echostr;
}

void glk_window_set_echo_stream(window_t *win, strid_t str)
{
    if (!win) {
        gli_strict_warning("window_set_echo_stream: invalid window id");
        return;
    }
    win->echostr = str;
}

void glk_request_mouse_event(window_t *win)
{
    if (!win) {
        gli_strict_warning("request_mouse_event: invalid ref");
        return;
    }

    if (!gli_output_wintype(win->type)) {
        gli_strict_warning("request_mouse_event: window does not support mouse input");
        return;
    }

    if (!win->mouse_request) {
        (*jni_env)->CallVoidMethod(WIN_M(win->jwin, REQUESTMOUSE));
        if (!jni_check_exc()) {
            win->mouse_request = TRUE;
        }
    }
}

void glk_cancel_mouse_event(window_t *win)
{
    if (!win) {
        gli_strict_warning("cancel_mouse_event: invalid ref");
        return;
    }

    if (win->mouse_request) {
        (*jni_env)->CallVoidMethod(WIN_M(win->jwin, CANCELMOUSE));
        jni_check_exc();
        win->mouse_request = FALSE;
    }
}

void glk_window_clear(window_t *win)
{
    if (!win) {
        gli_strict_warning("window_clear: invalid ref");
        return;
    }

    if (!gli_output_wintype(win->type)) {
        return;
    }

    if (win->text) {
        if (win->text->kb_request & KB_LINE_REQ) {
            gli_strict_warning("window_clear: window has pending line request");
            return;
        }
        gli_window_clear_outbuf(win->text);
    }

    (*jni_env)->CallVoidMethod(WIN_M(win->jwin, CLEAR));
    jni_check_exc();
}

static void jni_window_get_size(jobject jwin, glui32 *width, glui32 *height)
{
    jintArray jsizes;
    jint *sizes = NULL;
    glui32 wid = 0;
    glui32 hgt = 0;

    jsizes = (*jni_env)->NewIntArray(jni_env, 2);
    if (!jsizes) {
        jni_no_mem();
    }
    (*jni_env)->CallVoidMethod(WIN_M(jwin, SIZE), jsizes);
    if (jni_check_exc()) {
        goto done;
    }

    sizes = (*jni_env)->GetIntArrayElements(jni_env, jsizes, NULL);
    if (!sizes) {
        goto done;
    }
    wid = (glui32)sizes[0];
    hgt = (glui32)sizes[1];
    (*jni_env)->ReleaseIntArrayElements(jni_env, jsizes, sizes, JNI_ABORT);

done:
    DELETE_LOCAL(jsizes);
    if (width) {
        *width = wid;
    }
    if (height) {
        *height = hgt;
    }
}

void glk_window_get_size(window_t *win, glui32 *width, glui32 *height)
{
    if (!win) {
        gli_strict_warning("window_get_size: invalid ref");
        return;
    }

    if (gli_output_wintype(win->type)) {
        jni_window_get_size(win->jwin, width, height);
    } else {
        if (width) {
            *width = 0;
        }
        if (height) {
            *height = 0;
        }
    }
}

void glk_window_get_arrangement(window_t *win, glui32 *method, glui32 *size,
    winid_t *keywin)
{
    if (!win) {
        gli_strict_warning("window_get_arrangement: invalid ref");
        return;
    }

    if (win->type != wintype_Pair) {
        gli_strict_warning("window_get_arrangement: not a Pair window");
        return;
    }

    if (size) {
        *size = win->constraint;
    }
    if (keywin) {
        *keywin = win->key;
    }
    if (method) {
        *method = win->split_method;
    }
}

void glk_window_set_arrangement(window_t *win, glui32 method, glui32 size,
    winid_t key)
{
    glui32 newdir, olddir;
    int newvertical, oldvertical;

    if (!win) {
        gli_strict_warning("window_set_arrangement: invalid ref");
        return;
    }

    if (win->type != wintype_Pair) {
        gli_strict_warning("window_set_arrangement: not a Pair window");
        return;
    }

    if ((jint)size < 0) {
        gli_strict_warning("window_set_arrangement: size too large");
        return;
    }

    if (key) {
        window_t *wx;
        if (key->type == wintype_Pair) {
            gli_strict_warning("window_set_arrangement: keywin cannot be a Pair");
            return;
        }
        for (wx = key; wx; wx = wx->parent) {
            if (wx == win && wx != key)
                break;
        }
        if (wx == NULL) {
            gli_strict_warning("window_set_arrangement: keywin must be a descendant");
            return;
        }
    } else {
        key = win->key;
    }

    newdir = method & winmethod_DirMask;
    newvertical = (newdir == winmethod_Left || newdir == winmethod_Right);

    olddir = win->split_method & winmethod_DirMask;
    oldvertical = (olddir == winmethod_Left || olddir == winmethod_Right);

    if ((newvertical && !oldvertical) || (!newvertical && oldvertical)) {
        if (!oldvertical) {
            gli_strict_warning("window_set_arrangement: split must stay horizontal");
        } else {
            gli_strict_warning("window_set_arrangement: split must stay vertical");
        }
        return;
    }

    if (key && key->type == wintype_Blank
        && (method & winmethod_DivisionMask) == winmethod_Fixed) {
        gli_strict_warning("window_set_arrangement: a Blank window cannot have a fixed size");
        return;
    }

    if (win->split_method == method
            && win->key == key
            && win->constraint == (jint)size) {
        return;
    }

    win->split_method = method;
    win->key = key;
    win->constraint = (jint)size;

    (*jni_env)->CallVoidMethod(WIN_M(win->jwin, ARRANGE),
            (jint)method, (jint)size, key->jwin);
    jni_check_exc();
}

/* Returns the window structure identified by ID. */
window_t *gli_window_by_id(int id)
{
    window_t *win;

    for (win = gli_windowlist; win; win = win->next) {
        if (win->id == id) {
            break;
        }
    }

    return win;
}
