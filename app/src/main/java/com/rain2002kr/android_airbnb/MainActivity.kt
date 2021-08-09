package com.rain2002kr.android_airbnb

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.widget.LocationButtonView
import com.rain2002kr.android_airbnb.databinding.ActivityMainBinding
import com.rain2002kr.android_airbnb.model_house.*


import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), OnMapReadyCallback, Overlay.OnClickListener {
	private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }


	private lateinit var naverMap: NaverMap
	private lateinit var locationSource:FusedLocationSource

	private val houseViewPagerAdapter by lazy { HouseViewPagerAdapter(itemClickListener = {
		val intent = Intent()
			.apply {
				action = Intent.ACTION_SEND
				putExtra(Intent.EXTRA_TEXT, "지금 이가격에 예약하세요!!! ${it.title}, ${it.price}, 사진 보기${it.imgUrl} ")
				type = "text/plain"
			}
		startActivity(Intent.createChooser(intent,null))


	}) }

	private val bottomSheetRecyclerView :RecyclerView by lazy {findViewById(R.id.bottomSheetRecyclerView)}
	private val currentLocationButton :LocationButtonView by lazy {findViewById(R.id.currentLocationButton)}


	private val houseAdapter by lazy { HouseAdapter(itemClickListener = {
		//Log.d(TAG, it.title)


		})
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(binding.root)
		binding.mapView.onCreate(savedInstanceState)
		binding.mapView.getMapAsync(this)
		binding.houseViewPager.adapter = houseViewPagerAdapter

		binding.houseViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
			override fun onPageSelected(position: Int) {
				super.onPageSelected(position)

				val selectedHouseModel = houseViewPagerAdapter.currentList[position]
				// 카메라 업데이트 & animate 이용하여 부드럽게 이동하기
				val cameraUpdate = CameraUpdate.scrollTo(LatLng(selectedHouseModel.lat,selectedHouseModel.lng))
					.animate(CameraAnimation.Easing)

				// moveCamera 이용
				naverMap.moveCamera(cameraUpdate)
			}

			override fun onPageScrolled(
				position: Int,
				positionOffset: Float,
				positionOffsetPixels: Int
			) {
				super.onPageScrolled(position, positionOffset, positionOffsetPixels)
			}

			override fun onPageScrollStateChanged(state: Int) {
				super.onPageScrollStateChanged(state)
			}
		})


		bottomSheetRecyclerView.adapter = houseAdapter
		bottomSheetRecyclerView.layoutManager = LinearLayoutManager(this)

	}
	override fun onMapReady(map: NaverMap) {
		naverMap = map
		// 맥스와 민값을 줘서 줌 리미트를 줌.
		naverMap.maxZoom = 18.0
		naverMap.minZoom = 10.0

		// 카메라로 이동, 구글지도에서 값을 얻을수 있다.
		val cameraUpdate  = CameraUpdate.scrollTo(LatLng(37.51542644011184, 126.73738606913064))
		naverMap.moveCamera(cameraUpdate)

		// 현위치 세팅하는 버튼
		val uiSetting = naverMap.uiSettings
		// 아래 커스텀 위해서 enable 오프
		uiSetting.isLocationButtonEnabled = true
		// 버튼위치에 네이버 맵
		currentLocationButton.map = naverMap

		// 시작 위치지정
		locationSource = FusedLocationSource(this@MainActivity,LOCATION_PERMISSION_REQUEST_CODE )
		naverMap.locationSource = locationSource
		// 마커
		val marker = Marker()
		marker.position = LatLng(37.506670221989225, 126.74202399796573)
		marker.map = naverMap
		// 마커 색상
		marker.icon = MarkerIcons.BLACK
		marker.iconTintColor = Color.RED
		marker.width = Marker.SIZE_AUTO
		marker.height = Marker.SIZE_AUTO

		getHouseListFromAPI()

	}
	override fun onStart() {
		super.onStart()
		binding.mapView.onStart()
	}
	override fun onResume() {
		super.onResume()
		binding.mapView.onResume()
	}
	override fun onPause() {
		super.onPause()
		binding.mapView.onPause()
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		binding.mapView.onSaveInstanceState(outState)
	}
	override fun onStop() {
		super.onStop()
		binding.mapView.onStop()
	}
	override fun onDestroy() {
		super.onDestroy()
		binding.mapView.onDestroy()
	}
	override fun onLowMemory() {
		super.onLowMemory()
		binding.mapView.onLowMemory()
	}




	private fun getHouseListFromAPI(){
		val retrofit = Retrofit.Builder()
			.baseUrl("https://run.mocky.io")
			.addConverterFactory(GsonConverterFactory.create())
			.build()

		retrofit.create(HouseService::class.java).also{
			it.getHouseList()
				.enqueue(object : Callback<HouseDto>{
					override fun onResponse(call: Call<HouseDto>, response: Response<HouseDto>) {
						if(!response.isSuccessful)
							{
								faildLoadingDataFromMockAPI("onResponse : 데이터 읽어오는것을 실패하였습니다.")
								return
							}
							response.body()?.let{ dto ->
								Log.d(TAG, "데이터읽어오기 성공")
								houseViewPagerAdapter.submitList(dto.items)
								houseAdapter.submitList(dto.items)

								houseViewPagerAdapter.notifyDataSetChanged()

								//todo  마커찍기
								setMarker(dto.items)

							}
					}
					override fun onFailure(call: Call<HouseDto>, t: Throwable) {
						// 실패 처리에 대한 구현
						faildLoadingDataFromMockAPI("onFailure : 데이터 읽어오는것을 실패하였습니다.")
					}
				})
		}
	}



	private fun setMarker(houseModels:List<HouseModel> ) {

		houseModels.forEach { houseModel ->
			val marker = Marker()
				// todo onClickListener 와 tag에 id 값 연결
				marker.onClickListener = this
				marker.tag = houseModel.id

				marker.position = LatLng(houseModel.lat, houseModel.lng)

				marker.map = naverMap
				marker.icon = MarkerIcons.BLACK
				marker.iconTintColor = Color.RED
				marker.width = Marker.SIZE_AUTO
				marker.height = Marker.SIZE_AUTO
		}

	}

	private fun faildLoadingDataFromMockAPI(message:String){
		Log.d(TAG,"$message")
	}


	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if(requestCode != LOCATION_PERMISSION_REQUEST_CODE){
			return
		}
		if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)){
			if (!locationSource.isActivated){
				naverMap.locationTrackingMode = LocationTrackingMode.None
			}
			return
		}
	}



	companion object{
		private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
		private const val TAG = "MainActivity"
	}


	override fun onClick(overly: Overlay): Boolean {
		//overly.tag
		Log.d(TAG,"OVERRAY ${overly.tag}")
		val selectedHouseModel = houseViewPagerAdapter.currentList.firstOrNull{
				it.id == overly.tag
		}
		selectedHouseModel?.let{
			val position = houseViewPagerAdapter.currentList.indexOf(it)
			Log.d(TAG,"position ${position}")
			binding.houseViewPager.currentItem = position
		}

		return true
	}

}