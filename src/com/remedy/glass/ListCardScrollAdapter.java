package com.remedy.glass;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;

class ListCardScrollAdapter extends CardScrollAdapter {
	List<Card> cards;
	
	ListCardScrollAdapter(List<Card> cards) {
		this.cards = cards;
	}
	
	@Override
	public int findIdPosition(Object id) {
		return (Integer)id;
	}

	@Override
	public int findItemPosition(Object item) {
		return cards.indexOf(item);
	}

	@Override
	public int getCount() {
		return cards.size();
	}

	@Override
	public Object getItem(int position) {
		return cards.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return cards.get(position).toView();
	}
}