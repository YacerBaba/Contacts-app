package owner.yacer.contactapp.Models

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recent_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import owner.yacer.contactapp.Activities.CreateNewContactActivity
import owner.yacer.contactapp.Activities.PreviewActivity
import owner.yacer.contactapp.Activities.PreviewRecentContactActivity
import owner.yacer.contactapp.R
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class RecentAdapter(
    val context: Context,
    var listRecent: LinkedList<RecentContact>,
    val fragment: Fragment? = null
) : RecyclerView.Adapter<RecentAdapter.ViewHolder>() {
    companion object {
        var recentExpandedItemPosition = -1
    }
    private val dbHelper = DbHelper(context = context)

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val photo = view.recent_photo
        val fullName = view.recent_fullName
        val btn_call = view.recent_btn_call
        val type: ImageView = view.recent_type
        val date = view.recent_date
        val cardView = view.recent_cardView
        val layoutExpanded = view.recent_layout_expanded
        val btn_sendMsg = view.recent_btn_sendMsg
        val btn_addToContacts = view.recent_btn_addToContacts

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.recent_item,
            parent,
            false
        )
        return ViewHolder(view)
    }

    fun setFilteredList(filteredList: LinkedList<RecentContact>) {
        listRecent = filteredList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var img = listRecent[position].img
        holder.photo.setImageBitmap(img)
        if (listRecent[position].fullName == null) {
            holder.fullName.text = listRecent[position].phone
            holder.btn_addToContacts.visibility = View.VISIBLE
        } else {
            holder.fullName.text = listRecent[position].fullName
            holder.btn_addToContacts.visibility = View.GONE
        }
        val format = SimpleDateFormat("EEE HH:mm")
        val milliseconds = listRecent[position].date
        val dateObj = Date(milliseconds)
        val date = format.format(dateObj)
        Log.e("msgAdapter", date)
        holder.date.text = date
        when (listRecent[position].type) {
            1 -> {//incoming calls
                holder.type.background =
                    ContextCompat.getDrawable(context, R.drawable.ic_incoming_call_24)
                holder.date.setTextColor(Color.BLACK)
            }
            2 -> {//outgoing calls
                holder.type.background =
                    ContextCompat.getDrawable(context, R.drawable.ic_outgoing_call_24)
                holder.date.setTextColor(Color.BLACK)
            }
            3 -> {//missed calls
                holder.type.background =
                    ContextCompat.getDrawable(context, R.drawable.ic_call_missed_24)
                holder.date.setTextColor(Color.rgb(241, 26, 26))
            }
        }
        holder.btn_call.setOnClickListener {
            if (!hasCallPhonePermission()) {
                requestPermission()
            } else {
                performCall(listRecent[position].phone!!)
            }
        }

        val isExpanded = recentExpandedItemPosition == position
        if (isExpanded) {
            holder.cardView.cardElevation = 10f
            Animations.expand(holder.layoutExpanded)
        } else {
            holder.cardView.cardElevation = 0f
            Animations.collapse(holder.layoutExpanded)
        }

        holder.cardView.setOnClickListener {
            val expandedLayout = holder.layoutExpanded
            if (isExpanded) { // if the current item is expanded then close it
                recentExpandedItemPosition = -1
                notifyItemChanged(position)
            } else { // if not , expand this item and close previous expanded item if exist
                val previousExpandedItemPosition = recentExpandedItemPosition
                recentExpandedItemPosition = position
                if (previousExpandedItemPosition != -1) { // means there is an expanded item and should close it
                    notifyItemChanged(previousExpandedItemPosition)
                }
                // expand this item
                notifyItemChanged(position)
            }
        }

        holder.btn_sendMsg.setOnClickListener {
            sendMessage(listRecent[position].phone!!)
        }
        holder.btn_addToContacts.setOnClickListener {
            Intent(context, CreateNewContactActivity::class.java).also {
                it.putExtra("fragment", 1)
                it.putExtra("phoneNumber", listRecent[position].phone)
                context.startActivity(it)
            }
        }
        holder.photo.setOnClickListener { view ->
            CoroutineScope(Dispatchers.Main).launch {
                val search = dbHelper.searchByPhoneNumber(listRecent[position].phone!!)
                if(search.isNotEmpty()){
                    val contact = search[0]
                    Intent(view.context, PreviewActivity::class.java).also {
                        it.putExtra("id",contact.id)
                        it.putExtra("firstName",contact.firstName)
                        it.putExtra("lastName",contact.lastName)
                        it.putExtra("phone",contact.phone)
                        it.putExtra("photo",contact.photo)
                        it.putExtra("address",contact.address)
                        it.putExtra("city",contact.city)
                        it.putExtra("favorite",contact.favorite)
                        it.putExtra("fromRecent","no")
                        context.startActivity(it)
                        (context as Activity).overridePendingTransition(
                            R.anim.slide_in_right,R.anim.slide_out_left
                        )
                    }
                }else{
                    Intent(view.context,PreviewRecentContactActivity::class.java).also {
                        it.putExtra("id",listRecent[position].rowid)
                        context.startActivity(it)
                        (context as Activity).overridePendingTransition(
                            R.anim.slide_in_right,R.anim.slide_out_left
                        )
                    }
                }

            }
        }

    }

    private fun hasCallPhonePermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CALL_PHONE
        ) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        fragment?.requestPermissions(
            arrayOf(android.Manifest.permission.CALL_PHONE), 101
        )
    }

    private fun performCall(number: String) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
        context.startActivity(intent)
    }

    private fun sendMessage(number: String) {
        val smsUri = Uri.parse("sms:$number")
        val intent = Intent(Intent.ACTION_SENDTO, smsUri)
        context.startActivity(intent)
    }

    override fun getItemCount(): Int = listRecent.size
}