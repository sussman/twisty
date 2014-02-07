#include <string.h>

#include "glk.h"
#include "glkstart.h"

// TODO: Define a macro that creates the externs for the four glk names.

extern glkunix_argumentlist_t glkunix_arguments_git[];
extern int glkunix_startup_code_git(glkunix_startup_t *data);
extern void glk_main_git(void);
extern void glk_shutdown_git(void);

extern glkunix_argumentlist_t glkunix_arguments_nitfol[];
extern int glkunix_startup_code_nitfol(glkunix_startup_t *data);
extern void glk_main_nitfol(void);
extern void glk_shutdown_nitfol(void);

glkunix_argumentlist_t glkunix_arguments[10];

int glkunix_startup_code(glkunix_startup_t *data) {
  return glkunix_startup_code_git(data);
}

void glk_main() {
  // TODO(jenchen): implement.
  int argidx = 0;
  while (glkunix_arguments_git[argidx].argtype != glkunix_arg_End) {
    argidx++;
  }
  memcpy(glkunix_arguments, glkunix_arguments_git,
      sizeof(glkunix_argumentlist_t) * (argidx + 1));
  glk_main_git();
}

void glk_shutdown() {
  // TODO(jenchen): implement.
  glk_shutdown_git();
}
