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

#ifndef STREAM_H_
#define STREAM_H_

#include <stdio.h>

typedef struct mstream_data_struct mstream_data_t;
typedef struct fstream_data_struct fstream_data_t;

mstream_data_t *mstream_create_data(void *buf, glui32 buflen, glui32 fmode,
        int unicode);
void mstream_register(mstream_data_t *data);
void mstream_unregister(mstream_data_t *data);
void mstream_set_pos(mstream_data_t *data, glsi32 pos, glui32 seekmode);
glui32 mstream_get_pos(mstream_data_t *data);
glsi32 mstream_getc(mstream_data_t *data);
glui32 mstream_read(mstream_data_t *data, char *buf, glui32 *ubuf,
        glui32 len);
glui32 mstream_gets(mstream_data_t *data, char *buf, glui32 *ubuf,
        glui32 len);
void mstream_putc(mstream_data_t *data, glui32 ch);
void mstream_write(mstream_data_t *data, char *buf, glui32 len);
void mstream_write_uni(mstream_data_t *data, glui32 *buf, glui32 len);

fstream_data_t *fstream_create_data(FILE *fl, int textmode, int unicode);
void fstream_delete(fstream_data_t *data);
void fstream_set_pos(fstream_data_t *data, glsi32 pos, glui32 seekmode);
glui32 fstream_get_pos(fstream_data_t *data);
glsi32 fstream_getc(fstream_data_t *data);
glui32 fstream_read(fstream_data_t *data, char *buf, glui32 *ubuf,
        glui32 len);
glui32 fstream_gets(fstream_data_t *data, char *buf, glui32 *ubuf,
        glui32 len);
void fstream_putc(fstream_data_t *data, glui32 ch);
void fstream_write(fstream_data_t *data, char *buf, glui32 len);
void fstream_write_uni(fstream_data_t *data, glui32 *buf, glui32 len);

#endif /* STREAM_H_ */
