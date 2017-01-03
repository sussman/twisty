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

#ifndef GLKJNI_H_
#define GLKJNI_H_

#ifdef ANDROID
#include <setjmp.h>
#include <android/log.h>
#endif

#include <stdlib.h>
#include <jni.h>
#include "glk.h"
#include "gi_dispa.h"
#include "gi_blorb.h"

/* First, we define our own TRUE and FALSE and NULL, because ANSI
    is a strange world. */

#ifndef TRUE
#define TRUE 1
#endif
#ifndef FALSE
#define FALSE 0
#endif
#ifndef NULL
#define NULL 0
#endif

#define MAX_UNICHAR 0x10FFFF
#define MAX_L1CHAR 0xFF
#define UNKNOWN_CHAR 0x3F

#define strtype_File (1)
#define strtype_Window (2)
#define strtype_Memory (3)

#ifdef ANDROID
#define gli_strict_warning(msg) \
    (__android_log_print(ANDROID_LOG_WARN, "glk", "Library error: %s\n", msg))
#else
#define gli_strict_warning(msg) \
    (fprintf(stderr, "Glk library error: %s\n", msg))
#endif

typedef glui32 gli_case_block_t[2]; /* upper, lower */
/* If both are 0xFFFFFFFF, you have to look at the special-case table */

typedef glui32 gli_case_special_t[3]; /* upper, lower, title */
/* Each of these points to a subarray of the unigen_special_array
   (in cgunicode.c). In that subarray, element zero is the length,
   and that's followed by length unicode values. */

/* Callbacks necessary for the dispatch layer. */

extern gidispatch_rock_t (*gli_register_obj)(void *obj, glui32 objclass);
extern void (*gli_unregister_obj)(void *obj, glui32 objclass,
    gidispatch_rock_t objrock);
extern gidispatch_rock_t (*gli_register_arr)(void *array, glui32 len,
    char *typecode);
extern void (*gli_unregister_arr)(void *array, glui32 len, char *typecode,
    gidispatch_rock_t objrock);

/* Declarations of library internal functions. */

void gli_fatal(char *msg);
void *gli_malloc(size_t size);

int gli_fileref_get_textmode(frefid_t fref);
char *gli_fileref_get_filename(frefid_t fref);

void gli_stream_set_current(strid_t str);
strid_t gli_stream_register(int type, int readable, int writable,
        int rock, void *data);
void gli_stream_unregister(strid_t str);
void gli_stream_fill_result(strid_t str, stream_result_t *result);
void gli_stream_echo_line(strid_t str, char *buf, glui32 len);
void gli_stream_echo_line_uni(strid_t str, glui32 *buf, glui32 len);
void gli_set_style(strid_t str, glui32 val);
void gli_put_char(strid_t str, glui32 ch);
void gli_put_buffer(strid_t str, char *buf, glui32 len);
void gli_put_buffer_uni(strid_t str, glui32 *buf, glui32 len);

void gli_windows_unechostr(strid_t str);
void gli_window_set_style(winid_t win, glui32 val);
void gli_window_set_hyperlink(winid_t win, glui32 val);
void gli_window_write(winid_t win, char *buf, glui32 len);
void gli_window_write_uni(winid_t win, glui32 *buf, glui32 len);
void gli_window_putc(winid_t win, glui32 ch);
void gli_windows_print();
winid_t gli_window_by_id(int id);
int gli_process_window_event(winid_t win, glui32 type, glui32 val1);

void gli_initialize_latin1(void);

gidispatch_rock_t gli_win_get_disprock(winid_t win);
void gli_win_set_disprock(winid_t win);
gidispatch_rock_t gli_str_get_disprock(strid_t str);
void gli_str_set_disprock(strid_t str);
gidispatch_rock_t gli_fref_get_disprock(frefid_t fref);
void gli_fref_set_disprock(frefid_t fref);
gidispatch_rock_t gli_schan_get_disprock(schanid_t schan);
void gli_schan_set_disprock(schanid_t schan);

jobject glkjni_get_blorb_resource(glui32 usage, glui32 resnum);

void gli_exit(void);
void gli_interrupted(void);

#ifdef ANDROID
extern jmp_buf jump_error;
#define JMP_WHOOPS 1
#define JMP_DONE 2
#define JMP_INT 3
#endif

#define gli_event_clearevent(evp)  \
    ((evp)->type = evtype_None,    \
    (evp)->win = NULL,    \
    (evp)->val1 = 0,   \
    (evp)->val2 = 0)

#endif /* GLKJNI_H_ */
