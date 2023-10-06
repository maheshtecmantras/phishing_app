package com.example.phishing_payment_app.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.mikhaellopez.circularimageview.CircularImageView
import com.payment.app.PayAmountActivity
import com.payment.app.R
import com.payment.app.model.ContactModel


class ContactAdapter(private val context: Context, private val data: List<ContactModel>) : RecyclerView.Adapter<ContactAdapter.MyViewHolder>()  {

    var onItemClick: ((ContactModel) -> Unit)? = null

    class MyViewHolder(val view: View): RecyclerView.ViewHolder(view){
        var recCard: CardView? = null

        fun bind(property: ContactModel){
            val tv_number = view.findViewById<TextView>(R.id.tv_number)
            val imageView = view.findViewById<ImageView>(R.id.contact_profile)
            val tv_name = view.findViewById<TextView>(R.id.tv_name)
            val tv_single_char = view.findViewById<TextView>(R.id.single_char)
            recCard = itemView.findViewById(R.id.recCard)

            tv_number.text = property.number
            tv_name.text = property.name
            tv_single_char.text = property.singleChar
            imageView.setImageBitmap(property.image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.contact_child, parent, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(data[position])
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(data[position])
            Log.d("OnTap", "onTap ${data[position]}")
            val intent = Intent(context, PayAmountActivity::class.java)
            intent.putExtra("contactNumber", data.get(holder.adapterPosition).number)
            intent.putExtra("name", data.get(holder.adapterPosition).name)
            context.startActivity(intent)

        }
    }

}