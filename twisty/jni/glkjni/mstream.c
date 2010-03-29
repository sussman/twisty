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

#include <stddef.h>
#include <string.h>
#include "glk.h"
#include "glkjni.h"
#include "stream.h"

struct mstream_data_struct {
    int unicode;

    void *buf;
    void *bufptr;
    void *bufend;
    void *bufeof;
    glui32 buflen;

    gidispatch_rock_t arrayrock;
};

mstream_data_t *mstream_create_data(void *buf, glui32 buflen, glui32 fmode,
        int unicode)
{
    mstream_data_t *data;

    data = (mstream_data_t *)gli_malloc(sizeof(mstream_data_t));
    data->unicode = unicode;

    if (buf && buflen) {
        data->buf = buf;
        data->bufptr = buf;
        data->buflen = buflen;
        if (unicode) {
            data->bufend = (glui32 *)buf + buflen;
        } else {
            data->bufend = (unsigned char *)buf + buflen;
        }
        if (fmode == filemode_Write) {
            data->bufeof = buf;
        }
        else {
            data->bufeof = data->bufend;
        }
    } else {
        data->buf = NULL;
        data->bufend = NULL;
        data->bufeof = NULL;
        data->bufptr = NULL;
        data->buflen = 0;
    }

    return data;
}

void mstream_register(mstream_data_t *data)
{
    char *typedesc = (data->unicode ? "&+#!Iu" : "&+#!Cn");
    data->arrayrock = (*gli_register_arr)(data->buf, data->buflen,
            typedesc);
}

void mstream_unregister(mstream_data_t *data)
{
    char *typedesc = (data->unicode ? "&+#!Iu" : "&+#!Cn");
    (*gli_unregister_arr)(data->buf, data->buflen, typedesc,
                data->arrayrock);
}

void mstream_set_pos(mstream_data_t *data, glsi32 pos, glui32 seekmode)
{
    ptrdiff_t from_current, from_eof;

    if (data->unicode) {
        from_current = ((glui32 *)data->bufptr - (glui32 *)data->buf);
        from_eof = ((glui32 *)data->bufeof - (glui32 *)data->buf);
    } else {
        from_current = ((unsigned char *)data->bufptr
                - (unsigned char *)data->buf);
        from_eof = ((unsigned char *)data->bufeof
                - (unsigned char *)data->buf);
    }

    if (seekmode == seekmode_Current) {
        pos += from_current;
    } else if (seekmode == seekmode_End) {
        pos += from_eof;
    }

    if (pos < 0) {
        pos = 0;
    } else if (pos > from_eof) {
        pos = from_eof;
    }

    if (data->unicode) {
        data->bufptr = (glui32 *)data->buf + pos;
    } else {
        data->bufptr = (unsigned char *)data->buf + pos;
    }
}

glui32 mstream_get_pos(mstream_data_t *data)
{
    if (data->unicode) {
        return ((glui32 *)data->bufptr - (glui32 *)data->buf);
    } else {
        return ((unsigned char *)data->bufptr - (unsigned char *)data->buf);
    }
}

glsi32 mstream_getc(mstream_data_t *data)
{
    glsi32 ch;

    if (data->bufptr < data->bufend) {
        if (data->unicode) {
            ch = *((glui32 *)data->bufptr);
            if (ch > MAX_UNICHAR) {
                ch = UNKNOWN_CHAR;
            }
            data->bufptr = ((glui32 *)data->bufptr) + 1;
        } else {
            ch = *((unsigned char *)data->bufptr);
            data->bufptr = ((unsigned char *)data->bufptr) + 1;
        }
    } else {
        ch = -1;
    }

    return ch;
}

static glui32 mstream_check_len(mstream_data_t *data, glui32 len)
{
    void *newbufptr;

    if (data->bufptr >= data->bufend) {
        return 0;
    }

    if (data->unicode) {
        newbufptr = (glui32 *)data->bufptr + len;
    } else {
        newbufptr = (unsigned char *)data->bufptr + len;
    }

    if (newbufptr > data->bufend) {
        glui32 lx;

        if (data->unicode) {
            lx = (glui32 *)newbufptr - (glui32 *)data->bufend;
        } else {
            lx = (unsigned char *)newbufptr - (unsigned char *)data->bufend;
        }

        if (lx < len) {
            len -= lx;
        } else {
            len = 0;
        }
    }

    return len;
}

glui32 mstream_read(mstream_data_t *data, char *buf, glui32 *ubuf,
        glui32 len)
{
    glui32 i;

    len = mstream_check_len(data, len);

    if (!len) {
        return 0;
    }

    if (data->unicode) {
        glui32 ch;
        if (ubuf) {
            for (i = 0; i < len; i++) {
                ch = ((glui32 *)data->bufptr)[i];
                if (ch > MAX_UNICHAR) {
                    ch = UNKNOWN_CHAR;
                }
                ubuf[i] = ch;
            }
        } else {
            for (i = 0; i < len; i++) {
                ch = ((glui32 *)data->bufptr)[i];
                if (ch > MAX_L1CHAR) {
                    ch = UNKNOWN_CHAR;
                }
                buf[i] = ch;
            }
        }
        data->bufptr = (glui32 *)data->bufptr + len;
    } else {
        if (ubuf) {
            for (i = 0; i < len; i++) {
                ubuf[i] = ((unsigned char *)data->bufptr)[i];
            }
        } else {
            memcpy(buf, data->bufptr, len);
        }
        data->bufptr = (unsigned char *)data->bufptr + len;
    }

    if (data->bufptr > data->bufeof) {
        data->bufeof = data->bufptr;
    }

    return len;
}

glui32 mstream_gets(mstream_data_t *data, char *buf, glui32 *ubuf,
        glui32 len)
{
    glui32 i;
    int gotnewline = FALSE;

    len = mstream_check_len(data, len - 1);

    if (!len) {
        return 0;
    }

    if (data->unicode) {
        glui32 ch;
        if (ubuf) {
            for (i = 0; i < len && !gotnewline; i++) {
                ch = ((glui32 *)data->bufptr)[i];
                if (ch > MAX_UNICHAR) {
                    ch = UNKNOWN_CHAR;
                } else {
                    gotnewline = (ch == '\n');
                }
                ubuf[i] = ch;
            }
        } else {
            for (i = 0; i < len && !gotnewline; i++) {
                ch = ((glui32 *)data->bufptr)[i];
                if (ch > MAX_L1CHAR) {
                    ch = UNKNOWN_CHAR;
                } else {
                    gotnewline = (ch == '\n');
                }
                buf[i] = ch;
            }
        }
        data->bufptr = (glui32 *)data->bufptr + i;
    } else {
        if (ubuf) {
            for (i = 0; i < len && !gotnewline; i++) {
                ubuf[i] = ((unsigned char *)data->bufptr)[i];
                gotnewline = (ubuf[i] == '\n');
            }
        } else {
            for (i = 0; i < len && !gotnewline; i++) {
                buf[i] = ((unsigned char *)data->bufptr)[i];
                gotnewline = (buf[i] == '\n');
            }
        }
        data->bufptr = (unsigned char *)data->bufptr + i;
    }

    if (data->bufptr > data->bufeof) {
        data->bufeof = data->bufptr;
    }

    return len;
}

void mstream_putc(mstream_data_t *data, glui32 ch)
{
    if (data->bufptr < data->bufend) {
        if (data->unicode) {
            if (ch > MAX_UNICHAR) {
                ch = UNKNOWN_CHAR;
            }
            *((glui32 *)data->bufptr) = ch;
            data->bufptr = ((glui32 *)data->bufptr) + 1;
        } else {
            if (ch > MAX_L1CHAR) {
                ch = UNKNOWN_CHAR;
            }
            *((unsigned char *)data->bufptr) = (unsigned char)ch;
            data->bufptr = ((unsigned char *)data->bufptr) + 1;
        }
        if (data->bufptr > data->bufeof) {
            data->bufeof = data->bufptr;
        }
    }
}

void mstream_write(mstream_data_t *data, char *buf, glui32 len)
{
    len = mstream_check_len(data, len);
    if (!len) {
        return;
    }

    if (data->unicode) {
        int i;
        for (i = 0; i < len; i++) {
            *((glui32 *)data->bufptr) = (unsigned char)(buf[i]);
            data->bufptr = (glui32 *)data->bufptr + 1;
        }
    } else {
        memcpy(data->bufptr, buf, len);
        data->bufptr = (unsigned char *)data->bufptr + len;
    }

    if (data->bufptr > data->bufeof) {
        data->bufeof = data->bufptr;
    }
}

void mstream_write_uni(mstream_data_t *data, glui32 *buf, glui32 len)
{
    glui32 i;

    for (i = 0; i < len; i++) {
        mstream_putc(data, buf[i]);
    }
}
