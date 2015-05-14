package com.example.sharvani.mapsapp;



import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class StoreLocationsFragment extends Fragment {


    ListView listView;
    String queryString;
    String[] addresses=new String[5];

    String[] justAdd=new String[5];
    Communicator communicator;
    public StoreLocationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_store_locations,
                container, false);
        listView = (ListView) view.findViewById(R.id.listView1);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);

        queryString = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20local.search(5)%20where%20query%3D%22"
                + MapsActivity.name
                + "%22%20and%20location%3D%22"
                + MapsActivity.zipCode
                + "%2C%20ca%22&format=json&callback=";

        new MyAsyncTask().execute();

    }

    public void displayListView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, addresses);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                communicator.respondName(justAdd[position]);
            }


        });
    }

    private class MyAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... arg0) {

            AndroidHttpClient httpClient = AndroidHttpClient.newInstance("");
            HttpGet request = new HttpGet(queryString);

            InputStream inputStream = null;
            String result = null;

            try {
                HttpResponse response = httpClient.execute(request);
                HttpEntity entity = response.getEntity();
                inputStream = entity.getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder theStringBuilder = new StringBuilder();
                String line = null;

                while ((line = reader.readLine()) != null) {
                    theStringBuilder.append(line + "\n");
                }
                result = theStringBuilder.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            finally {
                try {

                    if (inputStream != null)
                        inputStream.close();
                } catch (Exception e) {

                }
            }

            JSONObject jsonObject;
            try {
                Log.v("JSONParser RESULT", result);
                jsonObject = new JSONObject(result);

                JSONObject queryJSONObject = jsonObject.getJSONObject("query");
                JSONObject resultJSONObject = queryJSONObject
                        .getJSONObject("results");

                JSONArray jsonArray = resultJSONObject.getJSONArray("Result");
                int num = jsonArray.length();

                Log.v("ARRAY", "len" + num);
                for (int i = 0; i < jsonArray.length(); i++) {

                    String slNum = (i + 1)+". ";
                    StringBuffer addressOfLoc = new StringBuffer();
                    addressOfLoc.append(jsonArray.getJSONObject(i).getString(
                            "Address"));
                    Log.v("Address", addressOfLoc.toString());

                    addressOfLoc .append(" ");
                    addressOfLoc = addressOfLoc.append(jsonArray.getJSONObject(i).getString("City"))
                            .append(" ").append(jsonArray.getJSONObject(i).getString("State"));
                    justAdd[i]=addressOfLoc.toString();

                    addressOfLoc.append("    Dist: ").append(jsonArray.getJSONObject(i).getString("Distance")).append("miles");
                    addressOfLoc.insert(0, slNum);


                    addresses[i] = addressOfLoc.toString();

                    Log.v("Address", addressOfLoc.toString());

                }
                Log.v("AllOK", "fine");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            displayListView();
            communicator.respond(justAdd);

        }

    }
    public void setCommunicator(Communicator communicator){
        this.communicator=communicator;
    }

    public interface Communicator{
        public void respond(String[] strArr);
        public void respondName(String str);

    }


}
