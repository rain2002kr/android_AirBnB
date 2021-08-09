package com.rain2002kr.android_airbnb.model_house

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.RoundedCorner
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.rain2002kr.android_airbnb.databinding.ItemHouseBinding

class HouseAdapter(private val itemClickListener: (HouseModel) -> Unit ):ListAdapter<HouseModel,HouseAdapter.ViewHolder>(diffUtil) {
	inner class ViewHolder(val binding: ItemHouseBinding):RecyclerView.ViewHolder(binding.root){
		fun bind(houseModel: HouseModel){
			binding.titleTextView.text = houseModel.title
			binding.priceTextView.text = houseModel.price
			Glide
				.with(binding.thumbnailImageView.context)
				.load(houseModel.imgUrl)
				.transform(CenterCrop(), RoundedCorners(dpToPx(binding.thumbnailImageView.context,30)))
				.into(binding.thumbnailImageView)

			binding.root.setOnClickListener {
				itemClickListener(houseModel)
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return  ViewHolder(ItemHouseBinding.inflate(LayoutInflater.from(parent.context),parent,false))
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(currentList[position])
	}

	private fun dpToPx(context: Context, dp: Int ):Int{
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),context.resources.displayMetrics).toInt()
	}

	companion object{
		val diffUtil = object : DiffUtil.ItemCallback<HouseModel>(){
			override fun areItemsTheSame(oldItem: HouseModel, newItem: HouseModel): Boolean {
				return oldItem.id == newItem.id
			}

			override fun areContentsTheSame(oldItem: HouseModel, newItem: HouseModel): Boolean {
				return oldItem == newItem
			}
		}
	}
}