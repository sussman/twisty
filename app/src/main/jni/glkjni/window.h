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

#ifndef WINDOW_H_
#define WINDOW_H_

#define KB_CHAR_REQ (0x01)
#define KB_LINE_REQ (0x02)
#define KB_UNI_REQ  (0x04)

typedef struct glk_window_struct window_t;
typedef struct textwin_data_struct textwin_data_t;

struct glk_window_struct {
    glui32 rock;
    glui32 id;

    glui32 type;

    window_t *parent;

    strid_t str;
    strid_t echostr;

    gidispatch_rock_t disprock;
    window_t *next, *prev;

    jobject jwin;

    /* For pair windows: */
    glui32 split_method;
    window_t *key;
    jint constraint;

    /* For all visible windows: */
    int mouse_request;

    /* For text windows: */
    textwin_data_t *text;

    /* A scratch variable used by gli_window_close(). */
    int deletemark;
};

struct textwin_data_struct {
    glui32 curr_style;
    glui32 curr_linkval;
    int kb_request;
    int link_request;
    void *inbuf;
    int inbuf_unicode;
    int inbuf_len;
    jchar *outbuf;
    int outbuf_count;
    gidispatch_rock_t inbuf_rock;
};

void gli_textwin_init(window_t *win);
void gli_unregister_win_input(window_t *win);
void gli_echo_line_input(window_t *win, glui32 len);
void gli_window_print(window_t *win);
void gli_window_clear_outbuf(textwin_data_t *text);
int gli_text_wintype(glui32 wintype);

#endif /* WINDOW_H_ */
