package org.mixare.sectionedlist;

public class SectionItem implements Item {
	private String title;	
	private int markerCount;
	
	public SectionItem(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return this.title;
	}

	@Override
	public boolean isSection() {
		return true;
	}

	public int getMarkerCount() {
		return markerCount;
	}
	
	public void setMarkerCount(int markerCount) {
		this.markerCount = markerCount;
	}
}
