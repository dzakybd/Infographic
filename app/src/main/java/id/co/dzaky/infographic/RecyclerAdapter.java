package id.co.dzaky.infographic;

import android.app.DownloadManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by Zaki on 10/12/2016.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ReyclerViewHolder> {

    private LayoutInflater layoutInflater;
    private Context context;
    private List<DataInfographic> items;
    private Retrofit retrofit;
    private RestClient service;
    private String countlikes;

    public RecyclerAdapter(Context context, List<DataInfographic> items) {
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.items = items;

    }

    @Override
    public ReyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View acara = layoutInflater.inflate(R.layout.item_recycler, parent, false);

        return new ReyclerViewHolder(acara);
    }

    @Override
    public void onBindViewHolder(final ReyclerViewHolder holder, int position) {
            final String filename=items.get(position).getImage();
            final String name=items.get(position).getName();
            final String url="http://dzakyproject.esy.es/"+filename;
            final String id=items.get(position).getIdinfographic();
            countlikes = items.get(position).getLikes();
            Picasso.with(context).load(url).into(holder.image);
            holder.name.setText(name);
            holder.like.setText("{faw-heart} "+countlikes);
            holder.download.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                save(url,filename,"Infographic");
                }
            });
        holder.like.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                likes(holder,id);
            }
        });
        holder.share.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                shareIt(name);
            }
        });
        holder.image.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ViewInfographic.class);

                i.putExtra("id",id);
                i.putExtra("name",name);
                i.putExtra("filename",filename);
                i.putExtra("countlikes",countlikes);

                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        });
    }

    private void shareIt(String name) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Infographic!");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, name+" "+R.string.sharemsg);
        context.startActivity(Intent.createChooser(sharingIntent, "Share via").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void likes(final ReyclerViewHolder holder,String id) {
        retrofit = new Retrofit.Builder()
                .baseUrl("http://dzakyproject.esy.es/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(RestClient.class);
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        Observable<ResponseLikes> call = service.likes(id,deviceId);
        call.retry(3).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<ResponseLikes>() {
            @Override
            public void onNext(ResponseLikes responselikes) {
                try {
                    if(responselikes.getResponse().equals("OK")){
                        countlikes = String.valueOf(Integer.parseInt(countlikes)+1);
                        holder.like.setText("{faw-heart} "+ countlikes);
                        Toast.makeText(context,"Like it",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(context,"You already like it",Toast.LENGTH_SHORT).show();
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

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ReyclerViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView name;
        private Button like,download,share;

        private ReyclerViewHolder(final View v) {
            super(v);

            image = (ImageView) v.findViewById(R.id.image);
            name = (TextView) v.findViewById(R.id.name);
            like = (Button) v.findViewById(R.id.like);
            download = (Button) v.findViewById(R.id.download);
            share = (Button) v.findViewById(R.id.share);
        }
    }

    private void save(String url,String filename,String directory){
        File direct = new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .getAbsolutePath() + "/" + directory + "/");
        if (!direct.exists()) {
            direct.mkdir();
        }
        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
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
        Toast.makeText(context,"Saved",Toast.LENGTH_SHORT).show();
    }
}
