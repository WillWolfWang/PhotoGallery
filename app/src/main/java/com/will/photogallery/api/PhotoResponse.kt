package com.will.photogallery.api

import com.google.gson.annotations.SerializedName
import com.will.photogallery.data.GalleryItem

class PhotoResponse {
    @SerializedName("photo")
    lateinit var galleryItems: List<GalleryItem>
}