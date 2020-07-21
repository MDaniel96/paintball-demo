package demo.app.paintball.data.game.rest

import demo.app.paintball.PaintballApplication
import demo.app.paintball.R
import demo.app.paintball.data.model.Game
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface GameService {

    @GET("/api/game")
    fun getGame(): Call<Game>

    @POST("/api/game")
    fun createGame(@Body game: Game): Call<Any>

    @DELETE("/api/game")
    fun deleteGame(): Call<Any>

    companion object {
        val BASE_URL = PaintballApplication.context.getString(R.string.baseUrl)

        fun create(): GameService {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(GameService::class.java)
        }
    }
}