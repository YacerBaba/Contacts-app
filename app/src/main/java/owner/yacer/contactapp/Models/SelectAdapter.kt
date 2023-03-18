package owner.yacer.contactapp.Models

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.select_item.view.*
import owner.yacer.contactapp.Activities.SelectActivity
import owner.yacer.contactapp.R
import java.util.*

class SelectAdapter(val context:Context, var listContactsNotFavorite: LinkedList<Contact>) :
    RecyclerView.Adapter<SelectAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val select_item = view.select_item
        val photo = view.select_iv_contact
        val fullName = view.select_tv_contact
        val btn_add = view.btn_add
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.select_item,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentContact = listContactsNotFavorite[position]
        holder.photo.setImageBitmap(listContactsNotFavorite[position].photo)
        holder.fullName.text = "${currentContact.firstName} ${currentContact.lastName}"
        holder.select_item.setOnClickListener {  }
        val dbHelper = DbHelper(context)
        holder.btn_add.setOnClickListener {
            currentContact.favorite = true
            dbHelper.updateFavorite(currentContact)
            listContactsNotFavorite.remove(currentContact)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int  = listContactsNotFavorite.size
}