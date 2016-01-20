package mhealth.singingleaf.proj.helloworld;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;


public class ServerCommunicator {

    private String connString;
    private boolean succeeded;


    public ServerCommunicator(String connString) {
        this.connString = connString;
    }

    public ServerCommunicator() {
        this.connString =
                "http://ec2-52-27-8-0.us-west-2.compute.amazonaws.com/HelloWorld/Receiver.php";
    }

    public boolean postToServer(RequestParams rp) {

        // Create a client to perform networking
        AsyncHttpClient client= new AsyncHttpClient();

        // Have the client get RequestParams
        // and define how to respond
        client.post(connString, rp,
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        succeeded = true;
                    }

                    @Override
                    public void onFailure(int statusCode, Throwable throwable, JSONObject error) {
                        succeeded = false;
                    }
                });
        return succeeded;
    }


}
