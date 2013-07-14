/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.activities;

import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;

import gov.in.bloomington.georeporter.R;

public class PersonalInfoActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personalinfo);
        ActionBar actionBar = getSupportActionBar();
        title = getString(R.string.menu_personal_info);
        actionBar.setTitle(title);

        
    }

    
}
