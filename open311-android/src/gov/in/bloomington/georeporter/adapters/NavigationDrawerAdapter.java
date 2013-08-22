
package gov.in.bloomington.georeporter.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.json.ServerAttributeJson;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.models.Preferences;

import java.util.ArrayList;
import java.util.Locale;

public class NavigationDrawerAdapter extends BaseAdapter {
    private ArrayList<ServerAttributeJson> mServers;
    private static LayoutInflater mInflater;
    public String mCurrentServerURL;
    private String[] navigationList;
    private ArrayList<AdapterList> mList;
    private AdapterList tempItem;
    public int selectedServerPosition = -1;
    public View selectedView;

    // Differant list layouts possible
    private final int TYPE_LIST_HEADER_SERVER = 0;
    private final int TYPE_LIST_SERVER = 1;
    private final int TYPE_LIST_ACTION_ADD_SERVER = 2;
    private final int TYPE_LIST_HEADER_ACTION = 3;
    private final int TYPE_LIST_ACTION = 4;

    private ServerAttributeJson currentServer;
    public boolean isServerSelected;
    public Context context;

    public NavigationDrawerAdapter(ArrayList<ServerAttributeJson> d, Context c) {
        mServers = d;
        mInflater = LayoutInflater.from(c);
        context = c;
        navigationList = c.getResources().getStringArray(R.array.navigation_list);
        mList = new ArrayList<NavigationDrawerAdapter.AdapterList>();
        // First Item
        tempItem = new AdapterList();
        tempItem.name = navigationList[0];
        tempItem.layoutId = TYPE_LIST_HEADER_SERVER;
        mList.add(tempItem);

        ServerAttributeJson serverTemp;
        // All Servers
        for (int j = 0; j < mServers.size(); j++)
        {
            tempItem = new AdapterList();
            serverTemp = mServers.get(j);
            tempItem.name = serverTemp.name;
            tempItem.url = serverTemp.url;
            tempItem.layoutId = TYPE_LIST_SERVER;
            mList.add(tempItem);
        }

        // Remaining actions
        for (int i = 1; i < navigationList.length; i++)
        {
            tempItem = new AdapterList();
            tempItem.name = navigationList[i];
            tempItem.layoutId = TYPE_LIST_HEADER_ACTION;
            switch (i)
            {
                case 1:
                    tempItem.itemResource = R.drawable.ic_action_add_server;
                    tempItem.layoutId = TYPE_LIST_ACTION_ADD_SERVER;
                    break;
                case 3:
                    // TODO Selected Item Resource
                    tempItem.itemResource = R.drawable.ic_action_home;
                    tempItem.itemResourceSelected = R.drawable.ic_action_home_selected;
                    tempItem.layoutId = TYPE_LIST_ACTION;
                    break;
                case 4:
                    tempItem.itemResource = R.drawable.ic_action_personal_info;
                    tempItem.itemResourceSelected = R.drawable.ic_action_personal_info_selected;
                    tempItem.layoutId = TYPE_LIST_ACTION;
                    break;
                case 5:
                    tempItem.itemResource = R.drawable.ic_action_report;
                    tempItem.itemResourceSelected = R.drawable.ic_action_report_selected;
                    tempItem.layoutId = TYPE_LIST_ACTION;
                    break;
                case 6:
                    tempItem.itemResource = R.drawable.ic_action_about;
                    tempItem.itemResourceSelected = R.drawable.ic_action_about_selected;
                    tempItem.layoutId = TYPE_LIST_ACTION;
                    break;
            }

            mList.add(tempItem);
        }
        currentServer = Preferences.getCurrentServer(c);
        if (currentServer == null)
            isServerSelected = false;
        else
            isServerSelected = true;
        mCurrentServerURL = currentServer == null ? "" : currentServer.url;

    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        AdapterList tempItem = mList.get(position);
        if (isServerSelected
                && (tempItem.layoutId == TYPE_LIST_HEADER_ACTION || tempItem.layoutId == TYPE_LIST_HEADER_SERVER))
            return false;
        else if (isServerSelected)
            return true;
        // You should be able to add servers at all times.
        else if (tempItem.layoutId == TYPE_LIST_SERVER || position == mServers.size() + 1)
            return true;
        else
            return false;
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).layoutId;
    }

    public void addServer(ServerAttributeJson server)
    {
        tempItem = new AdapterList();
        tempItem.name = server.name;
        tempItem.url = server.url;
        tempItem.layoutId = TYPE_LIST_SERVER;
        // Log.d("Position Stored", mServers.size() +" "+mServers.size());
        mList.add(mServers.size(), tempItem);
        // logMlistEntirely();
        notifyDataSetChanged();
        mServers.add(server);
    }

    public void logMlistEntirely()
    {
        AdapterList temp;
        for (int i = 0; i < mList.size(); i++)
        {
            temp = mList.get(i);
            Log.d("MList", temp.name + " " + temp.layoutId + " " + i + "  " + mServers.size());
        }

    }

    public void removeServer(int listPosition, int serverPosition)
    {
        mList.remove(listPosition);
        mServers.remove(serverPosition);
        notifyDataSetChanged();
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }

    @Override
    public int getCount() {
        return (mList == null) ? 0 : mList.size();
    }

    @Override
    public ServerAttributeJson getItem(int position) {
        return mServers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View,
     * android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int type = getItemViewType(position);
        if (convertView == null) {
            holder = new ViewHolder();
            switch (type)
            {
                case TYPE_LIST_HEADER_SERVER:

                    convertView = mInflater.inflate(R.layout.list_item_navdrawer_header_server,
                            parent,
                            false);

                    holder.name = (TextView) convertView.findViewById(R.id.textViewHeader);
                    convertView.setTag(holder);
                    break;
                case TYPE_LIST_HEADER_ACTION:

                    convertView = mInflater.inflate(R.layout.list_item_navdrawer_header_action,
                            parent,
                            false);
                    holder.name = (TextView) convertView.findViewById(R.id.textViewHeader);
                    convertView.setTag(holder);
                    break;
                case TYPE_LIST_SERVER:
                    convertView = mInflater.inflate(R.layout.list_item_navdrawer_server, parent,
                            false);
                    holder.name = (TextView) convertView.findViewById(R.id.textViewServerName);
                    holder.url = (TextView) convertView.findViewById(R.id.textViewServerUrl);
                    holder.selectedView = convertView
                            .findViewById(R.id.viewSelected);
                    convertView.setTag(holder);
                    break;
                case TYPE_LIST_ACTION_ADD_SERVER:
                    convertView = mInflater.inflate(R.layout.list_item_navdrawer_action_add_server,
                            parent,
                            false);
                    holder.name = (TextView) convertView.findViewById(R.id.textViewAction);
                    holder.image = (ImageView) convertView.findViewById(R.id.imageViewAction);
                    convertView.setTag(holder);
                    break;
                case TYPE_LIST_ACTION:
                    convertView = mInflater.inflate(R.layout.list_item_navdrawer_action, parent,
                            false);
                    holder.selectedView = convertView.findViewById(R.id.viewActionSelected);
                    holder.name = (TextView) convertView.findViewById(R.id.textViewAction);
                    holder.image = (ImageView) convertView.findViewById(R.id.imageViewAction);
                    convertView.setTag(holder);
                    break;
            }
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        switch (type)
        {
            case TYPE_LIST_HEADER_SERVER:
            case TYPE_LIST_HEADER_ACTION:
                holder.name.setText(mList.get(position).name);
                break;
            case TYPE_LIST_SERVER:
                String name = mList.get(position).name;
                String url = mList.get(position).url;
                if (url.equals(mCurrentServerURL)) {
                    holder.selectedView.setBackgroundColor(context.getResources().getColor(
                            R.color.navdrawer_server_selected_colour));
                    holder.name.setTextColor(context.getResources().getColor(
                            R.color.navdrawer_server_selected_colour));
                    selectedServerPosition = position;
                    selectedView = convertView;
                }
                else {
                    holder.selectedView.setBackgroundColor(context.getResources().getColor(
                            R.color.transparent));
                    holder.name.setTextColor(context.getResources().getColor(
                            R.color.navdrawer_text_colour));
                }
                holder.name.setText(name.toUpperCase());
                holder.url.setText(url);
                break;
            case TYPE_LIST_ACTION:
                holder.name.setText(mList.get(position).name);
                if (position == Open311.selectedActionPosition)
                {
                    holder.image.setBackgroundResource(mList.get(position).itemResourceSelected);
                    holder.name.setTextColor(context.getResources().getColor(
                            R.color.text_colour_section_header));
                    holder.selectedView.setBackgroundColor(context.getResources().getColor(
                            R.color.text_colour_section_header));
                }
                else
                {
                    holder.image.setBackgroundResource(mList.get(position).itemResource);
                    holder.name.setTextColor(context.getResources().getColor(
                            R.color.navdrawer_text_colour));
                    holder.selectedView.setBackgroundColor(context.getResources().getColor(
                            R.color.transparent));
                }
                break;
            case TYPE_LIST_ACTION_ADD_SERVER:
                holder.name.setText(mList.get(position).name);
                holder.image.setBackgroundResource(mList.get(position).itemResource);
                break;
        }

        return convertView;
    }

    private static class ViewHolder {
        public TextView name, url;
        public View selectedView;
        public ImageView image;
    }

    /**
     * The Class which holds the entire Navigation List attributes
     */
    private class AdapterList
    {
        public String name;
        public String url;
        public int itemResource, itemResourceSelected;
        public int layoutId;

        public AdapterList() {

        }

        public AdapterList(int itemResource)
        {
            this.itemResource = itemResource;
        }
    }
}
