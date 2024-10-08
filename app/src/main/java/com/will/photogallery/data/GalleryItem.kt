package com.will.photogallery.data

import android.net.Uri
import com.google.gson.annotations.SerializedName

/**
 * GSON 进行反序列化时，需要 bean 属性名字与 GSON字段对应
 *
 * 如果需要修改名字，使用 @SerializedName("url_s") 注解，告诉 GSON 这个字段对应
 * 的是 GSON 的哪个
 */
data class GalleryItem(var title: String = "",
                       var id: String = "",
                       @SerializedName("url_s") var url: String = "",
                       @SerializedName("owner") var owner: String = "") {

    // https://www.flickr.com/photos/owner/id
    val photoPageUri: Uri
        get() {
            return Uri.parse("https://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(owner)
                .appendPath(id)
                .build()
        }
}