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
#include <jni.h>
#include "glk.h"
#include "glkjni.h"
#include "jcall.h"

typedef struct glk_fileref_struct fileref_t;

struct glk_fileref_struct {
    glui32 rock;

    jobject file;
    char *filename;

    int filetype;
    int textmode;

    fileref_t *next, *prev;
    gidispatch_rock_t disprock;
};

/* Linked list of all filerefs */
static fileref_t *gli_filereflist = NULL;

static void gli_fileref_delete(fileref_t *fref)
{
    if (fref->file) {
        DELETE_GLOBAL(fref->file);
    }

    if (fref->filename) {
        free(fref->filename);
        fref->filename = NULL;
    }

    free(fref);
}

void fileref_c_shutdown(void)
{
    fileref_t *curr, *next;

    curr = gli_filereflist;

    while (curr) {
        next = curr->next;
        gli_fileref_delete(curr);   /* This frees curr. */
        curr = next;
    }

    gli_filereflist = NULL;
}

gidispatch_rock_t gli_fref_get_disprock(frefid_t fref)
{
    return fref->disprock;
}

void gli_fref_set_disprock(fileref_t *fref)
{
    if (gli_register_obj) {
        fref->disprock = (*gli_register_obj)(fref, gidisp_Class_Fileref);
    } else {
        fref->disprock.ptr = NULL;
    }
}

frefid_t glk_fileref_iterate(fileref_t *fref, glui32 *rock)
{
    if (!fref) {
        fref = gli_filereflist;
    } else {
        fref = fref->next;
    }

    if (fref) {
        if (rock) {
            *rock = fref->rock;
        }
        return fref;
    }

    if (rock) {
        *rock = 0;
    }
    return NULL;
}

static fileref_t *gli_fileref_new(char *filename, glui32 usage,
        glui32 rock, jobject file)
{
    fileref_t *fref;

    fref = (fileref_t *)gli_malloc(sizeof(fileref_t));

    fref->rock = rock;

    if (file) {
        fref->file = jni_new_global(file);
    } else {
        fref->file = NULL;
    }

    fref->filename = (char *)gli_malloc(1 + strlen(filename));
    strcpy(fref->filename, filename);

    fref->textmode = ((usage & fileusage_TextMode) != 0);
    fref->filetype = (usage & fileusage_TypeMask);

    return fref;
}

static fileref_t *gli_fileref_register(char *filename, glui32 usage,
        glui32 rock, jobject file)
{
    fileref_t *fref = gli_fileref_new(filename, usage, rock, file);

    fref->prev = NULL;
    fref->next = gli_filereflist;
    gli_filereflist = fref;
    if (fref->next) {
        fref->next->prev = fref;
    }

    gli_fref_set_disprock(fref);

    return fref;
}

frefid_t glk_fileref_create_temp(glui32 usage, glui32 rock)
{
    static jstring tempPrefix;

    jobject file = NULL;
    char *path;
    fileref_t *fref = NULL;

    if (!tempPrefix) {
        jstring localref;

        localref = (*jni_env)->NewStringUTF(jni_env, "glk");
        if (!localref) {
            goto done;
        }

        tempPrefix = jni_new_global(localref);
    }

    file = (*jni_env)->CallStaticObjectMethod(STATIC_M(FILE, CREATETEMP),
            tempPrefix, NULL);
    if (jni_check_exc()) {
        goto done;
    }

    (*jni_env)->CallVoidMethod(INSTANCE_M(file, FILE_DELETEONEXIT));
    jni_check_exc();

    path = jni_file_getpath(file);
    if (!path) {
        goto done;
    }

    fref = gli_fileref_register(path, usage, rock, file);
    free(path);

done:
    if (file) {
        DELETE_LOCAL(file);
    }
    if (!fref) {
        gli_strict_warning("fileref_create_temp: unable to create fileref.");
    }
    return fref;
}

frefid_t glk_fileref_create_from_fileref(glui32 usage, frefid_t oldfref,
        glui32 rock)
{
    fileref_t *fref;

    if (!oldfref) {
        gli_strict_warning("fileref_create_from_fileref: invalid ref");
        return NULL;
    }

    fref = gli_fileref_register(oldfref->filename, usage,
            rock, oldfref->file);

    if (!fref) {
        gli_strict_warning("fileref_create_from_fileref: unable to create fileref.");
    }
    return fref;
}

frefid_t glk_fileref_create_by_name(glui32 usage, char *name, glui32 rock)
{
    jstring rawname;
    jobject file = NULL;
    char *path;
    fileref_t *fref;

    rawname = jni_jstrfromnative(name);
    if (!rawname) {
        goto done;
    }

    file = (*jni_env)->CallObjectMethod(GLK_M(NAMEDFILE),
            rawname, (jint)usage);
    DELETE_LOCAL(rawname);
    if (jni_check_exc() || !file) {
        goto done;
    }

    path = jni_file_getpath(file);
    if (!path) {
        goto done;
    }

    fref = gli_fileref_register(path, usage, rock, file);
    free(path);

done:
    if (file) {
        DELETE_LOCAL(file);
    }
    if (!fref) {
        gli_strict_warning("fileref_create_by_name: unable to create fileref");
    }
    return fref;
}

frefid_t glk_fileref_create_by_prompt(glui32 usage, glui32 fmode,
        glui32 rock)
{
    jobject file = NULL;
    char *path;
    fileref_t *fref = NULL;

    if (fmode > filemode_ReadWrite && fmode != filemode_WriteAppend) {
        gli_strict_warning("fileref_create_by_prompt: invalid file mode");
        return NULL;
    }

    file = (*jni_env)->CallObjectMethod(GLK_M(PROMPTFILE),
            (jint)usage, (jint)fmode);
    if (jni_check_exc()) {
        goto done;
    }
    if (!file) {
        return NULL;
    }

    path = jni_file_getpath(file);
    if (!path) {
        goto done;
    }

    fref = gli_fileref_register(path, usage, rock, file);
    free(path);

done:
    if (file) {
        DELETE_LOCAL(file);
    }
    if (!fref) {
        gli_strict_warning("fileref_create_by_prompt: unable to create fileref");
    }
    return fref;
}

static void gli_fileref_unregister(fileref_t *fref)
{
    fileref_t *prev, *next;

    if (gli_unregister_obj) {
        (*gli_unregister_obj)(fref, gidisp_Class_Fileref, fref->disprock);
        fref->disprock.ptr = NULL;
    }

    prev = fref->prev;
    next = fref->next;
    fref->prev = NULL;
    fref->next = NULL;

    if (prev) {
        prev->next = next;
    } else {
        gli_filereflist = next;
    }
    if (next) {
        next->prev = prev;
    }

    gli_fileref_delete(fref);
}

void glk_fileref_destroy(fileref_t *fref)
{
    if (!fref) {
        gli_strict_warning("fileref_destroy: invalid ref");
        return;
    }
    gli_fileref_unregister(fref);
}

glui32 glk_fileref_get_rock(fileref_t *fref)
{
    if (!fref) {
        gli_strict_warning("fileref_get_rock: invalid ref.");
        return 0;
    }

    return fref->rock;
}

int gli_fileref_get_textmode(fileref_t *fref) {
    return fref->textmode;
}

char *gli_fileref_get_filename(fileref_t *fref) {
    return fref->filename;
}

glui32 glk_fileref_does_file_exist(fileref_t *fref)
{
    jboolean exists = FALSE;

    if (!fref) {
        gli_strict_warning("fileref_does_file_exist: invalid ref");
        return FALSE;
    }

    exists = (*jni_env)->CallBooleanMethod(
            INSTANCE_M(fref->file, FILE_EXISTS));
    if (jni_check_exc()) {
        exists = FALSE;
    }

    return exists;
}

void glk_fileref_delete_file(fileref_t *fref)
{
    if (!fref || !fref->file) {
        gli_strict_warning("fileref_delete_file: invalid ref");
        return;
    }

    (*jni_env)->CallBooleanMethod(INSTANCE_M(fref->file, FILE_DELETE));
    jni_check_exc();
}

void glkunix_set_base_file(char *filename)
{
    /*
     * Not implemented; provided for compatibility with existing
     * Unix/Glk programs (see Glk#promptFile and Glk#namedFile).
     */
}

strid_t glkunix_stream_open_pathname(char *pathname, glui32 textmode,
    glui32 rock)
{
    strid_t str = NULL;
    fileref_t *fref;
    glui32 usage;

    usage = textmode ? fileusage_TextMode : fileusage_BinaryMode;
    fref = gli_fileref_new(pathname, usage, 0, NULL);
    str = glk_stream_open_file(fref, filemode_Read, 0);
    gli_fileref_delete(fref);

    return str;
}
