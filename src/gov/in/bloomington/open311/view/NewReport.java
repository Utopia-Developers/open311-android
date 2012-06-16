/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */
package gov.in.bloomington.open311.view;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.in.bloomington.open311.R;
import gov.in.bloomington.open311.controller.GeoreporterAPI;
import gov.in.bloomington.open311.controller.ServicesItem;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NewReport extends Activity implements OnClickListener  {
		private RelativeLayout r1;
		private ImageView img_photo;
		private EditText edt_newReport;
		private EditText edt_firstName;
		private EditText edt_lastName;
		private EditText edt_email;
		private EditText edt_phone;
		private Button btn_send;
		private TextView failed;

		private String content;
		private static final int CAMERA_REQUEST = 1888; 
		
		/** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.report);
	        
	        //for click listener
			r1 = (RelativeLayout) findViewById(R.id.r1);
		    r1.setOnClickListener((OnClickListener) this);
			
		    img_photo = (ImageView) findViewById(R.id.img_photo);
		    img_photo.setOnClickListener((OnClickListener) this);
		    
		    edt_newReport = (EditText) findViewById(R.id.edt_newReport);
		    edt_newReport.setOnFocusChangeListener(new OnFocusChangeListener() {
				public void onFocusChange(View v, boolean hasFocus) {
					if (edt_newReport.getText().toString().equals("Fill your report here")) {
			        	edt_newReport.setText(""); 
					}
					 edt_newReport.setTextColor(Color.BLACK);
				}
			});
		    
		    btn_send = (Button) findViewById(R.id.btn_submit);
		    btn_send.setOnClickListener((OnClickListener)this);
		    
			failed = (TextView) findViewById(R.id.txt_SendingFailed);
			failed.setVisibility(TextView.GONE);
	    }
	    
	    public void onClick(View v) {
			// TODO Auto-generated method stub
		    content = edt_newReport.getText().toString();

			switch (v.getId()) {
			case R.id.r1:
				
				/*intent = new Intent(ReportActivity.this, LocationMap.class);
				intent.putExtra("address", address);
				intent.putExtra("longitude", longitude);
				intent.putExtra("latitude", latitude);
				intent.putExtra("content", content);
	            startActivity(intent);*/
	            break;
			
			case R.id.img_photo:
		        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
	            break;
	        
			case R.id.btn_submit:			
				    	    
				//get the current server
				SharedPreferences pref = getSharedPreferences("server",0);
				JSONObject server;
				String server_name = null;
				try {
					server = new JSONObject(pref.getString("selectedServer", ""));
					server_name = server.getString("name");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
	    	    if (!content.equals("") && !content.equals("Fill your report here")) {
	    	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Available Service Groups from "+server_name);
					final JSONArray jar_services = GeoreporterAPI.streamServices(NewReport.this);
					final CharSequence[] group = ServicesItem.getGroup(jar_services);
		    		builder.setItems(group, new DialogInterface.OnClickListener() {
		    		    public void onClick(DialogInterface dialog, int nid) {
		    		    	AlertDialog.Builder builder = new AlertDialog.Builder(NewReport.this);
		    				builder.setTitle(group[nid]+" Services");
		    				CharSequence[] services = ServicesItem.getServicesByGroup(jar_services, group[nid]);
		    	    		builder.setItems(services, new DialogInterface.OnClickListener() {
		    	    		    public void onClick(DialogInterface dialog, int nid) {
		    	        	    	Toast.makeText(NewReport.this, "Your report has been sent", Toast.LENGTH_LONG).show();
		    	    		    }
		    	    		});
		    	    		AlertDialog alert = builder.create();
		    	    		alert.show();
		    		    }
		    		});
		    		AlertDialog alert = builder.create();
		    		alert.show();
	    	    }
	    	   
	    	    else 
	    	    	Toast.makeText(this, "Please fill your report first", Toast.LENGTH_SHORT).show();
	    	    
				break;
			}
		}
	    
	    
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
	        if (requestCode == CAMERA_REQUEST) {  
	        	if (resultCode == Activity.RESULT_OK) {
		            Bitmap photo = (Bitmap) data.getExtras().get("data"); 
		            img_photo.setImageBitmap(photo);
	        	}
	        }  
	    }  

	    @Override
		protected void onResume (){
			super.onResume();
			
			//for Shared Preferences
	        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
	        edt_firstName = (EditText) findViewById(R.id.edt_firstname);
	        edt_firstName.setText(preferences.getString("firstname", ""));
	        
	        edt_lastName = (EditText) findViewById(R.id.edt_lastname);
	        edt_lastName.setText(preferences.getString("lastname", ""));
	        
	        edt_email = (EditText) findViewById(R.id.edt_email);
	        edt_email.setText(preferences.getString("email", ""));
	        
	        edt_phone = (EditText) findViewById(R.id.edt_phone);
	        edt_phone.setText(preferences.getString("phone", ""));
		}	    
	 
}
