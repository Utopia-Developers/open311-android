/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.activities;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.adapters.NavigationDrawerAdapter;
import gov.in.bloomington.georeporter.fragments.MainFragment.OnSetActionBarTitleListener;
import gov.in.bloomington.georeporter.json.ServerAttributeJson;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.models.Preferences;

public class MainActivity extends BaseFragmentActivity implements OnSetActionBarTitleListener {

    private ServerAttributeJson current_server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        current_server = Preferences.getCurrentServer(MainActivity.this);
        // Needs to be called to setup the Nav drawer
        super.setupNavigationDrawer();
        title = "GeoReporter";
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

    }

    public void setActionBarTitle(String title)
    {
        this.title = title;
        getSupportActionBar().setTitle(this.title);
    }

    // Called whenever we call invalidateOptionsMenu()
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is
        // open, hide action items
        // related to the content
        // view boolean

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (mDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

}
