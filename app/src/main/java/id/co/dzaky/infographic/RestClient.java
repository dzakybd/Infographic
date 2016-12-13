package id.co.dzaky.infographic;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Zaki on 11/12/2016.
 */

public interface RestClient {

    @GET("getinfographic.php")
    Observable<List<DataInfographic>> request(@Query("order") String order, @Query("category") String category, @Query("search") String search);

    @GET("updatelike.php")
    Observable<ResponseLikes> likes(@Query("likes") String likes,@Query("device") String device);

}
