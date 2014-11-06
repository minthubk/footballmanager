package june.footballmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddFindPlayerActivity extends Activity implements OnClickListener {
	static final int LOCATION = 1;
	
	EditText title;
	TextView location;
	TextView position;
	TextView ages;
	EditText content;
	Button ok;
	
	// 현재 로그인 정보
	LoginManager lm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_find_player);
		
		// 액션바 설정
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setSubtitle("글쓰기");
		
		// 뷰 레퍼런싱
		title = (EditText)findViewById(R.id.title);
		location = (TextView)findViewById(R.id.location);
		location.setOnClickListener(this);
		position = (TextView)findViewById(R.id.position);
		position.setOnClickListener(this);
		ages = (TextView)findViewById(R.id.ages);
		ages.setOnClickListener(this);
		content = (EditText)findViewById(R.id.content);
		ok = (Button)findViewById(R.id.btnOK);
		ok.setOnClickListener(this);
		
		// 로그인 정보 객체 생성
		lm = new LoginManager(this);
	}

	@Override
	public void onClick(View v) {
		AlertDialog.Builder builder;
		
		switch(v.getId()){
		case R.id.location:
			startActivityForResult(new Intent(this, SelectLocationActivity.class), LOCATION);
			break;
		case R.id.position:
			builder = new AlertDialog.Builder(this);
			builder.setTitle("포지션을 선택하세요");
			builder.setItems(R.array.positions_long, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					position.setText(AddFindPlayerActivity.this.getResources().getStringArray(R.array.positions_short)[which]);
				}
			});
			builder.create().show();
			break;
		case R.id.ages:
			builder = new AlertDialog.Builder(this);
			builder.setTitle("연령층을 선택하세요");
			builder.setItems(R.array.ages, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ages.setText(AddFindPlayerActivity.this.getResources().getStringArray(R.array.ages)[which]);
				}
			});
			builder.create().show();
			break;
		case R.id.btnOK:
			
			if(title.getText().toString().isEmpty())
				Toast.makeText(this, "글 제목을 입력해주세요", 0).show();
			else if(location.getText().toString().isEmpty())
				Toast.makeText(this, "활동 지역을 설정해주세요", 0).show();
			else if(position.getText().toString().isEmpty())
				Toast.makeText(this, "포지션을 설정해주세요", 0).show();
			else if(ages.getText().toString().isEmpty())
				Toast.makeText(this, "연령대를 설정해주세요", 0).show();
			else if(content.getText().toString().isEmpty())
				Toast.makeText(this, "내용을 입력해주세요", 0).show();
			else
				// 게시물 등록
				new AddFindPlayer().execute();
			
			break;
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_find_player, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// 주소 받아오기
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case LOCATION:
			if (resultCode == RESULT_OK) {
				location.setText(intent.getStringExtra("location"));
			}
		}
	}

	public class AddFindPlayer extends AsyncTask<Void, Void, Void> {
		ProgressDialog pd;
		String jsonString = "";
		
		@Override
		public void onPreExecute() {
			pd = new ProgressDialog(AddFindPlayerActivity.this);
			pd.setMessage("잠시만 기다려 주세요...");
			pd.show();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			
			String param = "memberNo=" + lm.getMemberNo()
					+ "&title=" + title.getText().toString()
					+ "&location=" + location.getText().toString()
					+ "&position=" + position.getText().toString()
					+ "&ages=" + ages.getText().toString()
					+ "&content=" + content.getText().toString().replace("\n", "__");
			
			try {
				URL url = new URL(getString(R.string.server)
						+ getString(R.string.add_find_player));
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);

				OutputStreamWriter out = new OutputStreamWriter(
						conn.getOutputStream(), "euc-kr");
				out.write(param);
				out.flush();
				out.close();

				String buffer = null;
				BufferedReader in = new BufferedReader(new InputStreamReader(
						conn.getInputStream(), "euc-kr"));
				while ((buffer = in.readLine()) != null) {
					jsonString += buffer;
				}
				in.close();
				
				Log.i("FM", "AddFindPlayer result : " + jsonString);


			} catch (MalformedURLException e) {
				Log.e("FM", "AddFindPlayer : " + e.getMessage());
			} catch (IOException e) {
				Log.e("FM", "AddFindPlayer : " + e.getMessage());
			} 
			
			return null;
		}
		
		@Override
		public void onPostExecute(Void params) {
			pd.dismiss();
			
			JSONObject jsonObj;
			try {
				jsonObj = new JSONObject(jsonString);
				
				if(jsonObj.getInt("success") == 1) {
					Toast.makeText(getApplicationContext(), "게시물이 등록되었습니다.", 0).show();
					finish();
				}
			} catch (JSONException e) {
				Log.e("FM", "AddFindPlayer : " + e.getMessage());
			}
		}
	}
}
