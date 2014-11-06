package june.footballmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ScrappedFindTeamListFragment extends Fragment implements
		OnItemClickListener {
	ListView list;
	TextView count;
	TextView empty;
	TextView txtSort; // 정렬기준 text
	ArrayList<FindTeamItem> findTeamList;
	FindTeamListAdapter tlAdapter;

	// 스크랩한 글 번호 스트링
	String scrappedItems;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_list, container, false);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setHasOptionsMenu(true);

		count = (TextView) getView().findViewById(R.id.count);

		// 리스트 객체 생성
		findTeamList = new ArrayList<FindTeamItem>();

		// 어댑터 객체 생성
		tlAdapter = new FindTeamListAdapter(getActivity(), findTeamList);

		list = (ListView) getView().findViewById(R.id.list);
		list.setEmptyView(getView().findViewById(R.id.empty));
		list.addHeaderView(new View(getActivity()), null, true);
		list.addFooterView(new View(getActivity()), null, true);
		list.setAdapter(tlAdapter);
		list.setOnItemClickListener(this);

		// 엠티뷰 텍스트 설정
		empty = (TextView) getView().findViewById(R.id.empty);
		empty.setText("스크랩한 게시물이 존재하지 않습니다.");
		
		// DB로부터 스크랩 목록 가져오기
	    DatabaseHandler db = new DatabaseHandler(getActivity());
		scrappedItems = db.getAllScrapFindTeam();
		Log.i("Scrapped Find Team List", scrappedItems);
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// DB -> 리스트뷰 출력
		new GetFindTeamList().execute();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long arg3) {
		
		Intent intent = new Intent(getActivity(), FindTeamDetailActivity.class);

		// 글 번호를 넘겨줌
		// 헤더뷰가 추가되었기 때문에 인덱스를 1 감소시킨다.
		intent.putExtra("no", findTeamList.get(position - 1).getNo());
		startActivity(intent);
	}

	// 어댑터 정의
	public class FindTeamListAdapter extends BaseAdapter {

		private Context context;
		private ArrayList<FindTeamItem> list;
		private LayoutInflater inflater;

		public FindTeamListAdapter(Context c, ArrayList<FindTeamItem> list) {
			this.context = c;
			this.list = list;
			this.inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public FindTeamItem getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.find_team_item, parent,
						false);
			}
			
			// 선수 닉네임 출력
			TextView nickname = (TextView)convertView.findViewById(R.id.nickname);
			nickname.setText(getItem(position).getNickName());
			
			// 제목 출력
			TextView title = (TextView)convertView.findViewById(R.id.title);
			title.setText(getItem(position).getTitle());
			
			// 포지션 출력
			TextView tvPosition = (TextView)convertView.findViewById(R.id.position);
			tvPosition.setText(getItem(position).getPosition());
			
			String strPos = getItem(position).getPosition();
			
			// 포지션별 색상 처리
			if(strPos.equals("GK"))
				tvPosition.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
			else if(strPos.equals("LB") || strPos.equals("CB") || strPos.equals("RB") 
					|| strPos.equals("LWB") || strPos.equals("RWB"))
				tvPosition.setTextColor(getResources().getColor(android.R.color.holo_green_light));
			else if(strPos.equals("LM") || strPos.equals("CM") || strPos.equals("RM") 
					|| strPos.equals("AM") || strPos.equals("DM"))
				tvPosition.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
			else
				tvPosition.setTextColor(getResources().getColor(android.R.color.holo_red_light));
			
			// 나이 출력
			TextView age = (TextView)convertView.findViewById(R.id.age);
			age.setText(getItem(position).getAge() + "세");
			
			// 지역 출력
			TextView location = (TextView) convertView.findViewById(R.id.location);
			location.setText(getItem(position).getLocation());
			
			// 즐겨찾기 버튼
			ImageView scrap = (ImageView) convertView
					.findViewById(R.id.img_scrap);
			DatabaseHandler db = new DatabaseHandler(
					ScrappedFindTeamListFragment.this.getActivity());
			boolean isScrapped = db.selectScrapFindTeam(getItem(position)
					.getNo());

			// 즐겨찾기 여부에 따라 다른 이미지를 출력한다.
			if (isScrapped)
				scrap.setImageResource(R.drawable.scrapped);
			else
				scrap.setImageResource(R.drawable.scrap);

			// 클릭 이벤트 리스너 등록
			scrap.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ImageView imgView = (ImageView) v;
					DatabaseHandler db = new DatabaseHandler(
							ScrappedFindTeamListFragment.this.getActivity());
					boolean isScrapped = db.selectScrapFindTeam(getItem(
							position).getNo());

					if (isScrapped) {
						db.deleteScrapFindTeam(getItem(position).getNo());
						imgView.setImageResource(R.drawable.scrap);
					} else {
						db.insertScrapFindTeam(getItem(position).getNo());
						imgView.setImageResource(R.drawable.scrapped);
					}
				}

			});

			return convertView;
		}
	}

	// 팀 리스트를 DB에서 가져온다.
	private class GetFindTeamList extends AsyncTask<Void, Void, Boolean> {
		String param = "";

		// URL로부터 가져온 json 형식의 string
		String jsonString = "";

		ProgressDialog pd;

		public void onPreExecute() {

			// 프로그레스 다이얼로그 출력
			pd = new ProgressDialog(getActivity());
			pd.setMessage("리스트를 불러오는 중입니다...");
			pd.show();

			param += "nos=" + scrappedItems;
			Log.i("param", param);
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {

			try {
				URL url = new URL(getString(R.string.server)
						+ getString(R.string.scrapped_find_team_list));
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);

				// URL에 파리미터 넘기기
				OutputStreamWriter out = new OutputStreamWriter(
						conn.getOutputStream(), "euc-kr");
				out.write(param);
				out.flush();
				out.close();

				// URL 결과 가져오기
				String buffer = null;
				BufferedReader in = new BufferedReader(new InputStreamReader(
						conn.getInputStream(), "euc-kr"));
				while ((buffer = in.readLine()) != null) {
					jsonString += buffer;
				}
				in.close();

				Log.i("FM", "GetFindTeamList result : " + jsonString);

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}

		public void onPostExecute(Boolean isSuccess) {
			try {
				JSONObject jsonObj = new JSONObject(jsonString);
				JSONArray jsonArr = jsonObj.getJSONArray("list");

				JSONObject item;

				findTeamList.clear();
				for (int i = 0; i < jsonArr.length(); i++) {
					item = jsonArr.getJSONObject(i);
					findTeamList.add(new FindTeamItem(item));
				}
			} catch (JSONException e) {
				findTeamList.clear();
				e.printStackTrace();
			} finally {

				tlAdapter.notifyDataSetChanged();
				count.setText("총 " + findTeamList.size() + "개");

				// 프로그레스 다이얼로그 종료
				pd.dismiss();
			}
		}
	}
}
