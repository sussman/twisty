/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package russotto.zplet.zmachine.state;

class ZSFrame {
  // FileDialog fd;
  boolean ran = false;

  // int boxtype = FileDialog.SAVE;
  Thread blockedthread = null;

  public ZSFrame() {
    // super();
  }

  public ZSFrame(String title) {
    // super(title);
  }

  public ZSFrame(String title, int boxtype) {
    this(title);
    // this.boxtype = boxtype;
  }

  /*
         * MM TODO public boolean handleEvent(Event evt) { if (!ran) { fd = new
         * FileDialog(this, "Save game as...", boxtype); fd.show(); } ran =
         * true; if (blockedthread != null) blockedthread.resume(); return
         * super.handleEvent(evt); }
         * 
         * public String getFile() { if (!ran) { blockedthread =
         * Thread.currentThread(); blockedthread.suspend(); // race conditions
         * galore } return fd.getFile(); }
         * 
         * public String getDirectory() { if (!ran) { blockedthread =
         * Thread.currentThread(); blockedthread.suspend(); // race conditions
         * galore } return fd.getDirectory(); }
         */
}

/** ZState holds the state-of-play for the Z machine */
