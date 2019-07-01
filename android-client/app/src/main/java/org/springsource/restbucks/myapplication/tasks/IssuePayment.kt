package org.springsource.restbucks.myapplication.tasks

import android.os.AsyncTask
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import org.springframework.hateoas.Link
import org.springframework.hateoas.MediaTypes
import org.springsource.restbucks.myapplication.HypermediaRemoteResource

class IssuePayment(private val self : Link) : AsyncTask<String, String, HypermediaRemoteResource>() {

    private val client = OkHttpClient()

    override fun doInBackground(vararg params: String?): HypermediaRemoteResource {

        val body = RequestBody.create(MediaType.parse("application/json"), "\"1234123412341234\"")

        return HypermediaRemoteResource(
            client.newCall(
                Request.Builder().put(body)
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