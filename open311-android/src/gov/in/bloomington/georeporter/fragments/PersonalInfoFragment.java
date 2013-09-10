/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.TelephonyManager;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragment;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.models.Preferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class PersonalInfoFragment extends SherlockFragment {
    JSONObject mPersonalInfo = null;
    SharedPreferences mPreferences = null;

    ViewGroup linearLayout;
    EditText firstName, lastName, emaiId, phoneNo;
    public static final String[] FIELDS = {
            "first_name", "last_name", "email", "phone"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_personal_info, container, false);
        linearLayout = (ViewGroup) v.findViewById(R.id.linearLayoutContainer);

        int count = 0;
        for (int i = 0; i < linearLayout.getChildCount(); i++)
        {
            View view = linearLayout.getChildAt(i);
            if (view instanceof EditText)
            {
                switch (count) {
                    case 0:
                        firstName = (EditText) view;
                        break;
                    case 1:
                        lastName = (EditText) view;
                        break;
                    case 2:
                        emaiId = (EditText) view;
                        break;
                    case 3:
                        phoneNo = (EditText) view;
                        phoneNo.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
                        break;
                    default:
                        break;
                }
                count++;
            }
        }
        firstName.setText(mPersonalInfo.optString(FIELDS[0]));
        lastName.setText(mPersonalInfo.optString(FIELDS[1]));
        emaiId.setText(mPersonalInfo.optString(FIELDS[2]));
        phoneNo.setText(mPersonalInfo.optString(FIELDS[3]));
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPersonalInfo = Preferences.getPersonalInfo(getActivity());
        if (mPersonalInfo.toString().contentEquals("{}"))
        {

            Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
            Account[] accounts = AccountManager.get(getActivity()).getAccounts();

            for (Account account : accounts) {
                if (emailPattern.matcher(account.name).matches()) {
                    try {
                        mPersonalInfo.put("email", account.name);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            TelephonyManager telephonyManager = (TelephonyManager) getActivity()
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String phoneNo = telephonyManager.getLine1Number();
            // No Guarantee we will be able to get the no.
            if (phoneNo != null)
            {
                try {
                    mPersonalInfo.put("phone", phoneNo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onPause() {
        try {
            mPersonalInfo.put(FIELDS[0], firstName.getText().toString());
            mPersonalInfo.put(FIELDS[1], lastName.getText().toString());
            mPersonalInfo.put(FIELDS[2], emaiId.getText().toString());
            mPersonalInfo.put(FIELDS[3], phoneNo.getText().toString());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Preferences.setPersonalInfo(mPersonalInfo, getActivity());

        super.onPause();
    }

}
