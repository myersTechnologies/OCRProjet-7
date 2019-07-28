package dasilva.marco.go4lunch.ui.map.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadUrl {

    public String readUrl(String url) throws IOException{

        String data = "";
        InputStream inputStream = null;
        HttpURLConnection connection = null;

        try {
            URL myUrl = new URL(url);
            connection = (HttpURLConnection) myUrl.openConnection();
            connection.connect();

            inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer = new StringBuffer();

            String line = "";

            while ((line = reader.readLine()) != null){
                stringBuffer.append(line);
            }

            data = stringBuffer.toString();

            reader.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        return data;
    }

}
