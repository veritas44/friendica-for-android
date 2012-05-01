package de.wikilab.android.friendica01;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendicaImgUploadActivity extends Activity {

	public final static int RQ_SELECT_CLIPBOARD = 1;
	
	String uploadCbName = "";
	int uploadCbId = 0;
	
	String fileExt;
	Uri fileToUpload;
	boolean deleteAfterUpload;
	
	String textToUpload;
	boolean uploadTextMode;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.uploadfile);
        

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String userName = prefs.getString("login_user", null);
		if (userName == null || userName.length() < 1) {
			showLoginForm(null);
		} else {
			tryLogin();
		}
        
        /*
        View btn_select_clipboard = (View) findViewById(R.id.btn_select_clipboard);
        btn_select_clipboard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startActivityForResult(new Intent(UploadFile.this, ClipboardSelector.class), RQ_SELECT_CLIPBOARD);
			}
		});
        */
        
		//TextView t = new TextView(UploadFile.this);
        //EditText txtFilename = (EditText) findViewById(R.id.txt_filename);
        
        EditText t = (EditText) findViewById(R.id.maintb);
		t.setText("File Uploader\n\nERR: Intent did not contain file!\n\nPress menu button for debug info !!!\n\n");

		View btn_upload = (View) findViewById(R.id.btn_upload);
		btn_upload.setEnabled(false);
		
		//this.setContentView(t);
		
		Intent callingIntent = getIntent();
		if (callingIntent != null) {
			if (callingIntent.hasExtra(Intent.EXTRA_STREAM)) {
				fileToUpload = (Uri) callingIntent.getParcelableExtra(Intent.EXTRA_STREAM);
				String fileSpec = Max.getRealPathFromURI(FriendicaImgUploadActivity.this, fileToUpload);
				
				
				((ImageView)findViewById(R.id.preview)).setImageURI(Uri.parse("file://"+fileSpec));
				//txtFilename.setText(Max.getBaseName(fileSpec));
				
				
				t.setText("Andfrnd Uploader Beta\n\n[b]URI:[/b] " + fileToUpload.toString() + "\n[b]File name:[/b] " + fileSpec);
				
				deleteAfterUpload = false;
				
				// restore data after failed upload:
				if (callingIntent.hasExtra(FileUploadService.EXTRA_DESCTEXT)) {
					t.setText(callingIntent.getStringExtra(FileUploadService.EXTRA_DESCTEXT));
				}
				
				uploadTextMode = false;
				btn_upload.setEnabled(true);
			}
		}
		
		
		btn_upload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				EditText txtDesc = (EditText) findViewById(R.id.maintb);
				
				Intent uploadIntent = new Intent(getApplicationContext(), FileUploadService.class);
				Bundle b = new Bundle();
				//b.putInt(FileUploadService.EXTRA_CLIPBOARDID, uploadCbId);
				//b.putString("clipboardName", uploadCbName);
				//b.putBoolean(FileUploadService.EXTRA_DELETE, deleteAfterUpload);
				//b.putString(FileUploadService.EXTRA_FILENAME, txtFilename.getText().toString());
				b.putString(FileUploadService.EXTRA_DESCTEXT, txtDesc.getText().toString());
				/*
				if (uploadTextMode == true) {
					try {
						String fileName = "textUploadTemp_" + System.currentTimeMillis() + ".txt";
						FileOutputStream fos = openFileOutput(fileName, Activity.MODE_WORLD_READABLE);
						BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
						bw.write(textToUpload);
						bw.close();
						b.putParcelable(Intent.EXTRA_STREAM, Uri.parse("file://" + getFilesDir().getAbsolutePath() + "/" + fileName));
					} catch (IOException e) { Log.e("UploadFile", "unable to write temp file !!! this should never happen !!!"); return; }
					
				} else {*/
					b.putParcelable(Intent.EXTRA_STREAM, fileToUpload);
				/*}*/
				uploadIntent.putExtras(b);

				Log.i("Andfrnd/UploadFile", "before startService");
				startService(uploadIntent);
				Log.i("Andfrnd/UploadFile", "after startService");
				
				finish();
				
				
			}
		});
		
		//
	}
	
	
	public void tryLogin() {
		final ProgressDialog pd = new ProgressDialog(this);
		pd.show();		

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String server = prefs.getString("login_server", null);
		
		final TwAjax t = new TwAjax(this, true, true);
		t.getUrlContent("http://"+server+"/api/account/verify_credentials", new Runnable() {
			@Override
			public void run() {
				pd.dismiss();
				try {
					if (t.isSuccess()) {
						if (t.getHttpCode() == 200) {
							JSONObject r = (JSONObject) t.getJsonResult();
							String name = r.getString("name");
							((TextView)findViewById(R.id.selected_clipboard)).setText(name);
							
							final TwAjax profileImgDl = new TwAjax();
							final String targetFs = getCacheDir()+"/"+r.getString("id")+".jpg";
							profileImgDl.urlDownloadToFile(r.getString("profile_image_url"), targetFs, new Runnable() {
								@Override
								public void run() {
									((ImageView)findViewById(R.id.profile_image)).setImageURI(Uri.parse("file://"+targetFs));
								}
							});
							
							
						} else {
							showLoginForm("Error:"+t.getResult());
						}
					} else {
						
						showLoginForm("ERR:"+t.getError().toString());
					}
					
				} catch(Exception ex) {
					showLoginForm("ERR2:"+t.getResult()+ex.toString());
					
				}
			}
		});
	}
	
	public void showLoginForm(String errmes) {
		View myView = getLayoutInflater().inflate(R.layout.loginscreen, null, false);
		final AlertDialog alert = new AlertDialog.Builder(this)
		.setTitle("Login to Friendica")
		.setView(myView)
		.show();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String server = prefs.getString("login_server", null);
		String userName = prefs.getString("login_user", null);
		
		if (errmes != null) {
			((TextView)myView.findViewById(R.id.lblInfo)).setText(errmes);
		}
		
		final EditText edtServer = (EditText)myView.findViewById(R.id.edtServer);
		edtServer.setText(server);
		
		final EditText edtUser = (EditText)myView.findViewById(R.id.edtUser);
		edtUser.setText(userName);

		final EditText edtPassword = (EditText)myView.findViewById(R.id.edtPassword);
		
		((Button)myView.findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(FriendicaImgUploadActivity.this).edit();
				prefs.putString("login_server", edtServer.getText().toString());
				prefs.putString("login_user", edtUser.getText().toString());
				prefs.putString("login_password", edtPassword.getText().toString());
				prefs.commit();
				
				alert.dismiss();
				
				tryLogin();
			}
		});
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.uploadfile_menu, menu);
        return true;
    }
    private String getTypeName(Object o) {
    	if (o == null) return "<null>";
    	Class type = o.getClass();
    	if (type == null) return "<unknown>"; else return type.getCanonicalName();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.view_debug:
        	Intent callingIntent = getIntent();
    		if (callingIntent != null) {
    			Bundle e = callingIntent.getExtras();
    			String[] val = new String[e.keySet().size()];
    			String[] val2 = new String[e.keySet().size()];
    			int i=0;
    			for(String key : e.keySet()) {
    				val[i] = key+": "+String.valueOf(e.get(key));
    				val2[i++] = getTypeName(e.get(key))+" "+key+":\n"+String.valueOf(e.get(key));
    			}
    			final String[] values = val2;
    			
    			new AlertDialog.Builder(FriendicaImgUploadActivity.this)
    			.setItems(val, new OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					new AlertDialog.Builder(FriendicaImgUploadActivity.this)
    					.setMessage(values[which])
    					.show();
    				}
    			})
    			.setTitle("Debug Info [File]")
    			.show();
    			
    		}
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }
    

}