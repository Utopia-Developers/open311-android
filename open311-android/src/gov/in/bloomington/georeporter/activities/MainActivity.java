/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.activities;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import com.actionbarsherlock.app.ActionBarDrawerToggle;
import com.actionbarsherlock.view.Menu;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.models.Open311;

public class MainActivity extends BaseFragmentActivity {

    // Navigation Drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(this, getSupportActionBar(), mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
        {

            /** Called when a drawer has settled in a completely closed state. */
            @Override
            public void onDrawerClosed(View view) {
                if (Open311.sEndpoint != null)
                    getSupportActionBar().setTitle(Open311.sEndpoint.name);
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

}
