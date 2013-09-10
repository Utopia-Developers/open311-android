/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.rajul.staggeredgridview.StaggeredGridView;
import com.rajul.staggeredgridview.StaggeredGridView.OnItemClickListener;
import com.rajul.staggeredgridview.StaggeredGridView.OnItemLongClickListener;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.adapters.SavedReportsAdapter;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.models.ServiceRequest;

import java.util.ArrayList;

public class SavedReportsListFragment extends SherlockFragment implements OnItemClickListener,
        OnItemLongClickListener, com.actionbarsherlock.view.ActionMode.Callback {
    private ArrayList<ServiceRequest> mServiceRequests;
    private boolean mDataChanged = false;
    private StaggeredGridView mGridView;
    private int colCount;
    private SavedReportsAdapter adapter;
    private View layout;
    protected ActionMode mActionMode;
    private int position;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mServiceRequests = Open311.loadServiceRequests(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_report_saved_list, container, false);
        mGridView = (StaggeredGridView) layout.findViewById(R.id.report_list);
        adapter = new SavedReportsAdapter(mServiceRequests, getActivity());
        mGridView.setAdapter(adapter);
        return mGridView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        colCount = this.getResources().getInteger(R.integer.column_no_saved_report);
        mGridView.setColumnCount(colCount);
        mGridView.setOnItemClickListener(this);
        int margin = getResources().getDimensionPixelSize(R.dimen.layout_margin_small);
        mGridView.setItemMargin(margin); // set the GridView margin

        mGridView.setPadding(margin, 0, margin, 0); // have the margin on the
                                                    // sides as well
        mGridView.setOnItemLongClickListener(this);
    }

    @Override
    public void onPause() {
        if (mDataChanged) {
            Open311.saveServiceRequests(getActivity(), mServiceRequests);
        }
        super.onPause();
    }

    private void refreshAdapter() {
        SavedReportsAdapter a = (SavedReportsAdapter) mGridView.getAdapter();
        a.updateSavedReports(mServiceRequests);
    }

    @Override
    public void onItemClick(StaggeredGridView parent, View view, int position, long id) {
        SavedReportViewFragment fragment = SavedReportViewFragment.newInstance(position);
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onItemLongClick(StaggeredGridView parent, View view, int position, long id) {
        if (mActionMode != null) {
            return false;
        }
        mActionMode = getSherlockActivity().startActionMode(this);
        view.setSelected(true);  
        this.position = position;
        return true;
    }

    // Called when the action mode is created; startActionMode() was called
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.context_listitem, menu);
        return true;
    }

    // Called each time the action mode is shown. Always called after
    // onCreateActionMode, but may be called multiple times if the mode is invalidated.
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false; // Return false if nothing is done
    }

    // Called when the user selects a contextual menu item
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:                
                mServiceRequests.remove(position);
                mDataChanged = true;
                refreshAdapter();                        
                mode.finish(); // Action picked, so close the CAB
                return true;
            default:
                return false;
        }
    }

    // Called when the user exits the action mode
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
    }
}
