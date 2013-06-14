
package gov.in.bloomington.georeporter.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragment;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.activities.ReportActivity;
import gov.in.bloomington.georeporter.activities.SettingsActivity;
import gov.in.bloomington.georeporter.json.ServerAttributeJson;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.models.Preferences;
import gov.in.bloomington.georeporter.util.Util;

public class MainFragment extends SherlockFragment {
    private View layout;
    private static String SPLASH_IMAGE = "splash_image";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_main,
                container, false);
        setRetainInstance(true);
        return layout;
    }

    @Override
    public void onResume() {
        // Good to start task in onResume cause It garentees Activity is
        // created.
        super.onResume();

        ServerAttributeJson current_server = Preferences.getCurrentServer(getActivity());

        if (current_server == null) {
            Log.d("Current Server Null", "K");
            startActivity(new Intent(getActivity(), SettingsActivity.class));

        } else {
            new EndpointLoader(getActivity()).execute(current_server);

            getSherlockActivity().getSupportActionBar().setTitle(current_server.name);

            String imageName = current_server.splash_image;
            Log.d("Image Name", imageName + "");
            if (imageName != null) {
                ImageView splash = (ImageView) layout.findViewById(R.id.splash);
                splash.setImageResource(getResources().getIdentifier(imageName,
                        "drawable", getActivity().getPackageName()));

                splash.setContentDescription(current_server.name);

            }
        }
    }

    /**
     * OnClick handler for activity_main layout
     * 
     * @param View v
     */
    public void onTouchImage(View v) {
        Intent intent = new Intent(getActivity(), ReportActivity.class);
        startActivity(intent);
    }

    private class EndpointLoader extends
            AsyncTask<ServerAttributeJson, Void, Boolean> {
        private ProgressDialog dialog;
        Context context;

        private EndpointLoader(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(getActivity(),
                    getString(R.string.dialog_loading_services), "", true);
        }

        @Override
        protected Boolean doInBackground(ServerAttributeJson... server) {
            boolean result = Open311.setEndpoint(server[0], getActivity());
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            dialog.dismiss();
            if (!result) {
                Util.displayCrashDialog(getActivity(),
                        getString(R.string.failure_loading_services));
            } else {
                Intent intent = new Intent(this.context, ReportActivity.class);
                startActivity(intent);
            }
        }
    }

}
