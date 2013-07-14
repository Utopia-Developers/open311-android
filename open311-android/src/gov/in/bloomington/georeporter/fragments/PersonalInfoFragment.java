/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

import gov.in.bloomington.georeporter.adapters.PersonalInfoAdapter;
import gov.in.bloomington.georeporter.models.Preferences;
import gov.in.bloomington.georeporter.util.json.JSONException;
import gov.in.bloomington.georeporter.util.json.JSONObject;

import java.util.regex.Pattern;

public class PersonalInfoFragment extends SherlockListFragment {
    JSONObject mPersonalInfo = null;
    SharedPreferences mPreferences = null;
    PersonalInfoAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPersonalInfo = Preferences.getPersonalInfo(getActivity());
        if(mPersonalInfo.toString().contentEquals("{}"))
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
            //No Guarantee we will be able to get the no.
            if(phoneNo != null)
            {
                try {
                    mPersonalInfo.put("phone", phoneNo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }            
        }        
        listAdapter = new PersonalInfoAdapter(mPersonalInfo, getActivity());
        setListAdapter(listAdapter);
    }

    @Override
    public void onPause() {
        Preferences.setPersonalInfo(mPersonalInfo, getActivity());

        super.onPause();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        final String fieldname = PersonalInfoAdapter.FIELDS[position];
        final TextView label = (TextView) v.findViewById(android.R.id.text1);
        final TextView input = (TextView) v.findViewById(android.R.id.text2);

        final EditText newValue = new EditText(getActivity());
        newValue.setText(input.getText());

        int type = InputType.TYPE_TEXT_FLAG_CAP_WORDS;
        if (fieldname == "email") {
            type = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
        }
        if (fieldname == "phone") {
            type = InputType.TYPE_CLASS_PHONE;
            newValue.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        }
        newValue.setInputType(type);

        new AlertDialog.Builder(getActivity())
                .setTitle(label.getText())
                .setView(newValue)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            mPersonalInfo.put(fieldname, newValue.getText());
                            listAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            // Just ignore any errors
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do Nothing
                    }
                })
                .show();
    }
}
