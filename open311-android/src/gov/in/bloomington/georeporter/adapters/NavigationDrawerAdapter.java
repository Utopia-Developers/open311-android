package gov.in.bloomington.georeporter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.json.ServerAttributeJson;
import gov.in.bloomington.georeporter.models.Preferences;

import java.util.ArrayList;

public class NavigationDrawerAdapter extends BaseAdapter{
    private ArrayList<ServerAttributeJson> mServers;
    private static LayoutInflater mInflater;
    private String mCurrentServerURL;
    private String [] navigationList;
    private ArrayList<AdapterList> mList;
    private AdapterList tempItem;
    
    //Differant list layouts possible
    private final int TYPE_LIST_HEADER = 0;
    private final int TYPE_LIST_SERVER = 1;
    private final int TYPE_LIST_ACTION = 2;

    public NavigationDrawerAdapter(ArrayList<ServerAttributeJson> d, Context c) {
        mServers = d;
        mInflater = LayoutInflater.from(c);
        navigationList = c.getResources().getStringArray(R.array.navigation_list);   
        mList = new ArrayList<NavigationDrawerAdapter.AdapterList>();
        //First Item
        tempItem = new AdapterList();
        tempItem.name = navigationList[0];
        mList.add(tempItem);
        
        ServerAttributeJson serverTemp;        
        //All Servers
        for(int j=0;j<mServers.size();j++)
        {
            tempItem = new AdapterList();
            serverTemp = mServers.get(j);
            tempItem.name = serverTemp.name;
            tempItem.url = serverTemp.url;
            mList.add(tempItem);
        }
        
        //Remaining actions
        for(int i=1;i<navigationList.length;i++)
        {
            tempItem = new AdapterList();
            tempItem.name = navigationList[i];
            switch(i)
            {
                case 1:
                    tempItem.itemResource = R.drawable.ic_menu_start_conversation;
                    break;
                case 3:
                    tempItem.itemResource = R.drawable.ic_menu_start_conversation;
                    break;
                case 4:
                    tempItem.itemResource = R.drawable.ic_menu_start_conversation;
                    break;
                case 5:
                    tempItem.itemResource = R.drawable.ic_menu_archive;
                    break;
                case 6:
                    tempItem.itemResource = R.drawable.ic_menu_start_conversation;
                    break;
                    
            }
            mList.add(tempItem);
        }
        ServerAttributeJson currentServer = Preferences.getCurrentServer(c);
        mCurrentServerURL = currentServer == null ? "" : currentServer.url;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0)
            return TYPE_LIST_HEADER;
        else if(position <= (mServers.size()))
            return TYPE_LIST_SERVER;
        else if(position == (mServers.size()+1))
            return TYPE_LIST_ACTION;
        else if(position == (mServers.size()+2))
            return TYPE_LIST_HEADER;
        else 
            return TYPE_LIST_ACTION;
    }
    
    public void addServer(ServerAttributeJson server)
    {
        tempItem = new AdapterList();
        tempItem.name = server.name;
        tempItem.url = server.url;
        mList.add(mServers.size()+1, tempItem);
        mServers.add(server);
        notifyDataSetChanged();
    }

    @Override
    public int getViewTypeCount() {
        return 3;
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
            switch(type)
            {
                case TYPE_LIST_HEADER:
                    convertView = mInflater.inflate(R.layout.list_item_navdrawer_header, parent,false);
                    holder.name = (TextView) convertView.findViewById(R.id.textViewHeader);
                    convertView.setTag(holder);
                    break;
                case TYPE_LIST_SERVER:
                    convertView = mInflater.inflate(R.layout.list_item_navdrawer_server, parent,false);
                    holder.name = (TextView) convertView.findViewById(R.id.textViewServerName);
                    holder.url = (TextView) convertView.findViewById(R.id.textViewServerUrl);
                    holder.radio = (RadioButton) convertView.findViewById(R.id.radioButtonServerSelect);
                    convertView.setTag(holder);
                    break;
                case TYPE_LIST_ACTION:
                    convertView = mInflater.inflate(R.layout.list_item_navdrawer_action, parent,false);
                    holder.name = (TextView) convertView.findViewById(R.id.textViewAction);
                    holder.image = (ImageView) convertView.findViewById(R.id.imageViewAction);
                    convertView.setTag(holder);
                    break;
            }           
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        switch(type)
        {
            case TYPE_LIST_HEADER:
                holder.name.setText(mList.get(position).name);
                break;
            case TYPE_LIST_SERVER:
                String name = mList.get(position).name;
                String url = mList.get(position).url;
                if (url.equals(mCurrentServerURL)) {
                    holder.radio.setChecked(true);
                }
                else {
                    holder.radio.setChecked(false);
                }
                holder.name.setText(name);
                holder.url.setText(url);
                break;
            case TYPE_LIST_ACTION:
                holder.name.setText(mList.get(position).name);
                holder.image.setBackgroundResource(mList.get(position).itemResource);
                break;
        }  

        
        return convertView;
    }
    
    private static class ViewHolder {
        public TextView name, url;
        public RadioButton radio;
        public ImageView image;
    }
    
    /**
     * The Class which holds the entire Navigation List attributes
    */
    private class AdapterList
    {
        public String name;
        public String url;
        public int itemResource;
        public AdapterList() {
            
        }
        public AdapterList(int itemResource)
        {
            this.itemResource = itemResource;
        }
    }
}
