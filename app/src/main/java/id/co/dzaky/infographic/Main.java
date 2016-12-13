package id.co.dzaky.infographic;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class Main extends AppCompatActivity {
    public AccountHeader header;
    public Drawer drawer;
    private Retrofit retrofit;
    private RestClient service;
    private RecyclerView recycler;
    private RecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-8888526906409449~7205510410");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("8FD6A25CDFC92399E9CBA752CF963D08")
                .build();
        mAdView.loadAd(adRequest);

        header = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .build();

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(header)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(1).withName(R.string.drawer1).withIcon(FontAwesome.Icon.faw_arrow_circle_up),
                        new PrimaryDrawerItem().withIdentifier(2).withName(R.string.drawer2).withIcon(FontAwesome.Icon.faw_heart),
                        new ExpandableDrawerItem().withIdentifier(3).withName(R.string.drawer3).withIcon(FontAwesome.Icon.faw_list).withSubItems(
                                new SecondaryDrawerItem().withIdentifier(4).withName(R.string.subdrawer4).withIcon(FontAwesome.Icon.faw_briefcase),
                                new SecondaryDrawerItem().withIdentifier(5).withName(R.string.subdrawer5).withIcon(FontAwesome.Icon.faw_glass),
                                new SecondaryDrawerItem().withIdentifier(6).withName(R.string.subdrawer6).withIcon(FontAwesome.Icon.faw_book)
                        ),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withIdentifier(7).withName(R.string.lastdrawer).withIcon(FontAwesome.Icon.faw_copyright)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch ((int) drawerItem.getIdentifier()){
                            case 1:
                                request("1",null,null);
                                break;
                            case 2:
                                request("2",null,null);
                                break;
                            case 4:
                                request(null,"1",null);
                                break;
                            case 5:
                                request(null,"2",null);
                                break;
                            case 6:
                                request(null,"3",null);
                                break;
                        }
                        return false;
                    }
                })
                .build();
        drawer.setSelection(1, true);

        recycler = (RecyclerView) findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recycler.setHasFixedSize(true);


    }

    private void request(final String a, final String b, final String c) {
        retrofit = new Retrofit.Builder()
                .baseUrl("http://dzakyproject.esy.es/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        service = retrofit.create(RestClient.class);
        Observable<List<DataInfographic>> call = service.request(a,b,c);
        call.retry(3).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<List<DataInfographic>>() {
            @Override
            public void onNext(List<DataInfographic> data) {
                try {
                    adapter = new RecyclerAdapter(getApplicationContext(), data);
                    recycler.setAdapter(adapter);
                    recycler.invalidate();
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
                // cast to retrofit.HttpException to get the response code
                Log.d("errornih","onFailure " +e.toString());
            }
        });
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder pilihan = new AlertDialog.Builder(this);
        pilihan.setMessage("Sure to close?");
        pilihan.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        pilihan.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog alert = pilihan.create();
        alert.show();

    }
}
