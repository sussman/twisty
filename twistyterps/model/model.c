#include "glk.h"

/* model.c: Model program for Glk API, version 0.5.
    Designed by Andrew Plotkin <erkyrath@netcom.com>
    http://www.eblong.com/zarf/glk/index.html
    This program is in the public domain.
*/

/* This is a simple model of a text adventure which uses the Glk API.
    It shows how to input a line of text, display results, maintain a
    status window, write to a transcript file, and so on. */

/* This is the cleanest possible form of a Glk program. It includes only
    "glk.h", and doesn't call any functions outside Glk at all. We even
    define our own str_eq() and str_len(), rather than relying on the
    standard libraries. */

#ifdef ANDROID
#include <android/log.h>
#endif

#include <stdio.h>

/* We also define our own TRUE and FALSE and NULL. */
#ifndef TRUE
#define TRUE 1
#endif
#ifndef FALSE
#define FALSE 0
#endif
#ifndef NULL
#define NULL 0
#endif

/* The story, status, and quote windows. */
static winid_t mainwin = NULL;
static winid_t statuswin = NULL;
static winid_t quotewin = NULL;

/* A file reference for the transcript file. */
static frefid_t scriptref = NULL;
/* A stream for the transcript file, when it's open. */
static strid_t scriptstr = NULL;

/* Your location. This determines what appears in the status line. */
static int current_room; 

/* A flag indicating whether you should look around. */
static int need_look; 

/* Resets static variables. */
void glk_shutdown(void)
{
    mainwin = NULL;
    statuswin = NULL;
    quotewin = NULL;
    scriptref = NULL;
    scriptstr = NULL;
    current_room = 0;
    need_look = 0;
}


/* Forward declarations */
void glk_main(void);

static void draw_statuswin(void);
static int yes_or_no(void);

static int str_eq(char *s1, char *s2);
static int str_len(char *s1);

__inline__ static void verb_help(void);
static void verb_jump(void);
static void verb_yada(int newline);
static void verb_quote(void);
static void verb_move(void);
static void verb_quit(void);
static void verb_spam(void);
static void verb_size(void);
#ifndef ANDROID
static void verb_script(void);
static void verb_unscript(void);
static void verb_save(void);
static void verb_restore(void);
#endif

typedef void glkunix_startup_t;

int glkunix_startup_code(glkunix_startup_t *data)
{
    return TRUE;
}

/* The glk_main() function is called by the Glk system; it's the main entry
    point for your program. */
void glk_main(void)
{
    /* Open the main window. */
    mainwin = glk_window_open(0, 0, 0, wintype_TextBuffer, 1);
    if (!mainwin) {
        /* It's possible that the main window failed to open. There's
            nothing we can do without it, so exit. */
        return; 
    }
    
    /* Set the current output stream to print to it. */
    glk_set_window(mainwin);
    
    /* Open a second window: a text grid, above the main window, three lines
        high. It is possible that this will fail also, but we accept that. */
    statuswin = glk_window_open(mainwin, winmethod_Above | winmethod_Fixed, 
        3, wintype_TextGrid, 0);

    /* The third window, quotewin, isn't opened immediately. We'll do
        that in verb_quote(). */

    glk_put_string("Model Glk Program\nAn Interactive Model Glk Program\n");
    glk_put_string("By Andrew Plotkin.\nRelease 7.\n");
    glk_put_string("Type \"help\" for a list of commands.\n");

    current_room = 0; /* Set initial location. */
    need_look = TRUE;
    
    while (1) {
        char commandbuf[256];
        char *cx, *cmd;
        int gotline, len;
        event_t ev;
        
        draw_statuswin();
        
        if (need_look) {
            need_look = FALSE;
            glk_put_string("\n");
            glk_set_style(style_Subheader);
            if (current_room == 0)
                glk_put_string("The Room\n");
            else
                glk_put_string("A Different Room\n");
            glk_set_style(style_Normal);
            glk_put_string("You're in a room of some sort.\n");
        }
        
        glk_put_string("\n>");
        /* We request up to 255 characters. The buffer can hold 256, but we
            are going to stick a null character at the end, so we have to
            leave room for that. Note that the Glk library does *not*
            put on that null character. */
        glk_request_line_event(mainwin, commandbuf, 255, 0);
        
        gotline = FALSE;
        while (!gotline) {
        
            /* Grab an event. */
            glk_select(&ev);
            
            switch (ev.type) {
                case evtype_LineInput:
                    if (ev.win == mainwin) {
                        gotline = TRUE;
                        /* Really the event can *only* be from mainwin,
                            because we never request line input from the
                            status window. But we do a paranoia test,
                            because commandbuf is only filled if the line
                            event comes from the mainwin request. If the
                            line event comes from anywhere else, we ignore
                            it. */
                    }
                    break;
                    
                case evtype_Arrange:
                    /* Windows have changed size, so we have to redraw the
                        status window. */
                    draw_statuswin();
                    break;
            }
        }
        
        /* commandbuf now contains a line of input from the main window.
            You would now run your parser and do something with it. */
        
        /* First, if there's a blockquote window open, let's close it. 
            This ensures that quotes remain visible for exactly one
            command. */
        if (quotewin) {
            glk_window_close(quotewin, NULL);
            quotewin = 0;
        }
        
        /* The line we have received in commandbuf is not null-terminated.
            We handle that first. */
        len = ev.val1; /* Will be between 0 and 255, inclusive. */

        commandbuf[len] = '\0';

        /* Then squash to lower-case. */
        for (cx = commandbuf; *cx; cx++) { 
            *cx = glk_char_to_lower(*cx);
        }
        
        /* Then trim whitespace before and after. */
        
        for (cx = commandbuf; *cx == ' '; cx++) { };
        
        cmd = cx;
        
        for (cx = commandbuf+len-1; cx >= cmd && *cx == ' '; cx--) { };
        *(cx+1) = '\0';
        
        /* cmd now points to a nice null-terminated string. We'll do the
            simplest possible parsing. */
        if (str_eq(cmd, "")) {
            glk_put_string("Excuse me?\n");
        }
        else if (str_eq(cmd, "help")) {
            verb_help();
        }
        else if (str_eq(cmd, "move")) {
            verb_move();
        }
        else if (str_eq(cmd, "jump")) {
            verb_jump();
        }
        else if (str_eq(cmd, "yada")) {
            verb_yada(TRUE);
        }
        else if (str_eq(cmd, "quote")) {
            verb_quote();
        }
        else if (str_eq(cmd, "quit")) {
            verb_quit();
        }
        else if (str_eq(cmd, "spam")) {
            verb_spam();
        }
        else if (str_eq(cmd, "size")) {
            verb_size();
        }
#ifndef ANDROID
        else if (str_eq(cmd, "save")) {
            verb_save();
        }
        else if (str_eq(cmd, "restore")) {
            verb_restore();
        }
        else if (str_eq(cmd, "script")) {
            verb_script();
        }
        else if (str_eq(cmd, "unscript")) {
            verb_unscript();
        }
#endif
        else {
            glk_put_string("I don't understand the command \"");
            glk_put_string(cmd);
            glk_put_string("\".\n");
        }
    }
}

static void draw_statuswin(void)
{
    char *roomname;
    glui32 width, height;
    
    if (!statuswin) {
        /* It is possible that the window was not successfully 
            created. If that's the case, don't try to draw it. */
        return;
    }
    
    if (current_room == 0)
        roomname = "The Room";
    else
        roomname = "A Different Room";
    
    glk_set_window(statuswin);
    glk_window_clear(statuswin);
    
    glk_window_get_size(statuswin, &width, &height);
    
    /* Print the room name, centered. */
    glk_window_move_cursor(statuswin, (width - str_len(roomname)) / 2, 1);
    glk_put_string(roomname);
    
    /* Draw a decorative compass rose in the upper right. */
    glk_window_move_cursor(statuswin, width - 3, 0);
    glk_put_string("\\|/");
    glk_window_move_cursor(statuswin, width - 3, 1);
    glk_put_string("-*-");
    glk_window_move_cursor(statuswin, width - 3, 2);
    glk_put_string("/|\\");
    
    glk_set_window(mainwin);
}

static int yes_or_no(void)
{
    char commandbuf[256];
    char *cx;
    int gotline, len;
    event_t ev;
    
    draw_statuswin();
    
    /* This loop is identical to the main command loop in glk_main(). */
    
    while (1) {
        glk_request_line_event(mainwin, commandbuf, 255, 0);
        
        gotline = FALSE;
        while (!gotline) {
        
            glk_select(&ev);
            
            switch (ev.type) {
                case evtype_LineInput:
                    if (ev.win == mainwin) {
                        gotline = TRUE;
                    }
                    break;
                    
                case evtype_Arrange:
                    draw_statuswin();
                    break;
            }
        }
        
        len = ev.val1;
        commandbuf[len] = '\0';
        for (cx = commandbuf; *cx == ' '; cx++) { };
        
        if (*cx == 'y' || *cx == 'Y')
            return TRUE;
        if (*cx == 'n' || *cx == 'N')
            return FALSE;
            
        glk_put_string("Please enter \"yes\" or \"no\": ");
    }
    
}

static void verb_help(void)
{
    glk_put_string("This model only understands the following commands:\n");
    glk_put_string("HELP: Display this list.\n");
    glk_put_string("JUMP: A verb which just prints some text.\n");
    glk_put_string("YADA: A verb which prints a very long stream of text.\n");
    glk_put_string("SPAM: A verb which prints a very very long stream of text.\n");
    glk_put_string("SIZE: A verb which prints the window size.\n");
    glk_put_string("MOVE: A verb which prints some text, and also changes the status line display.\n");
    glk_put_string("QUOTE: A verb which displays a block quote in a temporary third window.\n");
    glk_put_string("SCRIPT: Turn on transcripting, so that output will be echoed to a text file.\n");
    glk_put_string("UNSCRIPT: Turn off transcripting.\n");
    glk_put_string("SAVE: Write fake data to a save file.\n");
    glk_put_string("RESTORE: Read it back in.\n");
    glk_put_string("QUIT: Quit and exit.\n");
}

static void verb_jump(void)
{
    glk_put_string("You jump on the fruit, spotlessly.\n");
}

static void verb_yada(int newline)
{
    /* This is a goofy (and overly ornate) way to print a long paragraph. 
        It just shows off line wrapping in the Glk implementation. */
    #define NUMWORDS (13)
    static char *wordcaplist[NUMWORDS] = {
        "Ga", "Bo", "Wa", "Mu", "Bi", "Fo", "Za", "Mo", "Ra", "Po",
            "Ha", "Ni", "Na"
    };
    static char *wordlist[NUMWORDS] = {
        "figgle", "wob", "shim", "fleb", "moobosh", "fonk", "wabble",
            "gazoon", "ting", "floo", "zonk", "loof", "lob",
    };
    static int wcount1 = 0;
    static int wcount2 = 0;
    static int wstep = 1;
    static int jx = 0;
    int ix;
    int first = TRUE;
    
    for (ix=0; ix<85; ix++) {
        if (ix > 0) {
            glk_put_string(" ");
        }
                
        if (first) {
            glk_put_string(wordcaplist[(ix / 17) % NUMWORDS]);
            first = FALSE;
        }
        
        glk_put_string(wordlist[jx]);
        jx = (jx + wstep) % NUMWORDS;
        wcount1++;
        if (wcount1 >= NUMWORDS) {
            wcount1 = 0;
            wstep++;
            wcount2++;
            if (wcount2 >= NUMWORDS-2) {
                wcount2 = 0;
                wstep = 1;
            }
        }
        
        if ((ix % 17) == 16) {
            glk_put_string(".");
            first = TRUE;
        }
    }

    if (newline) {
        glk_put_char('\n');
    }
}

static void verb_spam(void) {
    verb_yada(FALSE);
    verb_yada(FALSE);
    verb_yada(FALSE);
    verb_yada(TRUE);
}

static void verb_quote(void)
{
    glk_put_string("Someone quotes some poetry.\n");

    /* Open a third window, or clear it if it's already open. Actually,
        since quotewin is closed right after line input, we know it
        can't be open. But better safe, etc. */
    if (!quotewin) {
        /* A five-line window above the main window, fixed size. */
        quotewin = glk_window_open(mainwin, winmethod_Above | winmethod_Fixed,
            5, wintype_TextBuffer, 0);
        if (!quotewin) {
            /* It's possible the quotewin couldn't be opened. In that
                case, just give up. */
            return;
        }
    }
    else {
        glk_window_clear(quotewin);
    }
    
    /* Print some quote. */
    glk_set_window(quotewin);
    glk_set_style(style_BlockQuote);
    glk_put_string("Tomorrow probably never rose or set\n"
        "Or went out and bought cheese, or anything like that\n"
        "And anyway, what light through yonder quote box breaks\n"
        "Handle to my hand?\n");
    glk_put_string("              -- Fred\n");
    
    glk_set_window(mainwin);
}

static void verb_move(void)
{
    current_room = (current_room+1) % 2;
    need_look = TRUE;
    
    glk_put_string("You walk for a while.\n");
}

static void verb_size(void)
{
    glui32 wid = 0;
    glui32 hgt = 0;
    char str[2];

    glk_window_get_size(mainwin, &wid, &hgt);

    glk_put_string("The width is ");
    snprintf(str, 2, "%u", wid);
    glk_put_string(str);
    glk_put_string("\nThe height is ");
    snprintf(str, 2, "%u", hgt);
    glk_put_string(str);
    glk_put_char('\n');
}

static void verb_quit(void)
{
    glk_put_string("Are you sure you want to quit? ");
    if (yes_or_no()) {
        glk_put_string("Thanks for playing.\n");
        glk_exit();
        /* glk_exit() actually stops the process; it does not return. */
    }
}

#ifndef ANDROID

static void verb_script(void)
{
    if (scriptstr) {
        glk_put_string("Scripting is already on.\n");
        return;
    }
    
    /* If we've turned on scripting before, use the same file reference; 
        otherwise, prompt the player for a file. */
    if (!scriptref) {
        scriptref = glk_fileref_create_by_prompt(
            fileusage_Transcript | fileusage_TextMode, 
            filemode_WriteAppend, 0);
        if (!scriptref) {
            glk_put_string("Unable to place script file.\n");
            return;
        }
    }
    
    /* Open the file. */
    scriptstr = glk_stream_open_file(scriptref, filemode_WriteAppend, 0);
    if (!scriptstr) {
        glk_put_string("Unable to write to script file.\n");
        return;
    }
    glk_put_string("Scripting on.\n");
    glk_window_set_echo_stream(mainwin, scriptstr);
    glk_put_string_stream(scriptstr, 
        "This is the beginning of a transcript.\n");
}

static void verb_unscript(void)
{
    if (!scriptstr) {
        glk_put_string("Scripting is already off.\n");
        return;
    }
    
    /* Close the file. */
    glk_put_string_stream(scriptstr, 
        "This is the end of a transcript.\n\n");
    glk_stream_close(scriptstr, NULL);
    glk_put_string("Scripting off.\n");
    scriptstr = NULL;
}

static void verb_save(void)
{
    int ix;
    frefid_t saveref;
    strid_t savestr;
    
    saveref = glk_fileref_create_by_prompt(
        fileusage_SavedGame | fileusage_BinaryMode, 
        filemode_Write, 0);
    if (!saveref) {
        glk_put_string("Unable to place save file.\n");
        return;
    }
    
    savestr = glk_stream_open_file(saveref, filemode_Write, 0);
    if (!savestr) {
        glk_put_string("Unable to write to save file.\n");
        glk_fileref_destroy(saveref);
        return;
    }

    glk_fileref_destroy(saveref); /* We're done with the file ref now. */
    
    /* Write some binary data. */
    for (ix=0; ix<256; ix++) {
        glk_put_char_stream(savestr, (unsigned char)ix);
    }
    
    glk_stream_close(savestr, NULL);
    
    glk_put_string("Game saved.\n");
}

static void verb_restore(void)
{
    int ix;
    int err;
    glui32 ch;
    frefid_t saveref;
    strid_t savestr;
    
    saveref = glk_fileref_create_by_prompt(
        fileusage_SavedGame | fileusage_BinaryMode, 
        filemode_Read, 0);
    if (!saveref) {
        glk_put_string("Unable to find save file.\n");
        return;
    }
    
    savestr = glk_stream_open_file(saveref, filemode_Read, 0);
    if (!savestr) {
        glk_put_string("Unable to read from save file.\n");
        glk_fileref_destroy(saveref);
        return;
    }

    glk_fileref_destroy(saveref); /* We're done with the file ref now. */
    
    /* Read some binary data. */
    err = FALSE;
    
    for (ix=0; ix<256; ix++) {
        ch = glk_get_char_stream(savestr);
        if (ch == (glui32)(-1)) {
            glk_put_string("Unexpected end of file.\n");
            err = TRUE;
            break;
        }
        if (ch != (glui32)ix) {
            glk_put_string("This does not appear to be a valid saved game.\n");
            err = TRUE;
            break;
        }
    }
    
    glk_stream_close(savestr, NULL);
    
    if (err) {
        glk_put_string("Failed.\n");
        return;
    }
    
    glk_put_string("Game restored.\n");
}

#endif

/* simple string length test */
static int str_len(char *s1)
{
    int len;
    for (len = 0; *s1; s1++)
        len++;
    return len;
}

/* simple string comparison test */
static int str_eq(char *s1, char *s2)
{
    for (; *s1 && *s2; s1++, s2++) {
        if (*s1 != *s2)
            return FALSE;
    }
    
    if (*s1 || *s2)
        return FALSE;
    else
        return TRUE;
}

