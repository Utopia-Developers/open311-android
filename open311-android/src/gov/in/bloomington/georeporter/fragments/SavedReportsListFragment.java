/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.fragments;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.actionbarsherlock.app.SherlockFragment;
import com.rajul.staggeredgridview.StaggeredGridView;
import com.rajul.staggeredgridview.StaggeredGridView.OnItemClickListener;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.adapters.SavedReportsAdapter;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.models.ServiceRequest;

import java.util.ArrayList;

public class SavedReportsListFragment extends SherlockFragment implements OnItemClickListener {
    private ArrayList<ServiceRequest> mServiceRequests;
    private boolean mDataChanged = false;
    private StaggeredGridView mGridView;
    private int colCount;
    private SavedReportsAdapter adapter;
    private View layout;

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
        registerForContextMenu(mGridView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_listitem, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case R.id.menu_delete:
                mServiceRequests.remove(info.position);
                mDataChanged = true;
                refreshAdapter();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
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
}
