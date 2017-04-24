#include <stdio.h>
#include <string.h>

#include "glk.h"
#include "glkstart.h"

#define GLK_TERP_DEF(NAME) \
  extern glkunix_argumentlist_t glkunix_arguments_ ## NAME[]; \
  extern int glkunix_startup_code_ ## NAME (glkunix_startup_t *data); \
  extern void glk_main_ ## NAME (void); \
  extern void glk_shutdown_ ## NAME (void); \
  void run_using_ ##NAME() { \
    int argidx = 0; \
    while (glkunix_arguments_ ## NAME[argidx].argtype != glkunix_arg_End) { \
      argidx++; \
    } \
    memcpy(glkunix_arguments, glkunix_arguments_ ## NAME, \
        sizeof(glkunix_argumentlist_t) * (argidx + 1)); \
    glk_main_ ## NAME(); \
  }
 
GLK_TERP_DEF(git);
GLK_TERP_DEF(nitfol);

typedef enum {UNKNOWN, NITFOL, GIT} supported_terps_t;
supported_terps_t terp_to_use = UNKNOWN;

glkunix_argumentlist_t glkunix_arguments[100];

int glkunix_startup_code(glkunix_startup_t *data) {
  // At least an interpreter name and a path are required
  if (data->argc < 2)
    return 0;

  // Nitfol supports myriad flag commands, but expects
  // at least one non-flag argv that contains the name
  // of the file to open.
  // Git expects exactly one argv that contains the name
  // of the file to open.
  if (strcmp(data->argv[0], "nitfol") == 0) {
    terp_to_use = NITFOL;
    return glkunix_startup_code_nitfol(data);
  } else if (strcmp(data->argv[0], "git") == 0) {
    terp_to_use = GIT;
    return glkunix_startup_code_git(data);
  } else {
    return 0;
  }
}

void glk_main() {
  if (terp_to_use == GIT) {
    run_using_git();
  } else if (terp_to_use == NITFOL) {
    run_using_nitfol();
  }
}

void glk_shutdown() {
  // Obsoleted by reloading the library from the Twisty side
  if (terp_to_use == GIT) {
    glk_shutdown_git();
  } else if (terp_to_use == NITFOL) {
    glk_shutdown_nitfol();
  }
}
