package com.google.code.twisty.zplet;

public class ZMachineInterrupted extends RuntimeException {
	private static final long serialVersionUID = -8679657711588998700L;

	public ZMachineInterrupted() {
	}

	public ZMachineInterrupted(String detailMessage) {
		super(detailMessage);
	}

	public ZMachineInterrupted(Throwable throwable) {
		super(throwable);
	}

	public ZMachineInterrupted(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
