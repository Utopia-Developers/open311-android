/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.json.ServiceEntityJson;

import java.util.ArrayList;

public class ServicesAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<ServiceEntityJson> mServices;

    public ServicesAdapter(ArrayList<ServiceEntityJson> services, Context c) {
        mServices = services;
        mInflater = LayoutInflater.from(c);
        // Log.d("Serv1",mServices.size()+" ");
    }

    @Override
    public int getCount() {
        return mServices.size();
    }

    @Override
    public ServiceEntityJson getItem(int position) {
        return mServices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        TextView name, description;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ServiceEntityJson service = getItem(position);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_service, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.textViewHeading);
            holder.description = (TextView) convertView.findViewById(R.id.textViewDescription);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText(service.getService_name());
        holder.description.setText(service.getDescription());
        return convertView;
    }

}
