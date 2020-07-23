package demo.app.paintball.data.rest

import demo.app.paintball.PaintballApplication
import demo.app.paintball.R
import demo.app.paintball.data.model.Game
import demo.app.paintball.data.model.Player
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface GameApi {

    @GET("/api/game")
    fun getGame(): Call<Game>

    @POST("/api/game")
    fun createGame(@Body game: Game): Call<Any>

    @DELETE("/api/game")
    fun deleteGame(): Call<Any>

    @POST("/api/game/red")
    fun addRedPlayer(@Body player: Player): Call<Any>

    @POST("/api/game/blue")
    fun addBluePlayer(@Body player: Player): Call<Any>

    companion object {
        val BASE_URL = PaintballApplication.context.getString(R.string.baseUrl)

        fun create(): GameApi {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(GameApi::class.java)
        }
    }
}