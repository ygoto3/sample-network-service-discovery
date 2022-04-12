package com.ygoto3.sample_android_network_service_discovery.tv

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter

class DetailsDescriptionPresenter : AbstractDetailsDescriptionPresenter() {

    override fun onBindDescription(
        viewHolder: AbstractDetailsDescriptionPresenter.ViewHolder,
        item: Any
    ) {
        val movie = item as Movie

        viewHolder.title.text = movie.title
        viewHolder.subtitle.text = movie.studio
        viewHolder.body.text = movie.description
    }
}