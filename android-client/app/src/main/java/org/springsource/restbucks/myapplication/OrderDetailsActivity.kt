package org.springsource.restbucks.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_order_details.*
import org.springframework.hateoas.Link
import org.springsource.restbucks.myapplication.tasks.CancelOrder
import org.springsource.restbucks.myapplication.tasks.GetOrder
import org.springsource.restbucks.myapplication.tasks.IssuePayment

class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var resource : HypermediaRemoteResource

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_order_details)

        cancel_button.visibility = View.INVISIBLE
        payment_button.visibility = View.INVISIBLE
        ready_for_pickup.visibility = View.GONE

        val selfUri = intent.extras.getString(Link.REL_SELF)

        resource = GetOrder(Link(selfUri)).execute().get()


        val details = resource.getPayloadAs(OrderDetails::class)
        name.text = details.getOrderedDate() + " – " + details.getStatus()

        resource.ifPresent("restbucks:cancel") {

            with(cancel_button) {

                visibility = View.VISIBLE

                setOnClickListener {

                    CancelOrder(Link(selfUri)).execute().get()

                    it.context.startActivity(Intent(it.context, MainActivity::class.java))
                }
            }
        }

        resource.ifPresent("restbucks:payment") { link ->

            with (payment_button) {

                visibility = View.VISIBLE

                setOnClickListener {

                    resource = IssuePayment(link).execute().get()

                    finish()
                    startActivity(intent)
                }
            }
        }

        resource.ifPresent("restbucks:receipt") {
            ready_for_pickup.visibility = View.VISIBLE
        }

        overview_button.setOnClickListener {
            it.context.startActivity(Intent(it.context, MainActivity::class.java))
        }

        refresh_button.setOnClickListener {

            finish()
            startActivity(intent)
        }
    }

    interface OrderDetails {

        fun getOrderedDate() : String

        fun getStatus() : String
    }
}
