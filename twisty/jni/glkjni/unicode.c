#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "glk.h"
#include "glkjni.h"
#include "unigen.c"

#define CASE_UPPER (0)
#define CASE_LOWER (1)
#define CASE_TITLE (2)
#define CASE_IDENT (3)

#define COND_ALL (0)
#define COND_LINESTART (1)

static glui32 gli_buffer_change_case(glui32 *buf, glui32 len,
    glui32 numchars, int destcase, int cond, int changerest)
{
    glui32 ix, jx;
    glui32 *outbuf;
    glui32 *newoutbuf;
    glui32 outcount;
    int dest_block_rest, dest_block_first;
    int dest_spec_rest, dest_spec_first;

    switch (cond) {
    case COND_ALL:
        dest_spec_rest = destcase;
        dest_spec_first = destcase;
        break;
    case COND_LINESTART:
        if (changerest) {
            dest_spec_rest = CASE_LOWER;
        } else {
            dest_spec_rest = CASE_IDENT;
        }
        dest_spec_first = destcase;
        break;
    }

    dest_block_rest = dest_spec_rest;
    if (dest_block_rest == CASE_TITLE) {
        dest_block_rest = CASE_UPPER;
    }
    dest_block_first = dest_spec_first;
    if (dest_block_first == CASE_TITLE) {
        dest_block_first = CASE_UPPER;
    }

    newoutbuf = NULL;
    outcount = 0;
    outbuf = buf;

    for (ix=0; ix<numchars; ix++) {
        int target;
        int isfirst;
        glui32 res;
        glui32 *special;
        glui32 *ptr;
        glui32 speccount;
        glui32 ch = buf[ix];

        isfirst = (ix == 0);
        
        target = (isfirst ? dest_block_first : dest_block_rest);

        if (target == CASE_IDENT) {
            res = ch;
        } else {
            gli_case_block_t *block;

            GET_CASE_BLOCK(ch, &block);
            if (!block) {
                res = ch;
            } else {
                res = block[ch & 0xFF][target];
            }
        }

        if (res != 0xFFFFFFFF || res == ch) {
            /* simple case */
            if (outcount < len) {
                outbuf[outcount] = res;
            }
            outcount++;
            continue;
        }

        target = (isfirst ? dest_spec_first : dest_spec_rest);

        /* complicated cases */
        GET_CASE_SPECIAL(ch, &special);
        if (!special) {
            gli_strict_warning("inconsistency in cgunigen.c");
            continue;
        }
        ptr = &unigen_special_array[special[target]];
        speccount = *(ptr++);
        
        if (speccount == 1) {
            /* simple after all */
            if (outcount < len) {
                outbuf[outcount] = ptr[0];
            }
            outcount++;
            continue;
        }

        /* Now we have to allocate a new buffer, if we haven't already. */
        if (!newoutbuf) {
            newoutbuf = malloc((len+1) * sizeof(glui32));
            if (!newoutbuf) {
                return 0;
            }
            if (outcount) {
                memcpy(newoutbuf, buf, outcount * sizeof(glui32));
            }
            outbuf = newoutbuf;
        }

        for (jx=0; jx<speccount; jx++) {
            if (outcount < len) {
                outbuf[outcount] = ptr[jx];
            }
            outcount++;
        }
    }

    if (newoutbuf) {
        if (outcount) {
            memcpy(buf, newoutbuf, outcount * sizeof(glui32));
        }
        free(newoutbuf);
    }

    return outcount;
}

glui32 glk_buffer_to_lower_case_uni(glui32 *buf, glui32 len,
    glui32 numchars)
{
    return gli_buffer_change_case(buf, len, numchars, 
        CASE_LOWER, COND_ALL, TRUE);
}

glui32 glk_buffer_to_upper_case_uni(glui32 *buf, glui32 len,
    glui32 numchars)
{
    return gli_buffer_change_case(buf, len, numchars, 
        CASE_UPPER, COND_ALL, TRUE);
}

glui32 glk_buffer_to_title_case_uni(glui32 *buf, glui32 len,
    glui32 numchars, glui32 lowerrest)
{
    return gli_buffer_change_case(buf, len, numchars, CASE_TITLE, 
        COND_LINESTART, lowerrest);
}
