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
#include <string.h>
#include "glk.h"
#include "glkjni.h"
#include "stream.h"

struct fstream_data_struct {
    int unicode;
    int textmode;

    FILE *file;
};

fstream_data_t *fstream_create_data(FILE *fl, int textmode, int unicode)
{
    fstream_data_t *data;

    data = (fstream_data_t *)gli_malloc(sizeof(fstream_data_t));
    data->file = fl;
    data->textmode = textmode;
    data->unicode = unicode;

    return data;
}

void fstream_delete(fstream_data_t *data)
{
    fclose(data->file);
}

void fstream_set_pos(fstream_data_t *data, glsi32 pos, glui32 seekmode)
{
    if (data->unicode) {
        pos *= 4;
    }
    fseek(data->file, pos,
            ((seekmode == seekmode_Current) ? 1 :
            ((seekmode == seekmode_End) ? 2 : 0)));
}

glui32 fstream_get_pos(fstream_data_t *data)
{
    if (data->unicode) {
        return ftell(data->file) / 4;
    } else {
        return ftell(data->file);
    }
}

static glsi32 gli_get4(FILE *file) {
    int res;
    glui32 ch;

    res = getc(file);
    if (res == -1) {
        return -1;
    }
    ch = (res & 0xFF);
    res = getc(file);
    if (res == -1) {
        return -1;
    }
    ch = (ch << 8) | (res & 0xFF);
    res = getc(file);
    if (res == -1) {
        return -1;
    }
    ch = (ch << 8) | (res & 0xFF);
    res = getc(file);
    if (res == -1) {
        return -1;
    }
    ch = (ch << 8) | (res & 0xFF);
    if (ch > MAX_UNICHAR) {
        ch = UNKNOWN_CHAR;
    }
    return ch;
}

glsi32 fstream_getc(fstream_data_t *data)
{
    if (data->unicode) {
        return gli_get4(data->file);
    } else {
        return getc(data->file);
    }
}

glui32 fstream_read(fstream_data_t *data, char *buf, glui32 *ubuf,
        glui32 len)
{
    glui32 i;

    if (data->unicode) {
        glsi32 ch;
        for (i = 0; i < len; i++) {
            ch = gli_get4(data->file);
            if (ch == -1) {
                break;
            }
            if (ubuf) {
                ubuf[i] = ch;
            } else {
                if (ch > MAX_L1CHAR) {
                    ch = UNKNOWN_CHAR;
                }
                buf[i] = ch;
            }
        }
        return i;
    } else {
        if (ubuf) {
            for (i = 0; i < len; i++) {
                int res = getc(data->file);
                if (res == -1)
                    break;
                ubuf[i] = res;
            }
            return i;
        } else {
            return fread(buf, 1, len, data->file);
        }
    }
}

glui32 fstream_gets(fstream_data_t *data, char *buf, glui32 *ubuf,
        glui32 len)
{
    glui32 i;
    int gotnewline = FALSE;

    len -= 1;

    if (data->unicode) {
        glsi32 ch;
        for (i = 0; i < len && !gotnewline; i++) {
            ch = gli_get4(data->file);
            if (ch == -1) {
                break;
            }
            if (ubuf) {
                ubuf[i] = ch;
            } else {
                if (ch > MAX_L1CHAR) {
                    ch = UNKNOWN_CHAR;
                }
                buf[i] = ch;
            }
            gotnewline = (ch == '\n');
        }
        return i;
    } else {
        if (ubuf) {
            for (i = 0; i < len && !gotnewline; i++) {
                int res = getc(data->file);
                if (res == -1)
                    break;
                ubuf[i] = res;
                gotnewline = (res == '\n');
            }
            return i;
        } else {
            char *res;
            res = fgets(buf, len, data->file);
            if (!res) {
                return 0;
            } else {
                return strlen(buf);
            }
        }
    }
}

void fstream_putc(fstream_data_t *data, glui32 ch)
{
    if (data->unicode) {
        if (ch > MAX_UNICHAR) {
            ch = UNKNOWN_CHAR;
        }
        putc(((ch >> 24) & 0xFF), data->file);
        putc(((ch >> 16) & 0xFF), data->file);
        putc(((ch >>  8) & 0xFF), data->file);
        putc( (ch        & 0xFF), data->file);
    } else {
        if (ch > MAX_L1CHAR) {
            ch = UNKNOWN_CHAR;
        }
        putc(ch, data->file);
    }
}

void fstream_write(fstream_data_t *data, char *buf, glui32 len)
{
    if (data->unicode) {
        int i;

        for (i = 0; i < len; i++) {
            unsigned char ch = ((unsigned char *)buf)[i];
            putc(0, data->file);
            putc(0, data->file);
            putc(0, data->file);
            putc(ch, data->file);
        }
    } else {
        fwrite(buf, 1, len, data->file);
    }
}

void fstream_write_uni(fstream_data_t *data, glui32 *buf, glui32 len)
{
    glui32 i;

    for (i = 0; i < len; i++) {
        fstream_putc(data, buf[i]);
    }
}
