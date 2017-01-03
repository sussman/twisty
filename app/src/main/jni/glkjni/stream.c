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

#include <string.h>
#include "glk.h"
#include "glkjni.h"
#include "jcall.h"
#include "stream.h"

typedef struct glk_stream_struct stream_t;

struct glk_stream_struct {
    glui32 rock;

    int type;
    void *data;

    int readable, writable;
    glui32 readcount, writecount;

    stream_t *next, *prev;
    gidispatch_rock_t disprock;
};

static stream_t *gli_streamlist = NULL; /* linked list of all streams */
static stream_t *gli_currentstr = NULL; /* the current output stream */

void stream_c_shutdown(void)
{
    stream_t *curr, *next;

    curr = gli_streamlist;

    while (curr) {
        next = curr->next;

        if (curr->type == strtype_File) {
            fstream_delete(curr->data);
        }

        /* This will be handled by window_c_shutdown. */
        if (curr->type != strtype_Window) {
            free(curr->data);
        }

        free(curr);
        curr = next;
    }

    gli_streamlist = NULL;
    gli_currentstr = NULL;
}

gidispatch_rock_t gli_str_get_disprock(strid_t str)
{
    return str->disprock;
}

void gli_str_set_disprock(stream_t *str)
{
    if (gli_register_obj) {
        str->disprock = (*gli_register_obj)(str, gidisp_Class_Stream);
    } else {
        str->disprock.ptr = NULL;
    }
}

strid_t glk_stream_iterate(strid_t str, glui32 *rock)
{
    if (!str) {
        str = gli_streamlist;
    } else {
        str = str->next;
    }

    if (str) {
        if (rock) {
            *rock = str->rock;
        }
        return str;
    } else {
        if (rock) {
            *rock = 0;
        }
        return NULL;
    }
}

void gli_stream_set_current(stream_t *str)
{
    gli_currentstr = str;
}

void glk_stream_set_current(stream_t *str)
{
    gli_stream_set_current(str);
}

strid_t glk_stream_get_current()
{
    return gli_currentstr;
}

stream_t *gli_stream_new(int type, int readable, int writable, int rock,
        void *data)
{
    stream_t *str = (stream_t *)gli_malloc(sizeof(stream_t));

    str->rock = rock;
    str->type = type;
    str->readable = readable;
    str->writable = writable;
    str->readcount = 0;
    str->writecount = 0;
    str->data = data;

    return str;
}

stream_t *gli_stream_register(int type, int readable, int writable,
        int rock, void *data)
{
    stream_t *str = gli_stream_new(type, readable, writable, rock, data);

    str->prev = NULL;
    str->next = gli_streamlist;
    gli_streamlist = str;
    if (str->next) {
        str->next->prev = str;
    }

    gli_str_set_disprock(str);

    if (type == strtype_Memory && gli_register_arr) {
        mstream_register(data);
    }

    return str;
}

static stream_t *gli_stream_open_memory(void *buf, glui32 buflen,
        glui32 fmode, glui32 rock, int unicode)
{
    mstream_data_t *data;

    if (fmode != filemode_Read
        && fmode != filemode_Write
        && fmode != filemode_ReadWrite) {
        gli_strict_warning("stream_open_memory: illegal filemode");
        return NULL;
    }

    data = mstream_create_data(buf, buflen, fmode, unicode);

    return gli_stream_register(strtype_Memory,
        (fmode != filemode_Write),
        (fmode != filemode_Read),
        rock, data);
}

strid_t glk_stream_open_memory(char *buf, glui32 buflen, glui32 fmode,
    glui32 rock)
{
    return gli_stream_open_memory(buf, buflen, fmode, rock, FALSE);
}

strid_t glk_stream_open_memory_uni(glui32 *ubuf, glui32 buflen,
        glui32 fmode, glui32 rock)
{
    return gli_stream_open_memory(ubuf, buflen, fmode, rock, TRUE);
}

static stream_t *gli_stream_open_file(frefid_t fref, glui32 fmode,
        glui32 rock, int unicode)
{
    char modestr[16];
    FILE *fl;
    fstream_data_t *data;
    int textmode;

    if (!fref) {
        gli_strict_warning("stream_open_file: invalid fileref id");
        return 0;
    }

    switch (fmode) {
    case filemode_Write:
        strcpy(modestr, "w");
        break;
    case filemode_Read:
        strcpy(modestr, "r");
        break;
    case filemode_ReadWrite:
        strcpy(modestr, "w+");
        break;
    case filemode_WriteAppend:
        strcpy(modestr, "a");
        break;
    }

    textmode = gli_fileref_get_textmode(fref);

    if (!textmode) {
        strcat(modestr, "b");
    }

    fl = fopen(gli_fileref_get_filename(fref), modestr);
    if (!fl) {
        gli_strict_warning("stream_open_file: unable to open file.");
        return NULL;
    }

    data = fstream_create_data(fl, textmode, unicode);

    return gli_stream_register(strtype_File,
            (fmode == filemode_Read || fmode == filemode_ReadWrite),
            !(fmode == filemode_Read),
            rock, data);
}

strid_t glk_stream_open_file(frefid_t fref, glui32 fmode, glui32 rock)
{
    return gli_stream_open_file(fref, fmode, rock, FALSE);
}

strid_t glk_stream_open_file_uni(frefid_t fref, glui32 fmode,
    glui32 rock)
{
    return gli_stream_open_file(fref, fmode, rock, TRUE);
}

void gli_stream_delete(stream_t *str) {
    gli_windows_unechostr(str);

    if (str->type == strtype_File) {
        fstream_delete(str->data);
        free(str->data);
    } else if (str->type == strtype_Memory) {
        free(str->data);
    }
    /* Do not free str->data for strtype_Window. */

    free(str);
}

void gli_stream_unregister(stream_t *str) {
    stream_t *prev, *next;

    if (str == gli_currentstr) {
        gli_currentstr = NULL;
    }

    if (gli_unregister_obj) {
        (*gli_unregister_obj)(str, gidisp_Class_Stream, str->disprock);
        str->disprock.ptr = NULL;
    }

    if (str->type == strtype_Memory && gli_unregister_arr) {
        mstream_unregister(str->data);
    }

    prev = str->prev;
    next = str->next;

    if (prev) {
        prev->next = next;
    } else {
        gli_streamlist = next;
    }
    if (next) {
        next->prev = prev;
    }

    gli_stream_delete(str);
}

void gli_stream_fill_result(stream_t *str, stream_result_t *result)
{
    if (!result) {
        return;
    }

    result->readcount = str->readcount;
    result->writecount = str->writecount;
}

void glk_stream_close(stream_t *str, stream_result_t *result)
{
    if (!str) {
        gli_strict_warning("stream_close: invalid ref.");
        return;
    }

    if (str->type == strtype_Window) {
        gli_strict_warning("stream_close: cannot close window stream");
        return;
    }

    gli_stream_fill_result(str, result);
    gli_stream_unregister(str);
}

void glk_stream_set_position(stream_t *str, glsi32 pos, glui32 seekmode)
{
    if (!str) {
        gli_strict_warning("stream_set_position: invalid ref");
        return;
    }

    if (str->type == strtype_Memory) {
        mstream_set_pos(str->data, pos, seekmode);
    } else if (str->type == strtype_File) {
        fstream_set_pos(str->data, pos, seekmode);
    }
}

glui32 glk_stream_get_position(stream_t *str)
{
    if (!str) {
        gli_strict_warning("stream_get_position: invalid ref");
        return 0;
    }

    if (str->type == strtype_Memory) {
        return mstream_get_pos(str->data);
    } else if (str->type == strtype_File) {
        return fstream_get_pos(str->data);
    } else {
        return 0;
    }
}

glui32 glk_stream_get_rock(stream_t *str)
{
    if (!str) {
        gli_strict_warning("stream_get_rock: invalid ref.");
        return 0;
    }
    return str->rock;
}

glsi32 gli_get_char(stream_t *str)
{
    glsi32 ch;

    if (!str->readable) {
        return -1;
    }

    if (str->type == strtype_Memory) {
        ch = mstream_getc(str->data);
    } else if (str->type == strtype_File) {
        ch = fstream_getc(str->data);
    } else {
        ch = -1;
    }

    if (ch >= 0) {
        str->readcount++;
    }

    return ch;
}

glsi32 glk_get_char_stream(stream_t *str)
{
    glsi32 ch;

    if (!str) {
        gli_strict_warning("get_char_stream: invalid ref");
        return -1;
    }
    ch = gli_get_char(str);
    return (ch > MAX_L1CHAR ? UNKNOWN_CHAR : ch);
}

glsi32 glk_get_char_stream_uni(strid_t str)
{
    if (!str) {
        gli_strict_warning("get_char_stream_uni: invalid ref");
        return -1;
    }
    return gli_get_char(str);
}

static glui32 gli_get_buffer(stream_t *str, char *buf, glui32 *ubuf,
        glui32 len)
{
    glui32 readcount;

    if (!str->readable) {
        return 0;
    }

    if (str->type == strtype_Memory) {
        readcount = mstream_read(str->data, buf, ubuf, len);
    } else if (str->type == strtype_File) {
        readcount = fstream_read(str->data, buf, ubuf, len);
    } else {
        readcount = 0;
    }

    str->readcount += readcount;
    return readcount;
}

glui32 glk_get_buffer_stream(stream_t *str, char *buf, glui32 len)
{
    if (!str) {
        gli_strict_warning("get_buffer_stream: invalid ref");
        return -1;
    }
    return gli_get_buffer(str, buf, NULL, len);
}

glui32 glk_get_buffer_stream_uni(strid_t str, glui32 *buf, glui32 len)
{
    if (!str) {
        gli_strict_warning("get_buffer_stream_uni: invalid ref");
        return -1;
    }
    return gli_get_buffer(str, NULL, buf, len);
}

static glui32 gli_get_line(stream_t *str, char *buf, glui32 *ubuf,
        glui32 len)
{
    glui32 readcount;

    if (!str->readable || !len) {
        return 0;
    }

    if (str->type == strtype_Memory) {
        readcount = mstream_gets(str->data, buf, ubuf, len);
    } else if (str->type == strtype_File) {
        readcount = fstream_gets(str->data, buf, ubuf, len);
    } else {
        return 0;
    }

    if (ubuf) {
        ubuf[readcount] = '\0';
    } else {
        buf[readcount] = '\0';
    }

    str->readcount += readcount;
    return readcount;
}

glui32 glk_get_line_stream(stream_t *str, char *buf, glui32 len)
{
    if (!str) {
        gli_strict_warning("get_line_stream: invalid ref");
        return -1;
    }
    return gli_get_line(str, buf, NULL, len);
}

glui32 glk_get_line_stream_uni(strid_t str, glui32 *buf, glui32 len)
{
    if (!str) {
        gli_strict_warning("get_line_stream_uni: invalid ref");
        return -1;
    }
    return gli_get_line(str, NULL, buf, len);
}

void gli_put_char(stream_t *str, glui32 ch)
{
    if (!str || !str->writable)
        return;

    str->writecount++;

    if (str->type == strtype_Memory) {
        mstream_putc(str->data, ch);
    } else if (str->type == strtype_File) {
        fstream_putc(str->data, ch);
    } else if (str->type == strtype_Window) {
        gli_window_putc(str->data, ch);
    }
}

void glk_put_char(unsigned char ch)
{
    gli_put_char(gli_currentstr, ch);
}

void glk_put_char_uni(glui32 ch)
{
    gli_put_char(gli_currentstr, ch);
}

void glk_put_char_stream(stream_t *str, unsigned char ch)
{
    if (!str) {
        gli_strict_warning("put_char_stream: invalid ref");
        return;
    }
    gli_put_char(str, ch);
}

void glk_put_char_stream_uni(stream_t *str, glui32 ch)
{
    if (!str) {
        gli_strict_warning("put_char_stream: invalid ref");
        return;
    }
    gli_put_char(str, ch);
}

void gli_put_buffer(stream_t *str, char *buf, glui32 len)
{
    if (!str || !str->writable) {
        return;
    }

    str->writecount += len;

    if (str->type == strtype_Memory) {
        mstream_write(str->data, buf, len);
    } else if (str->type == strtype_File) {
        fstream_write(str->data, buf, len);
    } else if (str->type == strtype_Window) {
        gli_window_write(str->data, buf, len);
    }
}

void glk_put_buffer(char *buf, glui32 len)
{
    gli_put_buffer(gli_currentstr, buf, len);
}

void glk_put_buffer_stream(stream_t *str, char *buf, glui32 len)
{
    if (!str) {
        gli_strict_warning("put_string_stream: invalid ref");
        return;
    }
    gli_put_buffer(str, buf, len);
}

void gli_put_buffer_uni(stream_t *str, glui32 *buf, glui32 len)
{
    if (!str || !str->writable) {
        return;
    }

    str->writecount += len;

    if (str->type == strtype_Memory) {
        mstream_write_uni(str->data, buf, len);
    } else if (str->type == strtype_File) {
        fstream_write_uni(str->data, buf, len);
    } else if (str->type == strtype_Window) {
        gli_window_write_uni(str->data, buf, len);
    }
}

void glk_put_buffer_uni(glui32 *buf, glui32 len)
{
    gli_put_buffer_uni(gli_currentstr, buf, len);
}

void glk_put_buffer_stream_uni(stream_t *str, glui32 *buf, glui32 len)
{
    if (!str) {
        gli_strict_warning("put_string_stream: invalid ref");
        return;
    }
    gli_put_buffer_uni(str, buf, len);
}

void glk_put_string(char *s)
{
    gli_put_buffer(gli_currentstr, s, strlen(s));
}

void glk_put_string_stream(stream_t *str, char *s)
{
    if (!str) {
        gli_strict_warning("put_string_stream: invalid ref");
        return;
    }
    gli_put_buffer(str, s, strlen(s));
}

static glui32 strlen_uni(glui32 *s)
{
    glui32 length = 0;
    while (*s++) length++;
    return length;
}

void glk_put_string_uni(glui32 *us)
{
    gli_put_buffer_uni(gli_currentstr, us, strlen_uni(us));
}

void glk_put_string_stream_uni(stream_t *str, glui32 *us)
{
    if (!str) {
        gli_strict_warning("put_string_stream: invalid ref");
        return;
    }
    gli_put_buffer_uni(str, us, strlen_uni(us));
}

void gli_stream_echo_line(stream_t *str, char *buf, glui32 len)
{
    gli_put_buffer(str, buf, len);
    gli_put_char(str, '\n');
}

void gli_stream_echo_line_uni(stream_t *str, glui32 *buf, glui32 len)
{
    gli_put_buffer_uni(str, buf, len);
    gli_put_char(str, '\n');
}

void gli_set_style(stream_t *str, glui32 val)
{
    if (!str || !str->writable) {
        return;
    }

    if (val >= style_NUMSTYLES) {
        val = style_Normal;
    }

    if (str->type == strtype_Window) {
        gli_window_set_style(str->data, val);
    }
}

void glk_set_style(glui32 val)
{
    gli_set_style(gli_currentstr, val);
}

void glk_set_style_stream(stream_t *str, glui32 val)
{
    if (!str) {
        gli_strict_warning("set_style_stream: invalid ref");
        return;
    }
    gli_set_style(str, val);
}

static void gli_set_hyperlink(stream_t *str, glui32 linkval)
{
    if (!str || !str->writable || (str->type != strtype_Window)) {
        return;
    }
    gli_window_set_hyperlink(str->data, linkval);
}

void glk_set_hyperlink(glui32 linkval)
{
    gli_set_hyperlink(gli_currentstr, linkval);
}

void glk_set_hyperlink_stream(strid_t str, glui32 linkval)
{
    if (!str) {
        gli_strict_warning("set_hyperlink_stream: invalid ref");
        return;
    }
    gli_set_hyperlink(str, linkval);
}
