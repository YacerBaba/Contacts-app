package owner.yacer.contactapp.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_preview.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import owner.yacer.contactapp.Models.Contact
import owner.yacer.contactapp.Models.DbHelper
import owner.yacer.contactapp.R

class PreviewActivity : AppCompatActivity() {
    lateinit var dbHelper: DbHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        dbHelper = DbHelper(this)
        val contactID = getContactID()
        val contact = getContactInformation(contactID)
        bindContactToViews(contact)
        preview_addFavorite.setOnClickListener {
            val isFavorite =
                iv_favorite.background.constantState == it.context.getDrawable(R.drawable.ic_star_filled_24)!!.constantState
            if (isFavorite) {
                iv_favorite.background = resources.getDrawable(R.drawable.ic_star_24)
                contact.favorite = false
                Toast.makeText(
                    this,
                    "${contact.firstName} deleted from your favorite list",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                iv_favorite.background = resources.getDrawable(R.drawable.ic_star_filled_24)
                contact.favorite = true
                Toast.makeText(
                    this,
                    "${contact.firstName} added to favorite list",
                    Toast.LENGTH_SHORT
                ).show()
            }
            val success = dbHelper.setFavorite(contact = contact)
            if (success) {
                return@setOnClickListener
            }
            Toast.makeText(
                this,
                "Something went wrong",
                Toast.LENGTH_SHORT
            ).show()
        }

        preview_edit.setOnClickListener {
            Intent(this, EditContactActivity::class.java).also {
                it.putExtra("id", contact.id)
                it.putExtra("firstName", contact.firstName)
                it.putExtra("lastName", contact.lastName)
                it.putExtra("phone", contact.phone)
                it.putExtra("photo", contact.photo)
                it.putExtra("address", contact.address)
                it.putExtra("city", contact.city)
                it.putExtra("favorite", contact.favorite)
                startActivity(it)
                overridePendingTransition(
                    androidx.appcompat.R.anim.abc_slide_in_bottom,
                    androidx.appcompat.R.anim.abc_slide_out_top
                )
            }
        }

        preview_more.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            popupMenu.inflate(R.menu.more_menu)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.mi_delete -> {
                        val result = dbHelper.deleteContact(contact)
                        if (result) Toast.makeText(this, "1 Contact deleted", Toast.LENGTH_SHORT)
                            .show()
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(1000)
                            finish()
                        }
                        true
                    }
                    R.id.mi_lock -> {
                        Toast.makeText(this, "Soon..", Toast.LENGTH_SHORT)
                            .show()
                        true
                    }
                    R.id.mi_Accessibility -> {
                        Toast.makeText(this, "Soon..", Toast.LENGTH_SHORT)
                            .show()
                        true
                    }
                    R.id.mi_search -> {
                        Toast.makeText(this, "Soon..", Toast.LENGTH_SHORT)
                            .show()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        preview_btn_call.setOnClickListener {
            makeCall(contact)
        }

        preview_btn_sendMsg.setOnClickListener {
            sendMessage(contact)
        }

        preview_btn_videoCall.setOnClickListener {

        }

        preview_btn_cancel.setOnClickListener {
            finish()
        }

    }

    private fun bindContactToViews(contact: Contact) {
        val fullName = "${contact.firstName} ${contact.lastName}"
        preview_tv_contactName.text = fullName
        iv_favorite.background = if(contact.favorite){
            ContextCompat.getDrawable(this,R.drawable.ic_star_filled_24)
        }else{
            ContextCompat.getDrawable(this,R.drawable.ic_star_24)
        }
        preview_iv_contactPhoto.setImageBitmap(contact.photo)

    }

    private fun sendMessage(contact: Contact) {

    }

    private fun makeCall(contact: Contact) {

    }

    private fun getContactID(): Int = intent.getIntExtra("id", -1)


    private fun getContactInformation(id:Int) :Contact{
        val dbHelper = DbHelper(this)
        val contact = dbHelper.getOneContact(id)
        return contact!!
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(
            android.R.anim.slide_in_left,android.R.anim.slide_out_right
        )
    }
}