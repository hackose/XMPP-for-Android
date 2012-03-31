package xmpp.client.ui.adapter;

import xmpp.client.R;
import xmpp.client.service.user.User;
import xmpp.client.service.user.UserState;
import xmpp.client.service.user.contact.Contact;
import xmpp.client.service.user.group.GroupList;
import xmpp.client.ui.provider.ContactProvider;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.TextView;

public class RosterAdapter extends BaseAdapter {
	private static final String TAG = RosterAdapter.class.getName();
	private final Context mContext;
	private CharSequence activeGroup;
	ContactProvider mContactProvider;

	public RosterAdapter(Context context, ContactProvider contactProvider) {
		mContactProvider = contactProvider;
		mContext = context;
		activeGroup = mContext.getText(R.string.startup_group_name);
	}

	public void addRosterEntry(User re) {
		mContactProvider.add(re);
		notifyDataSetChanged();
	}

	public void delRosterEntry(String address) {
		mContactProvider.remove(address);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (activeGroup.equals(mContext.getText(R.string.all_group_name))) {
			return mContactProvider.userSize() + 1;
		} else if (activeGroup.equals(mContext
				.getText(R.string.conferences_group_name))) {
			Log.i(TAG, "getCount: conferences not yet implemented");
			return 0 + 1;
		} else if (activeGroup.equals(mContext
				.getText(R.string.online_group_name))) {
			return mContactProvider.userOnlineSize() + 1;
		} else if (activeGroup.equals(mContext
				.getText(R.string.startup_group_name))) {
			return 1;
		} else {
			return mContactProvider.userGroupSize(activeGroup) + 1;
		}
	}

	public GroupList getGroups() {
		return mContactProvider.getGroups();
	}

	@Override
	public Object getItem(int position) {
		if (position == 0) {
			return mContactProvider.getMeContact();
		}
		return getRosterItem(position - 1);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).hashCode();
	}

	private View getNormalItemView(int position, View baseView) {
		final TextView name = (TextView) baseView.findViewById(R.id.name_text);
		final TextView status = (TextView) baseView
				.findViewById(R.id.status_text);
		name.setTextColor(Color.BLACK);
		status.setTextColor(Color.BLACK);

		final Contact contact = (Contact) getItem(position);
		baseView.setBackgroundResource(R.drawable.listitem_default);

		final TextView unread = (TextView) baseView
				.findViewById(R.id.unread_text);
		if (contact.getUnreadMessages() == 0) {
			unread.setVisibility(View.GONE);
		} else {
			unread.setVisibility(View.VISIBLE);
			unread.setText("" + contact.getUnreadMessages());
		}

		final LinearLayout iconContainer = (LinearLayout) baseView
				.findViewById(R.id.icon_container);
		iconContainer.removeAllViews();
		for (final User user : contact.getUsers()) {
			if (user.isInvisible()) {
				continue;
			}
			final ImageView iview = new ImageView(mContext);
			switch (user.getTransportType()) {
			case User.TRANSPORT_ICQ:
				iview.setImageResource(R.drawable.ic_state_icq);
				break;
			case User.TRANSPORT_MSN:
				iview.setImageResource(R.drawable.ic_state_msn);
				break;
			default:
				iview.setImageResource(R.drawable.ic_state_xmpp);
				break;
			}
			switch (user.getUserState().getStatus()) {
			case UserState.STATUS_AVAILABLE:
				iview.setColorFilter(Color.parseColor(mContext
						.getString(android.R.color.holo_green_light)));
				break;
			case UserState.STATUS_AWAY:
			case UserState.STATUS_IDLE:
				iview.setColorFilter(Color.parseColor(mContext
						.getString(android.R.color.holo_orange_light)));
				break;
			case UserState.STATUS_DO_NOT_DISTURB:
				iview.setColorFilter(Color.parseColor(mContext
						.getString(android.R.color.holo_red_light)));
				break;
			case UserState.STATUS_OFFLINE:
				iview.setColorFilter(Color.parseColor(mContext
						.getString(R.color.roster_offline)));
				break;
			}
			iconContainer.addView(iview);
		}

		return baseView;
	}

	public Contact getRosterEntry(String address) {
		return mContactProvider.getContact(address);
	}

	public Contact getRosterItem(int position) {
		if (activeGroup.equals(mContext.getText(R.string.all_group_name))
				|| activeGroup.equals(mContext
						.getText(R.string.online_group_name))) {
			return mContactProvider.getContact(position);
		} else if (activeGroup.equals(mContext
				.getText(R.string.conferences_group_name))) {
			Log.i(TAG, "getRosterItem: conferences not yet implemented");
			return null;
		} else {
			return mContactProvider.getContactInGroup(activeGroup, position);
		}
	}

	private View getSelfItemView(int position, View baseView) {
		final TextView name = (TextView) baseView.findViewById(R.id.name_text);
		final TextView status = (TextView) baseView
				.findViewById(R.id.status_text);
		name.setTextColor(Color.WHITE);
		status.setTextColor(Color.WHITE);

		final Contact contact = (Contact) getItem(position);
		switch (contact.getUserState().getStatus()) {
		case UserState.STATUS_AVAILABLE:
			baseView.setBackgroundResource(R.drawable.rosteritem_highlight_online);
			break;
		case UserState.STATUS_AWAY:
		case UserState.STATUS_IDLE:
			baseView.setBackgroundResource(R.drawable.rosteritem_highlight_away);
			break;
		case UserState.STATUS_OFFLINE:
			baseView.setBackgroundResource(R.drawable.rosteritem_highlight_offline);
			break;
		case UserState.STATUS_DO_NOT_DISTURB:
			baseView.setBackgroundResource(R.drawable.rosteritem_highlight_donotdisturb);
			break;
		}

		final TextView unread = (TextView) baseView
				.findViewById(R.id.unread_text);
		unread.setVisibility(View.GONE);

		final LinearLayout iconContainer = (LinearLayout) baseView
				.findViewById(R.id.icon_container);
		iconContainer.removeAllViews();

		return baseView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rosteritem;
		final Contact contact = (Contact) getItem(position);
		if (convertView == null) {
			final LayoutInflater layoutInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rosteritem = layoutInflater.inflate(R.layout.roster_item, parent,
					false);
		} else {
			rosteritem = convertView;
		}
		final TextView name = (TextView) rosteritem
				.findViewById(R.id.name_text);
		name.setText(contact.getUserName());
		final TextView status = (TextView) rosteritem
				.findViewById(R.id.status_text);
		final CharSequence statusText = contact.getUserState().getStatusText(
				mContext);
		if (statusText != null) {
			status.setText(statusText);
		}

		status.setCompoundDrawablesWithIntrinsicBounds(contact.getUserState()
				.getStatusIconResourceID(), 0, 0, 0);

		final QuickContactBadge q = (QuickContactBadge) rosteritem
				.findViewById(R.id.user_badge);
		if (contact.getUserContact() != null) {
			q.assignContactUri(Uri.parse(contact.getUserContact()));
		}
		q.setImageBitmap(contact.getBitmap(mContext));

		if (position == 0) {
			getSelfItemView(position, rosteritem);
		} else {
			getNormalItemView(position, rosteritem);
		}

		return rosteritem;
	}

	public void setActiveGroup(String id) {
		activeGroup = id;
		notifyDataSetChanged();
	}

	public void updateRosterEntry(User re) {
		mContactProvider.update(re);
		notifyDataSetChanged();
	}

}