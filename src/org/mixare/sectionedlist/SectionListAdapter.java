/**
 * Copyright (c) 2011, Polidea
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.mixare.sectionedlist;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.mixare.R;

import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

/**
 * Adapter for sections.
 */
public class SectionListAdapter extends BaseAdapter implements ListAdapter,
		OnItemClickListener {
	private final DataSetObserver dataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			super.onChanged();
			updateSessionCache();
		}

		@Override
		public void onInvalidated() {
			super.onInvalidated();
			updateSessionCache();
		};
	};

	private final ListAdapter linkedAdapter;
	private final Map<Integer, String> sectionPositions = new LinkedHashMap<Integer, String>();
	private final Map<Integer, Integer> itemPositions = new LinkedHashMap<Integer, Integer>();
	private final Map<View, String> currentViewSections = new HashMap<View, String>();
	private int viewTypeCount;
	protected final LayoutInflater inflater;

	private View transparentSectionView;

	private OnItemClickListener linkedListener;

	public SectionListAdapter(final LayoutInflater inflater,
			final ListAdapter linkedAdapter) {
		this.linkedAdapter = linkedAdapter;
		this.inflater = inflater;
		linkedAdapter.registerDataSetObserver(dataSetObserver);
		updateSessionCache();
	}

	private boolean isTheSame(final String previousSection,
			final String newSection) {
		if (previousSection == null) {
			return newSection == null;
		} else {
			return previousSection.equals(newSection);
		}
	}

	private synchronized void updateSessionCache() {
		int currentPosition = 0;
		sectionPositions.clear();
		itemPositions.clear();
		viewTypeCount = linkedAdapter.getViewTypeCount() + 1;
		String currentSection = null;
		final int count = linkedAdapter.getCount();
		for (int i = 0; i < count; i++) {
			final SectionedListItem item = (SectionedListItem) linkedAdapter
					.getItem(i);
			if (!isTheSame(currentSection, item.getSection())) {
				sectionPositions.put(currentPosition, item.getSection());
				currentSection = item.getSection();
				currentPosition++;
			}
			itemPositions.put(currentPosition, i);
			currentPosition++;
		}
	}

	@Override
	public synchronized int getCount() {
		return sectionPositions.size() + itemPositions.size();
	}

	@Override
	public synchronized Object getItem(final int position) {
		if (isSection(position)) {
			return sectionPositions.get(position);
		} else {
			final int linkedItemPosition = getLinkedPosition(position);
			return linkedAdapter.getItem(linkedItemPosition);
		}
	}

	public synchronized boolean isSection(final int position) {
		return sectionPositions.containsKey(position);
	}

	public synchronized String getSectionName(final int position) {
		if (isSection(position)) {
			return sectionPositions.get(position);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(final int position) {
		if (isSection(position)) {
			return sectionPositions.get(position).hashCode();
		} else {
			return linkedAdapter.getItemId(getLinkedPosition(position));
		}
	}

	protected Integer getLinkedPosition(final int position) {
		return itemPositions.get(position);
	}

	@Override
	public int getItemViewType(final int position) {
		if (isSection(position)) {
			return viewTypeCount - 1;
		}
		return linkedAdapter.getItemViewType(getLinkedPosition(position));
	}

	private View getSectionView(final View convertView, final String section) {
		View theView = convertView;
		if (theView == null) {
			theView = createNewSectionView();
		}
		setSectionText(section, theView);
		replaceSectionViewsInMaps(section, theView);
		return theView;
	}

	protected void setSectionText(final String section, final View sectionView) {
		final TextView textView = (TextView) sectionView
				.findViewById(R.id.listTextView);
		textView.setText(section);
	}

	protected synchronized void replaceSectionViewsInMaps(final String section,
			final View theView) {
		if (currentViewSections.containsKey(theView)) {
			currentViewSections.remove(theView);
		}
		currentViewSections.put(theView, section);
	}

	protected View createNewSectionView() {
		return inflater.inflate(R.layout.section_view, null);
	}

	@Override
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {
		if (isSection(position)) {
			return getSectionView(convertView, sectionPositions.get(position));
		}
		return linkedAdapter.getView(getLinkedPosition(position), convertView,
				parent);
	}

	@Override
	public int getViewTypeCount() {
		return viewTypeCount;
	}

	@Override
	public boolean hasStableIds() {
		return linkedAdapter.hasStableIds();
	}

	@Override
	public boolean isEmpty() {
		return linkedAdapter.isEmpty();
	}

	@Override
	public void registerDataSetObserver(final DataSetObserver observer) {
		linkedAdapter.registerDataSetObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(final DataSetObserver observer) {
		linkedAdapter.unregisterDataSetObserver(observer);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return linkedAdapter.areAllItemsEnabled();
	}

	@Override
	public boolean isEnabled(final int position) {
		if (isSection(position)) {
			return true;
		}
		return linkedAdapter.isEnabled(getLinkedPosition(position));
	}

	public void makeSectionInvisibleIfFirstInList(final int firstVisibleItem) {
		final String section = getSectionName(firstVisibleItem);
		// only make invisible the first section with that name in case there
		// are more with the same name
		boolean alreadySetFirstSectionIvisible = false;
		for (final Entry<View, String> itemView : currentViewSections
				.entrySet()) {
			if (itemView.getValue().equals(section)
					&& !alreadySetFirstSectionIvisible) {
				itemView.getKey().setVisibility(View.INVISIBLE);
				alreadySetFirstSectionIvisible = true;
			} else {
				itemView.getKey().setVisibility(View.VISIBLE);
			}
		}
		for (final Entry<Integer, String> entry : sectionPositions.entrySet()) {
			if (entry.getKey() > firstVisibleItem) {
				break;
			}
			setSectionText(entry.getValue(), getTransparentSectionView());
		}
	}

	public synchronized View getTransparentSectionView() {
		if (transparentSectionView == null) {
			transparentSectionView = createNewSectionView();
		}
		return transparentSectionView;
	}

	protected void sectionClicked(final String section) {
		// do nothing
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view,
			final int position, final long id) {
		if (isSection(position)) {
			sectionClicked(getSectionName(position));
		} else if (linkedListener != null) {
			linkedListener.onItemClick(parent, view,
					getLinkedPosition(position), id);
		}
	}

	public void setOnItemClickListener(final OnItemClickListener linkedListener) {
		this.linkedListener = linkedListener;
	}
}
