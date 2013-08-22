/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.fragments.ChooseGroupFragment;
import gov.in.bloomington.georeporter.fragments.ChooseGroupFragment.OnSetActionBarTitleListener;
import gov.in.bloomington.georeporter.fragments.ReportFragment;
import gov.in.bloomington.georeporter.fragments.ChooseGroupFragment.OnGroupSelectedListener;
import gov.in.bloomington.georeporter.fragments.ChooseServiceFragment.OnServiceSelectedListener;
import gov.in.bloomington.georeporter.fragments.ReportsNearYouFragment;
import gov.in.bloomington.georeporter.json.ServerAttributeJson;
import gov.in.bloomington.georeporter.json.ServiceEntityJson;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.models.Preferences;
import gov.in.bloomington.georeporter.models.ServiceRequest;

public class MainActivity extends BaseFragmentActivity implements OnSetActionBarTitleListener,
        OnGroupSelectedListener,
        OnServiceSelectedListener {

    private ServerAttributeJson current_server;
    OnDataRefreshListener mListener;
    private SlidingPaneLayout slidingPane;
    private ActionBar mActionBar;
    private ReportFragment mReportFragment;

    public interface OnDataRefreshListener {
        public void onRefreshRequested();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        slidingPane = (SlidingPaneLayout) findViewById(R.id.slidingpanelayout);
        slidingPane.openPane();
        current_server = Preferences.getCurrentServer(MainActivity.this);
        // Needs to be called to setup the Nav drawer
        super.setupNavigationDrawer();
        title = "GeoReporter";
        Open311.selectedActionPosition = totalServers + 3;
        if (current_server == null)
        {
            mListAdapter.isServerSelected = false;
            mListAdapter.notifyDataSetChanged();
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
        }
        else
        {
            Open311.sEndpoint = current_server;
            // Set the variable in the model from the server
            Open311.setCurrentServerDetails(current_server);
        }

        // Setup Fragments
        Fragment masterTemp = getSupportFragmentManager().findFragmentById(R.id.fragmentMaster);
        ChooseGroupFragment chooseGroup;
        if (masterTemp == null)
        {
            chooseGroup = new ChooseGroupFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentMaster, chooseGroup)
                    .commit();
        }
        Fragment detailTemp = getSupportFragmentManager().findFragmentById(R.id.fragmentDetail);
        ReportsNearYouFragment reportsNear;
        if (detailTemp == null)
        {
            reportsNear = new ReportsNearYouFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentDetail, reportsNear)
                    .commit();
        }

    }

    public void setActionBarTitle(String title)
    {
        this.title = title;
        if (mActionBar == null)
            mActionBar = getSupportActionBar();
        mActionBar.setTitle(this.title);
    }

    // Called whenever we call invalidateOptionsMenu()
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content
        // view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.menu_refresh).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle Closing by home button for first launch
        if (item.getItemId() == android.R.id.home && Open311.sEndpoint == null)
            return true;
        if (mDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        if (item.getItemId() == R.id.menu_refresh)
        {
            if (mListener == null)
            {

                mListener = (OnDataRefreshListener)
                        getSupportFragmentManager(
                        ).findFragmentById(R.id.fragmentMaster);
                mListener.onRefreshRequested();

            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onGroupSelected(String group, boolean single) {

    }

    @Override
    public void onServiceSelected(ServiceEntityJson service) {
        title = service.getService_name();

        if (mActionBar == null)
            mActionBar = getSupportActionBar();
        mActionBar.setTitle(title);

        ServiceRequest sr = new ServiceRequest(service, this);
        mReportFragment = ReportFragment.newInstance(sr);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentDetail, mReportFragment)
                .addToBackStack(null)
                .commit();
        slidingPane.closePane();
    }

    @Override
    public void onBackPressed() {
        int backstack = getSupportFragmentManager().getBackStackEntryCount();
        Log.d("Backstack", backstack + " " +slidingPane.isOpen());
        if (backstack == 0 && !slidingPane.isOpen())
            slidingPane.openPane();
        else
        {
            slidingPane.openPane();
            super.onBackPressed();
        }

    }

}
