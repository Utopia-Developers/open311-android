/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.activities;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.json.ServerAttributeJson;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.models.Preferences;
import gov.in.bloomington.georeporter.util.Util;

import gov.in.bloomington.georeporter.util.json.JSONException;
import gov.in.bloomington.georeporter.util.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends BaseActivity {
    private static String SPLASH_IMAGE = "splash_image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ServerAttributeJson current_server = Preferences.getCurrentServer(this);

        if (current_server == null) {
            Log.d("Current Server Null", "K");
            startActivity(new Intent(this, SettingsActivity.class));

        } else {
            new EndpointLoader(this).execute(current_server);

            getSupportActionBar().setTitle(current_server.name);

            String imageName = current_server.splash_image;
            Log.d("Image Name", imageName + "");
            if (imageName != null) {
                ImageView splash = (ImageView) findViewById(R.id.splash);
                splash.setImageResource(getResources().getIdentifier(imageName,
                        "drawable", getPackageName()));

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
        Intent intent = new Intent(this, ReportActivity.class);
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
            super.onPreExecute();
            dialog = ProgressDialog.show(MainActivity.this, getString(R.string.dialog_loading_services), "", true);
        }

        @Override
        protected Boolean doInBackground(ServerAttributeJson... server) {
            boolean result = Open311.setEndpoint(server[0], MainActivity.this);
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            dialog.dismiss();
            if (!result) {
                Util.displayCrashDialog(MainActivity.this,
                        getString(R.string.failure_loading_services));
            } else {
                Intent intent = new Intent(this.context, ReportActivity.class);
                startActivity(intent);
            }
        }
    }
}
