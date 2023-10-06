
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Part

interface APIInterface {
    @Multipart
    @POST("api/Gallery/AddGallery")
    fun uploadImage(@Part image: MultipartBody.Part?): Call<JsonObject?>?
}