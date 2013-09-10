/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.rajul.staggeredgridview.StaggeredGridView;
import com.rajul.staggeredgridview.StaggeredGridView.OnItemClickListener;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.adapters.ServicesAdapter;
import gov.in.bloomington.georeporter.json.ServiceEntityJson;

import java.util.ArrayList;

public class ChooseServiceFragment extends SherlockFragment implements OnItemClickListener {
    private static OnServiceSelectedListener mListener;
    private ArrayList<ServiceEntityJson> mServices;
    private StaggeredGridView mGridView;
    private int col_count;

    public interface OnServiceSelectedListener {
        public void onServiceSelected(ServiceEntityJson service);
    }

    public void setServices(ArrayList<ServiceEntityJson> services) {
        mServices = services;
        // Log.d("Serv2",mServices.size()+" ");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mGridView;
    }

    /**
     * @param sr
     * @return ReportFragment
     */
    public static ChooseServiceFragment newInstance(ArrayList<ServiceEntityJson> services) {
        // Log.d("Serv1",services.size()+" ");
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
        setRetainInstance(true);
        mListener = (OnServiceSelectedListener) activity;
        mGridView = new StaggeredGridView(getActivity());
        mGridView.setAdapter(new ServicesAdapter(mServices, getActivity()));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        col_count = this.getResources().getInteger(R.integer.column_no);
        mGridView.setColumnCount(col_count);
        mGridView.setOnItemClickListener(this);
        int margin = getResources().getDimensionPixelSize(R.dimen.layout_margin_small);
        mGridView.setItemMargin(margin); // set the GridView margin

        mGridView.setPadding(margin, 0, margin, 0); // have the margin on the
                                                    // sides as well

    }

    @Override
    public void onItemClick(StaggeredGridView parent, View view, int position, long id) {
        mListener.onServiceSelected(mServices.get(position));
    }

}
