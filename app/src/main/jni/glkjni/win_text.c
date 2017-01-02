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

#define OUTBUFCHARS (2048)

void gli_textwin_init(window_t *win)
{
    textwin_data_t *text =
            (textwin_data_t *)gli_malloc(sizeof(textwin_data_t));

    text->curr_style = style_Normal;
    text->curr_linkval = 0;
    text->link_request = FALSE;
    text->kb_request = 0;
    text->inbuf = NULL;
    text->inbuf_len = 0;
    text->inbuf_unicode = FALSE;

    text->outbuf = (jchar *)gli_malloc(sizeof(jchar) * OUTBUFCHARS);
    text->outbuf_count = 0;

    win->text = text;
}

static jobject gli_register_win_input(window_t *win, char *buf,
        glui32 *ubuf, glui32 maxlen, int unicode)
{
    jobject bytebuf;
    jlong buflen;
    textwin_data_t *text = win->text;

    text->inbuf = (unicode ? (void *)ubuf : (void *)buf);
    text->inbuf_unicode = unicode;
    text->inbuf_len = maxlen;

    if (gli_register_arr) {
        char *typedesc = (unicode ? "&+#!Iu" : "&+#!Cn");
        text->inbuf_rock = (*gli_register_arr)(text->inbuf, text->inbuf_len,
                typedesc);
    }

    buflen = (unicode ? maxlen * sizeof(glui32) : maxlen);
    bytebuf = jni_newbytebuffer(text->inbuf, buflen);

    return bytebuf;
}

void gli_unregister_win_input(window_t *win)
{
    textwin_data_t *text = win->text;

    if (text->inbuf) {
        if (gli_unregister_arr) {
            char *typedesc = (text->inbuf_unicode ? "&+#!Iu" : "&+#!Cn");
            (*gli_unregister_arr)(text->inbuf, text->inbuf_len, typedesc,
                    text->inbuf_rock);
        }
        text->inbuf = NULL;
    }
}

void gli_echo_line_input(window_t *win, glui32 len)
{
    textwin_data_t *text;

    if (!win->echostr || !len) {
        return;
    }

    text = win->text;

    if (text->inbuf_unicode) {
        gli_stream_echo_line_uni(win->echostr, text->inbuf, len);
    } else {
        gli_stream_echo_line(win->echostr, text->inbuf, len);
    }
}

void gli_window_clear_outbuf(textwin_data_t *text)
{
    text->outbuf_count = 0;
}

void gli_window_print(window_t *win)
{
    jobject jstr;
    textwin_data_t *text = win->text;

    if (!text->outbuf_count) {
        return;
    }

    jstr = (*jni_env)->NewString(jni_env, text->outbuf, text->outbuf_count);
    (*jni_env)->CallVoidMethod(WIN_M(win->jwin, PRINT), jstr);
    DELETE_LOCAL(jstr);
    jni_check_exc();

    gli_window_clear_outbuf(text);
}

static void gli_window_buffer_char(window_t *win, glui32 ch)
{
    if (win->text->outbuf_count > OUTBUFCHARS - 2) {
        gli_window_print(win);
    }
    if (ch > 0xFFFF) {
        jchar surr1, surr2;

        ch -= 0x10000;
        surr1 = 0xD800 | (ch >> 10);
        surr2 = 0xDC00 | (ch & 0x3FF);
        (win->text->outbuf)[win->text->outbuf_count++] = surr1;
        (win->text->outbuf)[win->text->outbuf_count++] = surr2;
    } else {
        (win->text->outbuf)[win->text->outbuf_count++] = (jchar)ch;
    }
}

void gli_window_write(window_t *win, char *buf, glui32 len)
{
    glui32 i;

    if (!win->text) {
        return;
    }

    if (win->text->kb_request & KB_LINE_REQ) {
        gli_strict_warning("put_buffer: window has pending line request");
        return;
    }

    for (i = 0; i < len; i++) {
        gli_window_buffer_char(win, (unsigned char)buf[i]);
    }

    if (win->echostr) {
        gli_put_buffer(win->echostr, buf, len);
    }
}

void gli_window_write_uni(winid_t win, glui32 *buf, glui32 len)
{
    glui32 i;

    if (!win->text) {
        return;
    }

    if (win->text->kb_request & KB_LINE_REQ) {
        gli_strict_warning("put_buffer: window has pending line request");
        return;
    }

    for (i = 0; i < len; i++) {
        gli_window_buffer_char(win, buf[i]);
    }

    if (win->echostr) {
        gli_put_buffer_uni(win->echostr, buf, len);
    }
}

void gli_window_putc(window_t *win, glui32 ch)
{
    if (!win->text) {
        return;
    }

    if (win->text->kb_request & KB_LINE_REQ) {
        gli_strict_warning("put_char: window has pending line request");
        return;
    }

    gli_window_buffer_char(win, ch);

    if (win->echostr) {
        gli_put_char(win->echostr, ch);
    }
}

void glk_window_move_cursor(window_t *win, glui32 xpos, glui32 ypos)
{
    if (!win) {
        gli_strict_warning("window_move_cursor: invalid ref");
        return;
    }

    if (win->type != wintype_TextGrid) {
        gli_strict_warning("window_move_cursor: not a TextGrid window");
        return;
    }
    if (((jint)xpos < 0) || ((jint)ypos < 0)) {
        gli_strict_warning("window_move_cursor: invalid position");
        return;
    }

    gli_window_print(win);

    (*jni_env)->CallVoidMethod(WIN_M(win->jwin, CURSOR),
            (jint)xpos, (jint)ypos);
    jni_check_exc();
}

static void gli_request_char_event(window_t *win, int unicode)
{
    jboolean junicode;

    if (!win) {
        gli_strict_warning("request_char_event: invalid ref");
        return;
    }

    if (!win->text) {
        gli_strict_warning("request_char_event: window does not support keyboard input");
        return;
    }

    if (win->text->kb_request) {
        gli_strict_warning("request_char_event: window already has keyboard request");
        return;
    }

    win->text->kb_request = KB_CHAR_REQ;
    if (unicode) {
        win->text->kb_request |= KB_UNI_REQ;
        junicode = JNI_TRUE;
    } else {
        junicode = JNI_FALSE;
    }

    (*jni_env)->CallVoidMethod(
            WIN_M(win->jwin, REQUESTCHAR),
            junicode);
    jni_check_exc();
}

void glk_request_char_event(window_t *win)
{
    gli_request_char_event(win, FALSE);
}

void glk_request_char_event_uni(window_t *win)
{
    gli_request_char_event(win, TRUE);
}

void glk_cancel_char_event(window_t *win)
{
    if (!win) {
        gli_strict_warning("cancel_char_event: invalid ref");
        return;
    }

    if (win->text) {
        if (win->text->kb_request & KB_CHAR_REQ) {
            (*jni_env)->CallVoidMethod(WIN_M(win->jwin, CANCELCHAR));
            jni_check_exc();
            win->text->kb_request = 0;
        }
    }
}

static void gli_request_line_event(window_t *win, char *buf, glui32 *ubuf,
        glui32 maxlen, glui32 initlen, int unicode)
{
    jobject bytebuf;

    if (!win) {
        gli_strict_warning("request_line_event: invalid ref");
        return;
    }

    if (!win->text) {
        gli_strict_warning("request_line_event: window does not support keyboard input");
        return;
    }

    if (win->text->kb_request) {
        gli_strict_warning("request_line_event: window already has keyboard request");
        return;
    }

    if ((jint)maxlen <= 0) {
        gli_strict_warning("request_line_event: maxlen out of range");
        return;
    }

    if ((jint)initlen < 0 || initlen > maxlen) {
        gli_strict_warning("request_line_event: initlen out of range");
        return;
    }

    gli_window_print(win);

    bytebuf = gli_register_win_input(win, buf, ubuf, maxlen, unicode);

    if (unicode) {
        jobject intbuf;

        intbuf = (*jni_env)->CallObjectMethod(
                INSTANCE_M(bytebuf, BYTEBUFFER_ASINTBUF));
        (*jni_env)->CallVoidMethod(
                WIN_M(win->jwin, REQUESTLINEUNI),
                intbuf, (jint)maxlen, (jint)initlen);
        DELETE_LOCAL(intbuf);
        if (jni_check_exc()) {
            goto done;
        }
    } else {
        (*jni_env)->CallVoidMethod(
                WIN_M(win->jwin, REQUESTLINE),
                bytebuf, (jint)maxlen, (jint)initlen);
        if (jni_check_exc()) {
            goto done;
        }
    }

    win->text->kb_request = KB_LINE_REQ;
    if (unicode) {
        win->text->kb_request |= KB_UNI_REQ;
    }

done:
    DELETE_LOCAL(bytebuf);
}

void glk_request_line_event(window_t *win, char *buf, glui32 maxlen,
    glui32 initlen)
{
    gli_request_line_event(win, buf, NULL, maxlen, initlen, FALSE);
}

void glk_request_line_event_uni(window_t *win, glui32 *buf, glui32 maxlen,
    glui32 initlen)
{
    gli_request_line_event(win, NULL, buf, maxlen, initlen, TRUE);
}

void glk_cancel_line_event(window_t *win, event_t *ev)
{
    event_t dummyev;

    if (!ev) {
        ev = &dummyev;
    }

    gli_event_clearevent(ev);

    if (!win) {
        gli_strict_warning("cancel_line_event: invalid ref");
        return;
    }

    if (!win->text) {
        return;
    }

    if (win->text->kb_request & KB_LINE_REQ) {
        int len = (*jni_env)->CallIntMethod(
                WIN_M(win->jwin, CANCELLINE));
        if (!jni_check_exc()) {
            ev->type = evtype_LineInput;
            ev->win = win;
            ev->val1 = len;
            gli_echo_line_input(win, len);
        }

        gli_unregister_win_input(win);
        win->text->kb_request = 0;
    }
}

void gli_window_set_style(window_t *win, glui32 val)
{
    if (!win->text) {
        return;
    }
    if (win->text->curr_style == val) {
        return;
    }

    gli_window_print(win);

    (*jni_env)->CallVoidMethod(WIN_M(win->jwin, STYLE),
            (jint)val);
    jni_check_exc();

    win->text->curr_style = val;
    if (win->echostr) {
        gli_set_style(win->echostr, val);
    }
}

glui32 glk_style_distinguish(window_t *win, glui32 styl1, glui32 styl2)
{
    jboolean res;

    if (!win) {
        gli_strict_warning("style_distinguish: invalid ref");
        return FALSE;
    }
    if (styl1 >= style_NUMSTYLES || styl2 >= style_NUMSTYLES) {
        return FALSE;
    }

    if (!win->text) {
        return FALSE;
    }

    res = (*jni_env)->CallBooleanMethod(
            WIN_M(win->jwin, DISTINGUISH),
            (jint)styl1, (jint)styl2);
    if (jni_check_exc()) {
        return FALSE;
    }

    return res;
}

glui32 glk_style_measure(window_t *win, glui32 styl, glui32 hint,
    glui32 *result)
{
    glui32 dummy;
    jint res;

    if (!win) {
        gli_strict_warning("style_measure: invalid ref");
        return FALSE;
    }

    if (styl >= style_NUMSTYLES || hint >= stylehint_NUMHINTS)
        return FALSE;

    if (!win->text) {
        return FALSE;
    }

    res = (*jni_env)->CallIntMethod(
            WIN_M(win->jwin, MEASURESTYLE),
            (jint)styl, (jint)hint);
    if (jni_check_exc()) {
        return FALSE;
    }

    if (!result) {
        result = &dummy;
    }

    *result = (glui32)res;
    return TRUE;
}

void gli_window_set_hyperlink(window_t *win, glui32 val)
{
    if (!win->text) {
        return;
    }
    if (win->text->curr_linkval == val) {
        return;
    }

    gli_window_print(win);

    (*jni_env)->CallVoidMethod(
            WIN_M(win->jwin, SETLINK), (jint)val);
    jni_check_exc();

    win->text->curr_linkval = val;
}

void glk_request_hyperlink_event(winid_t win)
{
    if (!win) {
        gli_strict_warning("request_hyperlink_event: invalid ref");
        return;
    }

    if (!win->text) {
        gli_strict_warning("request_hyperlink_event: window does not support hyperlinks");
        return;
    }

    if (!win->text->link_request) {
        (*jni_env)->CallVoidMethod(WIN_M(win->jwin, REQUESTLINK));
        if (!jni_check_exc()) {
            win->text->link_request = TRUE;
        }
    }
}

void glk_cancel_hyperlink_event(winid_t win)
{
    if (!win) {
        gli_strict_warning("cancel_hyperlink_event: invalid ref");
        return;
    }
    if (!win->text) {
        return;
    }

    if (win->text->link_request) {
        (*jni_env)->CallVoidMethod(WIN_M(win->jwin, CANCELLINK));
        jni_check_exc();
        win->text->link_request = FALSE;
    }
}

void glk_stylehint_set(glui32 wintype, glui32 styl, glui32 hint,
    glsi32 val)
{
    if (wintype != wintype_AllTypes && !gli_text_wintype(wintype)) {
        return;
    }

    if (hint >= stylehint_NUMHINTS || styl >= style_NUMSTYLES) {
        return;
    }

    switch (hint) {
    case stylehint_Justification:
        if (val != stylehint_just_Centered
                && val != stylehint_just_LeftFlush
                && val != stylehint_just_LeftRight
                && val != stylehint_just_RightFlush) {
            return;
        }
        break;
    case stylehint_Weight:
        if (val != -1 && val != 0 && val != 1) {
            return;
        }
        break;
    case stylehint_Oblique:
    case stylehint_Proportional:
    case stylehint_ReverseColor:
        if (val != 0 && val != 1) {
            return;
        }
        break;
    case stylehint_TextColor:
    case stylehint_BackColor:
        if (val & 0xFF000000) {
            return;
        }
    }

    (*jni_env)->CallVoidMethod(GLK_M(SETHINT),
            (jint)wintype, (jint)styl, (jint)hint, (jint)val);
    jni_check_exc();
}

void glk_stylehint_clear(glui32 wintype, glui32 styl, glui32 hint)
{
    if (wintype != wintype_AllTypes && !gli_text_wintype(wintype)) {
        return;
    }

    if (hint >= stylehint_NUMHINTS || styl >= style_NUMSTYLES) {
        return;
    }

    (*jni_env)->CallVoidMethod(GLK_M(CLEARHINT),
            (jint)wintype, (jint)styl, (jint)hint);
    jni_check_exc();
}
