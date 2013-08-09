/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.fragments.ChooseGroupFragment;
import gov.in.bloomington.georeporter.fragments.ChooseGroupFragment.OnGroupSelectedListener;
import gov.in.bloomington.georeporter.fragments.ChooseServiceFragment;
import gov.in.bloomington.georeporter.fragments.ChooseServiceFragment.OnServiceSelectedListener;
import gov.in.bloomington.georeporter.fragments.ReportFragment;
import gov.in.bloomington.georeporter.json.ServiceEntityJson;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.models.ServiceRequest;

public class ReportActivity extends BaseFragmentActivity
        implements OnGroupSelectedListener,
        OnServiceSelectedListener {

    public static final int CHOOSE_LOCATION_REQUEST = 1;
    private ActionBar mActionBar;
    private ReportFragment mReportFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        super.setupNavigationDrawer();
        title = getString(R.string.menu_report);
        mActionBar = getSupportActionBar();
        mActionBar.setTitle(title);
        if (Open311.sGroups == null)
        {

        }
        else if (Open311.sGroups.size() > 1) {
            Fragment temp = getSupportFragmentManager()
                    .findFragmentById(R.id.content_frame);
            ChooseGroupFragment chooseGroup;
            if (temp == null)
            {
                chooseGroup = new ChooseGroupFragment();
                Log.d("Frag", "Creating Grp Frag.");
                getSupportFragmentManager().beginTransaction()
                .add(R.id.content_frame, chooseGroup)
                .commit();
            }
            

        }
        else {
            onGroupSelected(Open311.sGroups.get(0), true);
        }

    }

    @Override
    public void onGroupSelected(String group, boolean single) {
        if (single == true)
        {
            ChooseServiceFragment chooseService = new ChooseServiceFragment();
            chooseService.setServices(Open311.getServices(group));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_frame, chooseService)
                    .commit();
        }

        else
        {
            Fragment temp = getSupportFragmentManager()
                    .findFragmentById(R.id.content_frame);
            ChooseServiceFragment chooseService;
            if (temp != null && temp instanceof ChooseServiceFragment)
                chooseService = (ChooseServiceFragment) temp;
            else
            {
                chooseService = new ChooseServiceFragment();
            }

            chooseService.setServices(Open311.getServices(group));
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, chooseService)
                    .addToBackStack(null)
                    .commit();
        }

    }

    @Override
    public void onServiceSelected(ServiceEntityJson service) {
        title = service.getService_name();
        mActionBar.setTitle(title);

        ServiceRequest sr = new ServiceRequest(service, this);
        mReportFragment = ReportFragment.newInstance(sr);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, mReportFragment)
                .addToBackStack(null)
                .commit();
    }
}
