package org.springsource.restbucks.myapplication.tasks

import android.os.AsyncTask
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import org.springframework.hateoas.Link
import org.springframework.hateoas.MediaTypes
import org.springsource.restbucks.myapplication.HypermediaRemoteResource

class GetOrder(val self : Link) : AsyncTask<String, String, HypermediaRemoteResource>() {

    val client = OkHttpClient()

    override fun doInBackground(vararg params: String?): HypermediaRemoteResource {

        return HypermediaRemoteResource(
            client.newCall(
                Request.Builder().get()
                    .url(self.href)
                    .addHeader("Accept", MediaTypes.HAL_JSON_VALUE)
                    .build()
            )
                .execute()
                .body()
                .string()
        )
    }
}