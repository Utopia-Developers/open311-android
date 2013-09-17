/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.adapters;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import gov.in.bloomington.georeporter.fragments.ChooseServiceFragment;
import gov.in.bloomington.georeporter.models.Open311;

/**
 * Keeps track of and displays the appropriate Service List in the various groups
 */
public class GroupsFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

    private Context context;

    public GroupsFragmentStatePagerAdapter(FragmentManager fm, Context c) {
        super(fm);
        context = c;

    }

    @Override
    public int getCount() {
        return (Open311.sGroups == null) ? 0 : Open311.sGroups.size();
    }

    /**
     * Returns the appropriate ChooseServiceFragment
     */
    @Override
    public ChooseServiceFragment getItem(int position) {
        String serviceName = Open311.sGroups.get(position);
        // Log.d("Adapter",serviceName+"  "+position);
        return ChooseServiceFragment.newInstance(Open311.getServices(serviceName));
    }

    /**
     * Returns title of the group
     */
    @Override
    public CharSequence getPageTitle(int position) {
        if (Open311.sGroups == null)
            return null;
        else
            return Open311.sGroups.get(position);
    }

}
