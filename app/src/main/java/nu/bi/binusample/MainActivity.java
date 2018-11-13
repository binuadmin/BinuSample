package nu.bi.binusample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import nu.bi.binuproxy.BinuProxy;
import nu.bi.binuproxy.Deployment;
import nu.bi.binuproxy.ProxySettings;
import nu.bi.binuproxy.http.Http;
import nu.bi.binuproxy.http.HttpGet;
import nu.bi.binuproxy.session.SessionManager;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    static BinuProxy mBinuProxy;

    // This is the id assigned to you and shown in Publisher Portal when you register.
    // The value here is just a sample, please change to your app id.
    private static final String mBinuAppId = "221";

    // below is sample only, change this to real value.
    final static String mFirstPageUrl = "http://www.google.com/";

    // use Deployment.SANDBOX for testing, or Deployment.PRODUCTION for live production app.
    final static Deployment mDeployment = Deployment.SANDBOX;

    // default site for PlayerActivity
    private static final String mPlayerUrl = "http://us4.internet-radio.com:8266";

    TextView mMainText;
    ImageView mMainImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainText = findViewById(R.id.textView2);
        mMainImage = findViewById(R.id.image1);

        final Context c = this;

        // other important stuff specific to your app here.
        // ...

        Map<String, String> extras = new HashMap<>();
        extras.put(ProxySettings.BINU_KEY_APP_ID, mBinuAppId);
        extras.put(ProxySettings.BINU_KEY_TOKEN, "84f3123a-3453-a7340-836e7843");
        extras.put(ProxySettings.BINU_KEY_VERSION_TAG, "1.0.0");
        extras.put(ProxySettings.BINU_KEY_BUILD_ID, "1234");

        ProxySettings settings = new ProxySettings(mDeployment, extras);

        // increase the value if having timeout frequently, default is 3 seconds if not set.
        settings.setConnectTimeout(60);
        settings.setReadTimeout(60);
        settings.setUseHttpInterceptor(true);

        // get BinuProxy instance with BinuAppId and settings.
        mBinuProxy = BinuProxy.getInstance(this, settings
                , new BinuProxy.OnInitFinishedListener() {
                    @Override
                    public void onSuccess() {
                        // app code if Binu Proxy initialises successfully.
                        // typically this will be your first network request to get data from
                        // your server.

                        // Get the first page.
                        getFirstPage(mFirstPageUrl);
                    }

                    @Override
                    public void onFailure() {
                        // app code if Binu Proxy fails to initialise.
                        // e.g. finish();
                        Toast.makeText(c, "BinuProxy failed to initialise", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

        // optionally register listener for network status or datafree status change.
        // This will let your app know when these statuses change.
        BinuProxy.setOnNetworkChangeListener(new BinuProxy.OnNetworkChangeListener() {
            @Override
            public void onFreeStatusChanged(BinuProxy.FreeStatus status) {
                // do something on #datafree status change
                // the status could be either FREE, PAID or UNKNOWN, see BinuProxy.FreeStatus for detail.
            }
            @Override
            public void onConnectivityChange(BinuProxy.NetStatus status) {
                // do something on network connectivity change
                // the status could be either WIFI, OFFLINE, ONLINE or UNKNOWN, see BinuProxy.NetStatus for detail.
            }
        });
    }

    private void getFirstPage(final String url) {
        final Context c = this;
        new HttpGet(url, "text/html", null) {
            @Override
            public void onSuccess(final Response response, final String body) {
                super.onSuccess(response, body);
                // do something with the response. The body is equivalent to response.body().
                Toast.makeText(c, "Yay! got the first page", Toast.LENGTH_SHORT).show();

                // next line if you want page navigation to be reported in Binu reporting.
                // call this method on all page navigation
                SessionManager.onNavigate(c, url, "home page");
            }

            @Override
            public void onNetworkingProblem(RequestBody body, Throwable t) {
                super.onNetworkingProblem(body, t);
                // handle the network error
                Toast.makeText(c, "network problem, failed to reach server :(", Toast.LENGTH_SHORT).show();
                mMainText.setText("Network Error");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Response<ResponseBody> response) {
                super.onFailure(call, response);
                // response code not 200..299, handle request failure here
                Toast.makeText(c, "server returned error ("+response.code()+")", Toast.LENGTH_SHORT).show();
                mMainText.setText("Request Failed "+response.code());
            }
        };

        Http.mPicasso
                .load("http://d12t435tmqq7vw.cloudfront.net/wp-content/uploads/2017/04/biNu-logo-colour-112x56px.png")
                .noPlaceholder()
                .into(mMainImage);
    }

    public void startExoplayer(View v) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("site", mPlayerUrl);
        startActivity(intent);
    }
}
