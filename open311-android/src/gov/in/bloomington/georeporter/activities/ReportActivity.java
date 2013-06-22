/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.activities;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.fragments.ChooseGroupFragment;
import gov.in.bloomington.georeporter.fragments.ChooseGroupFragment.OnGroupSelectedListener;
import gov.in.bloomington.georeporter.fragments.ChooseServiceFragment;
import gov.in.bloomington.georeporter.fragments.ChooseServiceFragment.OnServiceSelectedListener;
import gov.in.bloomington.georeporter.fragments.ReportFragment;
import gov.in.bloomington.georeporter.json.ServiceEntityJson;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.models.ServiceRequest;

import gov.in.bloomington.georeporter.util.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;

public class ReportActivity extends BaseFragmentActivity
        implements OnGroupSelectedListener,
        OnServiceSelectedListener {

    public static final int CHOOSE_LOCATION_REQUEST = 1;
    private ActionBar mActionBar;
    private ReportFragment mReportFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Report Activity", "K");
        mActionBar = getSupportActionBar();
        mActionBar.setTitle(R.string.menu_report);

        if (Open311.sGroups.size() > 1) {
            ChooseGroupFragment chooseGroup = new ChooseGroupFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, chooseGroup)
                    .addToBackStack(null)
                    .commit();
        }
        else {
            onGroupSelected(Open311.sGroups.get(0));
        }

    }

    @Override
    public void onGroupSelected(String group) {
        ChooseServiceFragment chooseService = new ChooseServiceFragment();
        chooseService.setServices(Open311.getServices(group, this));
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, chooseService)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onServiceSelected(ServiceEntityJson service) {
        mActionBar.setTitle(service.getService_name());

        ServiceRequest sr = new ServiceRequest(service, this);
        mReportFragment = ReportFragment.newInstance(sr);

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, mReportFragment)
                .addToBackStack(null)
                .commit();
    }
}
