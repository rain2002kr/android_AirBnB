package com.rain2002kr.android_airbnb.model_house

import retrofit2.Call
import retrofit2.http.GET

interface HouseService {
	@GET("/v3/6ddcaa8f-558c-4820-8270-581d7b0eba6e")
	fun getHouseList(): Call<HouseDto>
}


//	https://run.mocky.io/v3/6ddcaa8f-558c-4820-8270-581d7b0eba6e