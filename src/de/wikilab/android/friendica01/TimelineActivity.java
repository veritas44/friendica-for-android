package de.wikilab.android.friendica01;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class TimelineActivity extends Activity {
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.tl_listview);
		
		TextView header_logo = (TextView) findViewById(R.id.header_logo);
		header_logo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		loadTimeline();
	}
	
	void loadTimeline() {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String server = prefs.getString("login_server", null);
		
		final TwAjax t = new TwAjax(this, true, true);
		t.getUrlContent("http://" + server + "/api/statuses/home_timeline.json", new Runnable() {
			@Override
			public void run() {
				try {
					JSONArray j = (JSONArray) t.getJsonResult();
					JSONObject[] jsonObjectArray = new JSONObject[j.length()];
					
					for(int i = 0; i < j.length(); i++) {
						jsonObjectArray[i] = j.getJSONObject(i);
					}
					
					ListView lvw = (ListView) findViewById(R.id.listview);
					
					lvw.setAdapter(new PostListAdapter(TimelineActivity.this, jsonObjectArray));
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
	}
	
	
	
	
}
