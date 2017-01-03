package org.brickshadow.roboglk;

public class GlkWinDirection {
	// You cannot create these!
	// Use the static values or getInstance().
	protected GlkWinDirection(int numericValue) {
		this.numericValue = numericValue;
	}
	
	public static GlkWinDirection getInstance(int numericValue) {
		return instances[numericValue & 0x0f];
	}
	
	public int getNumericValue() { return numericValue; }
	
	public static final GlkWinDirection left = new GlkWinDirection(0) {
		@Override public String toString() { return "Left"; }
	};
	public static final GlkWinDirection right = new GlkWinDirection(1) {
		@Override public String toString() { return "Right"; }
	};
	public static final GlkWinDirection above = new GlkWinDirection(2) {
		@Override public String toString() { return "Above"; }
	};
	public static final GlkWinDirection below = new GlkWinDirection(3) {
		@Override public String toString() { return "Below"; }
	};
	
	private static final GlkWinDirection[] instances = {
			left, right, above, below
	};

	private final int numericValue;
}
