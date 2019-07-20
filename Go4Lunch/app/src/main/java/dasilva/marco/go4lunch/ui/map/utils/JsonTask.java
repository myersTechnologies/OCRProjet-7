package dasilva.marco.go4lunch.ui.map.utils;


import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.service.Go4LunchService;

public class JsonTask extends AsyncTask<String, String, String> {

    private String jSonUrl;
    private Go4LunchService service;
    String data;

    public JsonTask (String url){
        this.jSonUrl = url;
        service = DI.getService();
    }


    protected String doInBackground(String... params) {


        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(jSonUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line+"\n");
                Log.d("Response: ", "> " + line);
            }

            JSONObject jObj = new JSONObject(buffer.toString());
            JSONObject jResult = jObj.getJSONObject("result");

            service.getPlaceMarker().setTelephone(jResult.getString("formatted_phone_number"));
            service.getPlaceMarker().setName(jResult.getString("name"));
            service.getPlaceMarker().setWebSite(jResult.getString("website"));



            data = buffer.toString();
            return buffer.toString();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d("opened", String.valueOf(service.getPlaceMarker().getPhotoUrl()));
    }
}

