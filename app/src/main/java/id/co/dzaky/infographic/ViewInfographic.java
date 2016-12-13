package id.co.dzaky.infographic;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ViewInfographic extends AppCompatActivity {
    private PhotoViewAttacher mAttacher;
    private ImageView infoimage;
    private Button like,download,share;
    private String countlikes;
    private Retrofit retrofit;
    private RestClient service;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_infographic);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);
        infoimage = (ImageView) findViewById(R.id.infoimage);
        like = (Button) findViewById(R.id.like2);
        download = (Button) findViewById(R.id.download2);
        share = (Button) findViewById(R.id.share2);

        final String filename=getIntent().getExtras().getString("filename");
        final String name=getIntent().getExtras().getString("name");
        final String url="http://dzakyproject.esy.es/"+filename;
        final String id=getIntent().getExtras().getString("id");
        countlikes = getIntent().getExtras().getString("countlikes");
        TextView title = (TextView) findViewById(R.id.toolbartitle2);
        title.setText(name);
        Picasso.with(this).load(url).into(infoimage);
        mAttacher = new PhotoViewAttacher(infoimage);
        like.setText("{faw-heart} "+countlikes);
        download.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                save(url,filename,"Infographic");
            }
        });
        like.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                likes(id);
            }
        });
        share.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                shareIt(name);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
            super.onBackPressed();
    }

    private void shareIt(String name) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Infographic!");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, name+" "+R.string.sharemsg);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    private void likes(String id) {
        retrofit = new Retrofit.Builder()
                .baseUrl("http://dzakyproject.esy.es/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(RestClient.class);
        String deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        Observable<ResponseLikes> call = service.likes(id,deviceId);
        call.retry(3).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<ResponseLikes>() {
            @Override
            public void onNext(ResponseLikes responselikes) {
                try {
                    if(responselikes.getResponse().equals("OK")){
                        countlikes = String.valueOf(Integer.parseInt(countlikes)+1);
                        like.setText("{faw-heart} "+ countlikes);
                        Toast.makeText(ViewInfographic.this,"Like it",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(ViewInfographic.this,"You already like it",Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.d("errornih","errornih");
                    e.printStackTrace();
                }
            }
            @Override
            public void onCompleted() {
                // Nothing to do here
            }
            @Override
            public void onError(Throwable e) {
                Log.d("errornih","onFailure " +e.toString());
            }
        });

    }

    private void save(String url,String filename,String directory){
        File direct = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .getAbsolutePath() + "/" + directory + "/");
        if (!direct.exists()) {
            direct.mkdir();
        }
        DownloadManager dm = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri downloadUri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(filename)
                .setMimeType("image/jpeg")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,
                        File.separator + directory + File.separator + filename);
        dm.enqueue(request);
        Toast.makeText(this,"Saved",Toast.LENGTH_SHORT).show();
    }
}
