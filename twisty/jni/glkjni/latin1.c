#include "glk.h"
#include "glkjni.h"

/*
 * These variables should be persistent across invocations.
 */
static unsigned char char_tolower_table[256];
static unsigned char char_toupper_table[256];

void gli_initialize_latin1()
{
    int ix;
    int res;

    /* Initialize the to-uppercase and to-lowercase tables. These should
        *not* be localized to a platform-native character set! They are
        intended to work on Latin-1 data, and the code below correctly
        sets up the tables for that character set. */

    for (ix=0; ix<256; ix++) {
        char_toupper_table[ix] = ix;
        char_tolower_table[ix] = ix;
    }
    for (ix=0; ix<256; ix++) {
        if (ix >= 'A' && ix <= 'Z') {
            res = ix + ('a' - 'A');
        } else if (ix >= 0xC0 && ix <= 0xDE && ix != 0xD7) {
            res = ix + 0x20;
        } else {
            res = 0;
        }
        if (res) {
            char_tolower_table[ix] = res;
            char_toupper_table[res] = ix;
        }
    }
}

unsigned char glk_char_to_lower(unsigned char ch)
{
    return char_tolower_table[ch];
}

unsigned char glk_char_to_upper(unsigned char ch)
{
    return char_toupper_table[ch];
}
