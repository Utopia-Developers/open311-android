/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.ActionBarDrawerToggle;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.adapters.NavigationDrawerAdapter;
import gov.in.bloomington.georeporter.json.ServerAttributeJson;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.models.Preferences;
import gov.in.bloomington.georeporter.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseFragmentActivity extends SherlockFragmentActivity {
    // Navigation Drawer
    protected DrawerLayout mDrawerLayout;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected ListView mDrawerList;
    protected NavigationDrawerAdapter mListAdapter;
    protected String title;
    protected NavigationDrawerItemClickListener mClickListener;
    protected NavigationDrawerOnLongItemClickListener mLongClickListener;
    private final List<String> FORMAT_CHOICES = Arrays.asList(
            Open311.JSON, Open311.XML);
    private View dialogLayout;
    private LayoutInflater inflater;
    private boolean returnNow;
    private int mOrignallyAvailableServers;
    
    protected Gson gson;
    // Servers
    protected ArrayList<ServerAttributeJson> mCustomServers = null;
    /**
     * Available and Custom servers combined into one array
     */
    protected ArrayList<ServerAttributeJson> mServers = null;

    /***
     * Method to setup the Nav Drawer.Needs to be called by the child class
     * after the layout has been setup.
     */
    protected void setupNavigationDrawer()
    {
        mClickListener = new NavigationDrawerItemClickListener();
        mLongClickListener = new NavigationDrawerOnLongItemClickListener();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        inflater = getLayoutInflater();
        dialogLayout = inflater.inflate(R.layout.dialog_add_server,
                null);

        mDrawerToggle = new ActionBarDrawerToggle(this, getSupportActionBar(), mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
        {

            /** Called when a drawer has settled in a completely closed state. */
            @Override
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(title);
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
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        gson = new Gson();

        // TODO Background Thread
        mServers = gson.fromJson(
                Util.file_get_contents(this, R.raw.available_servers),
                new TypeToken<ArrayList<ServerAttributeJson>>() {
                }.getType());

        mOrignallyAvailableServers = mServers.size();

        mCustomServers = Preferences.getCustomServers(this);
        int len = mCustomServers.size();
        Log.d("Custom Server", len + " Custom Serv");
        for (int i = 0; i < len; i++) {
            mServers.add(mCustomServers.get(i));
        }

        mListAdapter = new NavigationDrawerAdapter(mServers, this);
        mDrawerList.setAdapter(mListAdapter);
        mDrawerList.setOnItemClickListener(mClickListener);
        mDrawerList.setOnItemLongClickListener(mLongClickListener);
        registerForContextMenu(mDrawerList);
    }

    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_delete:
                int position = (info.position - mOrignallyAvailableServers) - 1;
                Log.d("Delete Pos", info.position + " " + position);
                mCustomServers.remove(position);
                Preferences.setCustomServers(mCustomServers, BaseFragmentActivity.this);
                mListAdapter.removeServer(info.position, info.position - 1);
                mServers.remove(info.position - 1);
                return true;
            case android.R.id.home:
                intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true; 
            case R.id.menu_refresh:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Adds a new server JSONObject to Preferences This only updates the
     * Preferences data. You still need make sure that the screen actually
     * reflects this change.
     * 
     * @param dialog
     */
    private void saveNewServer(View dialog) {
        final EditText name = (EditText) dialog.findViewById(R.id.name);
        final EditText url = (EditText) dialog.findViewById(R.id.url);
        final EditText jurisdiction = (EditText) dialog
                .findViewById(R.id.jurisdiction);
        final EditText api_key = (EditText) dialog.findViewById(R.id.api_key);
        final ToggleButton supports_media = (ToggleButton) dialog
                .findViewById(R.id.supports_media);
        final Spinner format = (Spinner) dialog.findViewById(R.id.format);

        ServerAttributeJson server = new ServerAttributeJson();

        boolean flagValid = true;

        server.name = name.getText().toString();
        server.url = url.getText().toString();
        server.jurisdiction_id = jurisdiction.getText().toString();
        server.api_key = api_key.getText().toString();
        server.supports_media = supports_media.isChecked();
        Log.d("Selected Item", format.getSelectedItem().toString());
        server.format = format.getSelectedItem().toString();

        String error = "";

        if (!(server.url.startsWith("http://") || server.url.startsWith("https://") || server.url
                .startsWith("www.")))
        {
            flagValid = false;
            error = getString(R.string.invalid_server_url);
        }

        if (!(server.name.length() > 0))
        {
            flagValid = false;
            if (error.length() == 0)
                error = getString(R.string.invalid_server_name);
            else
                error += "\n\n" + getString(R.string.invalid_server_name);
        }

        if (flagValid)
        {
            mCustomServers.add(server);
            Preferences.setCustomServers(mCustomServers, BaseFragmentActivity.this);
            mServers.add(server);
            mListAdapter.addServer(server);
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(BaseFragmentActivity.this);
            builder.setTitle("Invalid Server Parameters");
            builder.setMessage(error);
            builder.setNegativeButton(getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

            builder.show();
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // Only show the delete menu for the custom servers.
        // Servers from available_servers cannot be deleted.
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        int reqPos = 0;
        if (mCustomServers == null)
            reqPos = (mServers.size());
        else
            reqPos = (mServers.size() - mCustomServers.size());
        if (info.position > reqPos
                && info.position <= mServers.size() + 2) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.context_listitem, menu);
        }
    }

    private class NavigationDrawerOnLongItemClickListener implements OnItemLongClickListener
    {

        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
            openContextMenu(mDrawerList);
            return true;
        }

    }

    private class NavigationDrawerItemClickListener implements OnItemClickListener
    {

        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            int position = -1;
            returnNow = false;
            Log.d("Position", pos + "  " + mServers.size());
            if (pos >= mServers.size() + 2)
                position = pos - (mServers.size() + 3);
            // Add Server
            else if (pos == (mServers.size() + 1))
            {
                // Remove the previous instance of the dialog from the
                // parent view
                if (dialogLayout.getParent() != null) {
                    ((ViewGroup) dialogLayout.getParent())
                            .removeView(dialogLayout);
                }

                final Spinner format = (Spinner) dialogLayout
                        .findViewById(R.id.format);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        BaseFragmentActivity.this,
                        android.R.layout.simple_spinner_item,
                        FORMAT_CHOICES);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                format.setAdapter(adapter);

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        BaseFragmentActivity.this);

                builder.setView(dialogLayout)
                        .setTitle(R.string.button_add_server)
                        .setPositiveButton(R.string.save,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which) {
                                        // Update the Preferences data
                                        saveNewServer(dialogLayout);
                                        // Close the dialog
                                        returnNow = true;
                                        dialog.dismiss();
                                    }
                                })
                        .setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which) {
                                        returnNow = true;
                                        dialog.cancel();
                                    }
                                });
                builder.show();
            }
            // Select Server
            else
            {
                position = pos - 1;
                ServerAttributeJson current_server = null;
                current_server = mServers.get(position);
                Preferences.setCurrentServer(current_server, BaseFragmentActivity.this);
                Open311.sEndpoint = current_server;
                returnNow = true;
                mListAdapter.selectedServerPosition = pos;
                mListAdapter.mCurrentServerURL = current_server.url;

                //First Occurrence
                if (mListAdapter.selectedView != null)
                    ((RadioButton) mListAdapter.selectedView
                            .findViewById(R.id.radioButtonServerSelect))
                            .setChecked(false);
                mListAdapter.selectedView = view;
                ((RadioButton) view.findViewById(R.id.radioButtonServerSelect)).setChecked(true);
                
                //Check for when no server is selected
                if(Open311.prevEndpoint == null)
                {
                    Intent i = new Intent(BaseFragmentActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    //To prevent the jarring effect of activity transition.
                    overridePendingTransition(0, 0);
                }
                else if(!Open311.sEndpoint.url.contentEquals(Open311.prevEndpoint))
                {
                    Intent i = new Intent(BaseFragmentActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    //To prevent the jarring effect of activity transition.
                    overridePendingTransition(0, 0);
                }
                
            }
            mDrawerLayout.closeDrawers();
            if (returnNow)
                return;
            Intent intent;
            switch (position) {
                case 0:
                    intent = new Intent(BaseFragmentActivity.this, ReportActivity.class);
                    startActivity(intent);
                    break;
                case 1:
                    intent = new Intent(BaseFragmentActivity.this, PersonalInfoActivity.class);
                    startActivity(intent);
                    break;
                case 2:
                    intent = new Intent(BaseFragmentActivity.this, SavedReportsActivity.class);
                    startActivity(intent);
                    break;
                case 3:
                    intent = new Intent(BaseFragmentActivity.this, AboutActivity.class);
                    startActivity(intent);
                    break;
            }
        }

    }
}
