#include "glk.h"
#include "nitfol.h"
#include "solve.h"
#include "globals.h"
#include "nio.h"
#include "portfunc.h"

void nitfol_z_io_c_shutdown(void);
void nitfol_op_v6_c_shutdown(void);
void nitfol_main_c_shutdown(void);
void nitfol_io_c_shutdown(void);

/*
 * Resets global/static variables. Some of this may not be needed
 * but if I had any doubts about whether a variable was read (and
 * assumed to have a default value) before written, I reset it here.
 */
void glk_shutdown() {
	/* globals.c */
	current_zfile = NULL;
	zfile_offset = 0;
	input_stream1 = NULL;
	blorb_file = NULL;
	imagecount = 0;
	interp_num = 2;
	interp_ver = 'N';
	aye_matey = FALSE;
	do_tandy = FALSE;
	do_spell_correct = TRUE;
	do_expand = TRUE;
	do_automap = TRUE;
	fullname = FALSE;
	quiet = TRUE;
	ignore_errors = FALSE;
	auto_save_undo = TRUE;
	auto_save_undo_char = FALSE;
	faked_random_seed = 0;
	stacklimit = 0;
	in_timer = FALSE;
	exit_decoder = FALSE;
	time_ret = 0;
	smart_timed = TRUE;
	lower_block_quotes = FALSE;
	read_abort = FALSE;
	has_done_save_undo = FALSE;
	allow_saveundo = TRUE;
	allow_output = TRUE;
	testing_string = FALSE;
	string_bad = FALSE;
	do_check_watches = FALSE;
	false_undo = FALSE;
	enablefont3 = FALSE;

	/* io.c */
	fgcolortable[0] = -1L;
	bgcolortable[0] = -1L;

	nitfol_io_c_shutdown();
	nitfol_z_io_c_shutdown();
	nitfol_op_v6_c_shutdown();
	nitfol_main_c_shutdown();

	automap_delete_cycles();
	n_rmfree();
}
