/**
 * Activity to display all the saved reports
 * 
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.activities;

import android.os.Bundle;

import com.actionbarsherlock.view.MenuItem;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.fragments.SavedReportsListFragment;

public class SavedReportsActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_report);
        super.setupNavigationDrawer();
        title = getString(R.string.menu_archive);
        getSupportActionBar().setTitle(title);
        if (getSupportFragmentManager().findFragmentById(R.id.content_frame) == null)
        {
            SavedReportsListFragment listFragment = new SavedReportsListFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_frame, listFragment)
                    .commit();
        }

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
