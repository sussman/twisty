package org.brickshadow.roboglk;

public class GlkWinDivision {
	// You cannot create these!
	// Use the static values or getInstance().
	protected GlkWinDivision(int numericValue) {
		this.numericValue = numericValue;	
	}	

	public static GlkWinDivision getInstance(int numericValue) {
		switch (numericValue & 0xf0) {
		case 0x10: return fixed;
		case 0x20: return proportional;
		default:   return null;
		}	
	}
	
	public int getNumericValue() { return numericValue; }
	
	public static final GlkWinDivision fixed = new GlkWinDivision(0x10) {
		@Override public String toString() { return "Fixed"; }
	};
	public static final GlkWinDivision proportional = 
		new GlkWinDivision(0x20) {
			@Override public String toString() { return "Proportional"; }
	};	
	
	private final int numericValue;
}
