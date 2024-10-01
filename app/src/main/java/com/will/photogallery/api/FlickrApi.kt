package com.will.photogallery.api

import retrofit2.Call
import retrofit2.http.GET

interface FlickrApi {

    // 每一个函数都对应着一个特定的 http 请求，必须使用 HTTP 请求方法注解。
    // 作用是告诉 Retrofit，api 接口定义的各个函数映射的是哪一个 http 请求类型
    // @GET @POST
    @GET("/") // 字符串 "/" 表示一个相对路径 URL，针对 baseURL 来说的相对路径
    fun fetchContents():Call<String>// Call 的泛型参数是什么类型，Retrofit 在反序列化 Http
    // 响应数据后就会生成同样的数据类型，Retrofit 默认会把 Http 响应数据反序列化为一个
    // Okhttp.ResponseBody 对象。指定 Call<String> 就是告诉 Retrofit, 我们需要 String 对象
}