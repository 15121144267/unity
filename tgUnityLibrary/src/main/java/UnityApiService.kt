import com.ypp.net.annotations.Host
import io.reactivex.Flowable
import retrofit2.http.*

/**
 * @author Created by helei
 * @data 18.3.22
 * Email:helei19910210@163.com
 * Description:
 */
@Host("https://api.hibixin.com")
interface UnityApiService {
    @POST
    fun postRequest(
        @Url url: String?,
        @Body body: Map<String?, Any?>?,
        @HeaderMap headers: Map<String?, String?>?
    ): Flowable<Any?>?

    @POST
    fun postRequest(@Url url: String?, @Body body: Map<String?, Any?>?): Flowable<Any?>?

    @GET
    fun getRequest(@Url url: String?, @HeaderMap headers: Map<String?, String?>?): Flowable<Any?>?

    @GET
    fun getRequest(@Url url: String?): Flowable<Any?>?
}