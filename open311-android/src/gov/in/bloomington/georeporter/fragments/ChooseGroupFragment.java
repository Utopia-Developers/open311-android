/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.adapters.GroupsFragmentStatePagerAdapter;

public class ChooseGroupFragment extends SherlockFragment {
    OnGroupSelectedListener mListener;
    View layout;
    GroupsFragmentStatePagerAdapter adapter;
    ViewPager pager;
    PagerTabStrip tabStrip;
    public interface OnGroupSelectedListener {
        public void onGroupSelected(String group,boolean single);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);        
        mListener = (OnGroupSelectedListener) activity;        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_choose_group, container, false);
        adapter = new GroupsFragmentStatePagerAdapter(getFragmentManager(), getActivity());
        pager = (ViewPager) layout.findViewById(R.id.pager);
        tabStrip = (PagerTabStrip) layout.findViewById(R.id.pager_tab_strip);
        pager.setAdapter(adapter);
        tabStrip.setTabIndicatorColorResource(R.color.actionbar_background_colour);
        return layout;
    }
    
    
    
    
}
