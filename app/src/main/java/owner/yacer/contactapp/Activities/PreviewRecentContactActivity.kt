package owner.yacer.contactapp.Activities

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.PopupMenu
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_preview.*
import kotlinx.android.synthetic.main.activity_preview_recent_contact.*
import owner.yacer.contactapp.Models.DbHelper
import owner.yacer.contactapp.Models.RecentContact
import owner.yacer.contactapp.R

class PreviewRecentContactActivity : AppCompatActivity() {
    var recentContact: RecentContact? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_recent_contact)
        recentContact = getInfoFromIntent()
        bindInfoToView(recentContact)
        preview_recent_addContact.setOnClickListener {
            Intent(this, CreateNewContactActivity::class.java).also {
                it.putExtra("fragment", 1)
                it.putExtra("phoneNumber", recentContact!!.phone)
                startActivity(it)
                overridePendingTransition(
                    R.anim.slide_in_right,R.anim.slide_out_left
                )
            }
        }
        preview_recent_more.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.inflate(R.menu.recent_more_menu)
            popupMenu.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.rmi_block -> {
                        Toast.makeText(this,"${recentContact!!.phone} blocked",Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
        preview_recent_btn_cancel.setOnClickListener {
            finish()
        }
        preview_recent_btn_call.setOnClickListener {

            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${recentContact!!.phone}"))
            startActivity(intent)

        }
        preview_recent_btn_sendMsg.setOnClickListener {
            sendMessage(recentContact!!.phone!!)
        }
        preview_recent_btn_videoCall.setOnClickListener {
            Toast.makeText(this, "Soon", Toast.LENGTH_SHORT).show()
        }
        preview_recent_cardView_call.setOnClickListener {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${recentContact!!.phone}"))
            startActivity(intent)
        }
        preview_recent_cardView_call.setOnLongClickListener {
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", preview_recent_cv_tv_phone.text.toString())
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun sendMessage(number: String) {
        val smsUri = Uri.parse("sms:$number")
        val intent = Intent(Intent.ACTION_SENDTO, smsUri)
        startActivity(intent)
    }

    private fun bindInfoToView(recentContact: RecentContact?) {
        preview_recent_tv_contactName.text = recentContact?.phone
        preview_recent_iv_contactPhoto.setImageBitmap(recentContact!!.img)
        preview_recent_cv_tv_phone.text = recentContact.phone

    }

    private fun getInfoFromIntent(): RecentContact? {
        val dbHelper = DbHelper(this)
        val rowid = intent?.getIntExtra("id", -1)
        return dbHelper.getOneRecentContact(rowid!!)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(
            android.R.anim.slide_in_left, android.R.anim.slide_out_right
        )
    }
}