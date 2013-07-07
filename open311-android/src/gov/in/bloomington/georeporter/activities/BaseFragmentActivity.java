/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBarDrawerToggle;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.adapters.NavigationDrawerAdapter;
import gov.in.bloomington.georeporter.json.ServerAttributeJson;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.models.Preferences;
import gov.in.bloomington.georeporter.util.Util;

import java.util.ArrayList;

public abstract class BaseFragmentActivity extends SherlockFragmentActivity {
    // Navigation Drawer
    protected DrawerLayout mDrawerLayout;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected ListView mDrawerList;
    protected NavigationDrawerAdapter mListAdapter;
    protected String title;

    protected Gson gson;
    // Servers
    protected ArrayList<ServerAttributeJson> mCustomServers = null;
    /**
     * Available and Custom servers combined into one array
     */
    protected ArrayList<ServerAttributeJson> mServers = null;

    /***
     * Method to setup the Nav Drawer.Needs to be called by the child class
     * after the layout has been setup.
     */
    protected void setupNavigationDrawer()
    {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerToggle = new ActionBarDrawerToggle(this, getSupportActionBar(), mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
        {

            /** Called when a drawer has settled in a completely closed state. */
            @Override
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(title);
                supportInvalidateOptionsMenu(); // creates call to
                                                // onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            @Override
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle("Navigate");
                supportInvalidateOptionsMenu(); // creates call to
                                                // onPrepareOptionsMenu()
            }

        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        gson = new Gson();

        // TODO Background Thread
        mServers = gson.fromJson(
                Util.file_get_contents(this, R.raw.available_servers),
                new TypeToken<ArrayList<ServerAttributeJson>>() {
                }.getType());

        mCustomServers = Preferences.getCustomServers(this);
        int len = mCustomServers.size();
        Log.d("Custom Server", len + " Custom Serv");
        for (int i = 0; i < len; i++) {
            mServers.add(mCustomServers.get(i));
        }

        mListAdapter = new NavigationDrawerAdapter(mServers, this);
        mDrawerList.setAdapter(mListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;

            case R.id.menu_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.menu_report:
                intent = new Intent(this, ReportActivity.class);
                startActivity(intent);
                return true;

            case R.id.menu_archive:
                intent = new Intent(this, SavedReportsActivity.class);
                startActivity(intent);
                return true;

            case R.id.menu_about:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
