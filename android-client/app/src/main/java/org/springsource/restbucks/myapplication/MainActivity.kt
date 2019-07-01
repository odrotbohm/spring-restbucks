package org.springsource.restbucks.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.order_list_item.view.*
import org.springframework.hateoas.Link
import org.springsource.restbucks.myapplication.tasks.GetOrders

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        order_list.adapter = SimpleItemRecyclerViewAdapter(GetOrders().execute().get())
    }


    class SimpleItemRecyclerViewAdapter(
        private val values: List<HypermediaRemoteResource>
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->

                val item = v.tag as HypermediaRemoteResource

                val intent = Intent(v.context, OrderDetailsActivity::class.java).apply {
                    putExtra(Link.REL_SELF, item.getLink(Link.REL_SELF)?.href)
                }

                v.context.startActivity(intent)
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.order_list_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val item = values[position]
            val summary = item.getPayloadAs(OrderPreview::class)

            holder.idView.text = summary.getOrderedDate()
            holder.contentView.text = summary.getStatus()

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idView: TextView = view.id_text
            val contentView: TextView = view.content
        }

        interface OrderPreview {

            fun getOrderedDate() : String
            fun getStatus() : String
        }
    }
}
