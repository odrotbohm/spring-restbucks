package org.springsource.restbucks.myapplication.tasks

import android.os.AsyncTask
import com.jayway.jsonpath.JsonPath
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import net.minidev.json.JSONArray
import org.springframework.hateoas.MediaTypes
import org.springsource.restbucks.myapplication.HypermediaRemoteResource

class GetOrders : AsyncTask<String, String, List<HypermediaRemoteResource>>() {

    val client = OkHttpClient()

    override fun doInBackground(vararg params: String?): List<HypermediaRemoteResource> {

        val orders = client.newCall(
            Request.Builder().get()
                .url("http://10.0.2.2:8080/orders")
                .addHeader("Accept", MediaTypes.HAL_JSON_VALUE)
                .build()
        )
            .execute()
            .body()
            .string()

        return JsonPath
            .compile("$._embedded.restbucks:orders[*]._links.self.href")
            .read<JSONArray>(orders)
            .map { Request.Builder().get().url(it.toString()).build() }
            .map { client.newCall(it).execute().body().string() }
            .map { HypermediaRemoteResource(it) }

    }
}