/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.customviews.RoundedDrawable;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.models.ServiceRequest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class SavedReportsAdapter extends BaseAdapter {
    private ArrayList<ServiceRequest> mServiceRequests;
    private static LayoutInflater mInflater;

    private DateFormat mDateFormat;
    private SimpleDateFormat mISODate;
    private Context context;
    private RoundedDrawable defaultLogo;
    private int size;

    @SuppressLint("SimpleDateFormat")
    public SavedReportsAdapter(ArrayList<ServiceRequest> serviceRequests, Context c) {
        mServiceRequests = serviceRequests;
        mInflater = LayoutInflater.from(c);
        mDateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        mISODate = new SimpleDateFormat(Open311.DATETIME_FORMAT);
        context = c;
        size = context.getResources().getDimensionPixelSize(R.dimen.logo);
        defaultLogo = new RoundedDrawable(BitmapFactory.decodeResource(
                context.getResources(), R.drawable.ic_launcher), size, size);        
    }

    @Override
    public int getCount() {
        int count = (mServiceRequests == null) ? 0 : mServiceRequests.size();
        Log.d("Count", count + "");
        return count;
    }

    @Override
    public ServiceRequest getItem(int position) {
        ServiceRequest request = mServiceRequests.get(position);
        return request;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        TextView serviceName, status, date, address, endpoint;
        ImageView media, logo,statusImage;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_saved_reports, parent, false);
            holder = new ViewHolder();
            holder.serviceName = (TextView) convertView.findViewById(R.id.service_name);
            holder.status = (TextView) convertView.findViewById(R.id.status);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.address = (TextView) convertView.findViewById(R.id.address);
            holder.endpoint = (TextView) convertView.findViewById(R.id.endpoint);
            holder.media = (ImageView) convertView.findViewById(R.id.media);
            holder.logo = (ImageView) convertView.findViewById(R.id.imageViewEndpoint);
            holder.statusImage = (ImageView) convertView.findViewById(R.id.imageViewStatus);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        ServiceRequest sr = getItem(position);
        try {
            holder.serviceName.setText(sr.service.getService_name());
            holder.endpoint.setText(sr.endpoint.name);
            holder.address.setText(sr.post_data.optString(Open311.ADDRESS_STRING));
            holder.status.setText(sr.service_request.getStatus());
            holder.date.setText(mDateFormat.format(mISODate.parse(sr.post_data
                    .optString(ServiceRequest.REQUESTED_DATETIME))));
            holder.media.setImageBitmap(sr.getMediaBitmap(80, 80, mInflater.getContext()));
            if(!sr.service_request.getStatus().contentEquals("open"))
                holder.statusImage.setImageResource(R.drawable.closedissue);
            
            // TODO Depending on the endpoint show the endpoint logo else show default logo
            holder.logo.setImageDrawable(defaultLogo);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return convertView;
    }

    /**
     * @param serviceRequests void
     */
    public void updateSavedReports(ArrayList<ServiceRequest> serviceRequests) {
        mServiceRequests = serviceRequests;
        super.notifyDataSetChanged();
    }
}
