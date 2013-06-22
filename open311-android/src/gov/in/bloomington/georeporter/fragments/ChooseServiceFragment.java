/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.fragments;

import gov.in.bloomington.georeporter.adapters.ServicesAdapter;
import gov.in.bloomington.georeporter.json.ServiceEntityJson;

import java.util.ArrayList;

import gov.in.bloomington.georeporter.util.json.JSONObject;

import android.app.Activity;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

public class ChooseServiceFragment extends SherlockListFragment {
    private static OnServiceSelectedListener mListener;
    private static ArrayList<ServiceEntityJson> mServices;

    public interface OnServiceSelectedListener {
        public void onServiceSelected(ServiceEntityJson service);
    }

    public void setServices(ArrayList<ServiceEntityJson> services) {
        mServices = services;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setListAdapter(new ServicesAdapter(mServices, activity));
        mListener = (OnServiceSelectedListener) activity;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mListener.onServiceSelected(mServices.get(position));
    }

}
