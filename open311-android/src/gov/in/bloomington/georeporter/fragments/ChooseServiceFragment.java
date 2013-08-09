/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.adapters.ServicesAdapter;
import gov.in.bloomington.georeporter.json.ServiceEntityJson;

import java.util.ArrayList;

public class ChooseServiceFragment extends SherlockListFragment {
    private static OnServiceSelectedListener mListener;
    private ArrayList<ServiceEntityJson> mServices;

    public interface OnServiceSelectedListener {
        public void onServiceSelected(ServiceEntityJson service);
    }

    public void setServices(ArrayList<ServiceEntityJson> services) {
        mServices = services;
        //Log.d("Serv2",mServices.size()+" ");
    }
    
    /**
     * @param sr
     * @return ReportFragment
     */
    public static ChooseServiceFragment newInstance(ArrayList<ServiceEntityJson> services) {
        //Log.d("Serv1",services.size()+" ");
        ChooseServiceFragment fragment = new ChooseServiceFragment();
        fragment.setServices(services);
        return fragment;
    }
    
    public ChooseServiceFragment()
    {
        
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //Log.d("Serv3",mServices.size()+" ");
        setListAdapter(new ServicesAdapter(mServices, activity));
        mListener = (OnServiceSelectedListener) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setDivider(this.getResources().getDrawable(R.drawable.transperent_color));
        int margin = getResources().getDimensionPixelSize(R.dimen.layout_margin_small);
        getListView().setDividerHeight(margin/2);
        getListView().setDrawSelectorOnTop(true);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        layoutParams.setMargins(margin, margin, margin, margin);
        getListView().setLayoutParams(layoutParams);
        //setRetainInstance(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mListener.onServiceSelected(mServices.get(position));
    }

}
