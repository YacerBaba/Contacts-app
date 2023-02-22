package owner.yacer.contactapp.Models


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.contact_item.view.*
import owner.yacer.contactapp.Activities.EditContactActivity
import owner.yacer.contactapp.Activities.PreviewActivity
import owner.yacer.contactapp.R
import java.util.*


class ContactAdapter(val context: Context, var listContacts: LinkedList<Contact>) :
    RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {
    private var isExpanded = false

    companion object {
        var expandedItemPosition: Int = -1
    }


    fun setFilteredList(filteredList: LinkedList<Contact>) {
        listContacts = filteredList
        notifyDataSetChanged()
    }

    inner class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fullName: TextView = view.contact_fullName
        val photo: CircleImageView = view.contact_photo
        val phoneNumber: TextView = view.contact_phoneNumber
        val layoutExpanded: LinearLayout = view.layout_expanded
        val cardView: CardView = view.contact_cardView
        val layoutMain: LinearLayout = view.layout_main
        val btn_call = view.btn_call
        val btn_sendMsg = view.btn_sendMsg
        val btn_addToFavorite = view.btn_addToFavorite
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.contact_item,
            parent,
            false
        )
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val fullName = "${listContacts[position].firstName} ${listContacts[position].lastName}"
        holder.fullName.text = fullName
        holder.photo.also {
            val photoBitmap = listContacts[position].photo
            Glide.with(it.context).load(photoBitmap).into(it)
        }
        holder.phoneNumber.text = "Mobile : ${listContacts[position].phone}"
        // check if the current item is expanded (at beginning all items should be not expanded)
        // cuz expandedItem = -1 and each time this item  is clicked it update the expandedItem and call
        // again all methods of a specific item to update its state (by making this check below)
        // notifyItemChange(position) is method used to update the state of a specific item
        val isExpanded = expandedItemPosition == position
        if (isExpanded) {
            holder.cardView.cardElevation = 10f
            Animations.expand(holder.layoutExpanded,holder.phoneNumber)
        } else {
            holder.cardView.cardElevation = 0f
            Animations.collapse(holder.layoutExpanded,holder.phoneNumber)
        }

        holder.cardView.setOnClickListener {
            val expandedLayout = holder.layoutExpanded
            if (isExpanded) { // if the current item is expanded then close it
                expandedItemPosition = -1
                notifyItemChanged(position)
            } else { // if not , expand this item and close previous expanded item if exist
                val previousExpandedItemPosition = expandedItemPosition
                expandedItemPosition = position
                if (previousExpandedItemPosition != -1) { // means there is an expanded item and should close it
                    notifyItemChanged(previousExpandedItemPosition)
                }
                // expand this item
                notifyItemChanged(position)
            }
        }

        holder.btn_call.setOnClickListener {

        }

        holder.btn_sendMsg.setOnClickListener {

        }

        holder.btn_addToFavorite.setOnClickListener {

        }

        holder.photo.setOnClickListener { view ->
            Intent(context,PreviewActivity::class.java).also {
                it.putExtra("id",listContacts[position].id)
                it.putExtra("firstName",listContacts[position].firstName)
                it.putExtra("lastName",listContacts[position].lastName)
                it.putExtra("phone",listContacts[position].phone)
                it.putExtra("photo",listContacts[position].photo)
                it.putExtra("address",listContacts[position].address)
                it.putExtra("city",listContacts[position].city)
                it.putExtra("favorite",listContacts[position].favorite)
                context.startActivity(it)
                (context as Activity).overridePendingTransition(
                    R.anim.slide_in_right,R.anim.slide_out_left
                )
            }
        }


    }

    override fun getItemCount(): Int = listContacts.size
}


