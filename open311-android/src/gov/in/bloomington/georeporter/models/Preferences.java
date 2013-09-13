/**
 * A static helper class for interacting with SharedPreferences
 * 
 * This class should handle all interactions with SharedPreferences
 *
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.json.ServerAttributeJson;
import gov.in.bloomington.georeporter.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Preferences {
    private static final String SETTINGS = "settings";
    private static final String PERSONAL_INFO = "personal_info";
    private static final String CUSTOM_SERVERS = "custom_servers";

    private static final String APP_STATE = "app_state";
    private static final String CURRENT_SERVER = "current_server";

    private static Gson gson = null;

    private static SharedPreferences mSettings = null;
    private static SharedPreferences mState = null;

    private static void loadSettings(Context c) {
        if (mSettings == null) {
            mSettings = c.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        }
    }

    private static void loadState(Context c) {
        if (mState == null) {
            mState = c.getSharedPreferences(APP_STATE, Context.MODE_PRIVATE);
        }
    }

    /**
     * Returns the personal_info fields stored in settings This should always
     * return a valid JSONObject. The JSONObject may be empty, but it needs to
     * be ready for the user to start filling out the fields.
     * 
     * @param c
     * @return JSONObject
     */
    public static JSONObject getPersonalInfo(Context c) {
        Preferences.loadSettings(c);
        try {
            return new JSONObject(mSettings.getString(PERSONAL_INFO, "{}"));
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Writes the personal info fields to disk
     * 
     * @param personal_info
     * @param c
     */
    public static void setPersonalInfo(JSONObject personal_info, Context c) {
        Preferences.loadSettings(c);
        Open311.mPersonalInfo = personal_info;
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(PERSONAL_INFO, personal_info.toString());
        editor.commit();
    }

    /**
     * Returns any custom server definitions stored in settings Users can add
     * additional servers to the settings. These additional server definitions
     * will be stored as JSON strings in settings.
     * 
     * @param c
     * @return ArrayList<ServerAttributeJson>
     */
    public static ArrayList<ServerAttributeJson> getCustomServers(Context c) {
        Preferences.loadSettings(c);
        if (gson == null)
            gson = new Gson();

        return gson.fromJson(mSettings.getString(CUSTOM_SERVERS, "[]"),
                new TypeToken<ArrayList<ServerAttributeJson>>() {
                }.getType());

    }

    /**
     * Writes custom servers back to disk
     * 
     * @param custom_servers
     * @param c
     */
    public static void setCustomServers(ArrayList<ServerAttributeJson> custom_servers, Context c) {
        Preferences.loadSettings(c);
        if (gson == null)
            gson = new Gson();

        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(CUSTOM_SERVERS, gson.toJson(custom_servers));
        editor.commit();
    }

    /**
     * Returns the current_server stored in app_state This may return null,
     * meaning there is no current_server chosen. Server definitions will change
     * over time, and we always want to use the latest defintion of each server.
     * Check for the server by name and fully reload the JSON each time.
     * 
     * @param context
     * @return {@link ServerAttributeJson}
     */
    public static ServerAttributeJson getCurrentServer(Context context) {
        Preferences.loadState(context);
        String serverUrl = mState.getString(CURRENT_SERVER, "");

        if (serverUrl != null) {
            ServerAttributeJson s = null;

            if (gson == null)
                gson = new Gson();

            ArrayList<ServerAttributeJson> available_servers = gson.fromJson(
                    Util.file_get_contents(context, R.raw.available_servers),
                    new TypeToken<ArrayList<ServerAttributeJson>>() {
                    }.getType());
            s = findServerByUrl(available_servers, serverUrl);
            if (s != null)
                return s;
            s = findServerByUrl(getCustomServers(context), serverUrl);
            if (s != null)
                return s;
        }
        return null;
    }

    /**
     * Loops through a JSONArray and returns the match, based on the name
     * 
     * @param servers
     * @param URL
     * @return {@link ServerAttributeJson}
     */
    private static ServerAttributeJson findServerByUrl(
            ArrayList<ServerAttributeJson> servers, String url) {
        int len = servers.size();
        ServerAttributeJson s;
        for (int i = 0; i < len; i++) {

            s = servers.get(i);
            if (s.url.contentEquals(url)) {
                return s;
            }

        }
        return null;
    }

    /**
     * Saves the name of the current server back into Preferences.app_state
     * Passing null for the server will unset the current_server We save only
     * the name, because we want to reload the full JSON from available_servers
     * each time. The endpoint definition may change over time, and we always
     * want to use the most up to date version.
     * 
     * @param server
     * @param c
     */
    public static void setCurrentServer(ServerAttributeJson server, Context c) {
        Preferences.loadState(c);

        SharedPreferences.Editor editor = mState.edit();
        if (server != null) {

            editor.putString(CURRENT_SERVER, server.url);

        } else {
            editor.remove(CURRENT_SERVER);
        }
        editor.commit();
    }
}
