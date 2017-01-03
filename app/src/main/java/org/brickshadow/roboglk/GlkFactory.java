/* This file is a part of roboglk.
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

package org.brickshadow.roboglk;


/**
 * Defines the methods needed to start an interpreter.
 * <p>
 * For example:
 * <pre>
 *   void startInterpreter() {
 *       terpThread = new Thread(new Runnable() {
 *           public void run() {
 *               if (GlkFactory.startup("progname", "story.z5") {
 *                   int err = GlkFactory.run();
 *                   if (err == 0) {
 *                       // Handle normal interpreter exit
 *                   } else {
 *                       // Handle abnormal interpreter exit
 *                   }
 *               } else {
 *                   // Handle initialization failure
 *               }
 *       });
 *       terpThread.start();
 *   }
 * </pre>
 * <p>
 * To stop the interpreter, interrupt the interpreter thread, e.g.
 * <pre>
 *   terpThread.interrupt();
 * </pre>
 */
public class GlkFactory {

    /**
     * Call this method to initialize the interpreter. It is an error
     * to pass null arguments.
     * <p>
     * <b>Note:</b> various interpreters handle initialization failures
     * in different ways; those used with Android could be patched so
     * that this method could return a string describing the cause for
     * failure.
     * 
     * @param glk the {@link Glk Glk bridge object}.
     * @param args {@code args[0]} should be the program name;
     *             {@code args[1..n]} are the options to pass to the
     *             interpreter, including the story file name (which
     *             must be the last element)
     * @return {@code true} if the interpreter was successfully
     *         initialized.
     */
    public static native boolean startup(Glk glk, String[] args);
    
    /**
     * Call ths method to start the interpreter. All Glk bridge methods
     * will be called on the thread that this method is called from.
     * 
     * @return 0 if the interpreter exited normally; 1 for abnormal
     *         termination; 2 if the interpreter thread was interrupted
     */
    public static native int run();
    

    /**
     * Call this method to cleanly shutdown the interpreter. Don't
     * forget to call it if {@link #startup(Glk, String[])} returns
     * false.
     */
    public static native void shutdown();
}
