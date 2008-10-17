/* This file was modified from the original source, please see the
 * zplet.patch file included at the top of this source tree.
 */
/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package russotto.zplet.screenmodel;

import java.util.NoSuchElementException;
import java.util.Vector;

import com.google.twisty.zplet.ZMachineInterrupted;

class SyncVector<T> extends Vector<T> {
	private static final long serialVersionUID = 1615647740021244904L;

	public SyncVector() {
		super();
	}

	public synchronized T syncPopFirstElement() {
		T first = syncFirstElement();
		if (first != null)
				removeElementAt(0);
		return first;
	}
	
	public synchronized T syncFirstElement() {
		T first = null;
		try {
			first = super.firstElement();
		} catch (NoSuchElementException booga) {}
		try {
			if (first==null) wait();
			else return first;
		} catch (InterruptedException booga) {
			throw new ZMachineInterrupted(booga);
		}
		return null;
	}

	public synchronized void syncAddElement(T obj) {
		super.addElement(obj);
		notify();
	}
}

