package owner.yacer.contactapp.Activities

import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_preview.*
import kotlinx.coroutines.*
import owner.yacer.contactapp.Models.Contact
import owner.yacer.contactapp.Models.DbHelper
import owner.yacer.contactapp.R
import kotlin.Exception

class PreviewActivity : AppCompatActivity() {
    lateinit var dbHelper: DbHelper
    lateinit var contact: Contact
    var fromRecent: Boolean = false

    @RequiresApi(Build.VERSION_CODES.M)
    private var isTextViewVisible = true

    companion object {
        var img: Bitmap? = null
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        dbHelper = DbHelper(this)
        fromRecent = intent.getStringExtra("fromRecent") == "yes"

        val contactID = getContactID()
        contact = getContactInformation(contactID)

        bindContactToViews(contact)
        preview_addFavorite.setOnClickListener {
            contact.favorite = !contact.favorite
            if (contact.favorite) {
                iv_favorite.background =
                    ContextCompat.getDrawable(this, R.drawable.ic_star_filled_24)!!
                Toast.makeText(
                    this,
                    "${contact.firstName} added to favorite list",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                iv_favorite.background = ContextCompat.getDrawable(this, R.drawable.ic_star_24)!!
                Toast.makeText(
                    this,
                    "${contact.firstName} deleted from your favorite list",
                    Toast.LENGTH_SHORT
                ).show()
            }
            val success = dbHelper.updateFavorite(contact = contact)
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
                it.putExtra("addedAt", contact.addedAt)

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
                        CoroutineScope(Dispatchers.IO).launch {
                            val result = dbHelper.deleteContact(contact)
                            Log.e("msgDelete", "$result")
                            withContext(Dispatchers.Main) {
                                if (result) Toast.makeText(
                                    this@PreviewActivity,
                                    "1 Contact deleted",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                finish()
                            }
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
            if (!hasCallPhonePermission()) {
                requestPermission()
            } else {
                performCall(contact.phone)
            }
        }

        preview_btn_sendMsg.setOnClickListener {
            sendMessage(contact.phone)
        }

        preview_btn_videoCall.setOnClickListener {
            Toast.makeText(this, "Soon", Toast.LENGTH_SHORT).show()
        }

        preview_btn_cancel.setOnClickListener {
            finish()
        }

        preview_scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            val textViewRect = Rect()
            val isVisible = (preview_tv_contactName.getGlobalVisibleRect(textViewRect))

            if (!isVisible && isTextViewVisible) {
                // if the textView is not visible , show the app bar
                val fadeInAnimation = AlphaAnimation(0.0f, 1.0f).apply {
                    duration = 200
                }
                appBar_tb_contactName.text = preview_tv_contactName.text.toString()
                appBar_tb_contactName.visibility = View.VISIBLE
                appBar_tb_contactName.startAnimation(fadeInAnimation)
                isTextViewVisible = false
            } else if (isVisible && !isTextViewVisible) {
                // if the textView is visible , hide the app bar
                val fadeOutAnimation = AlphaAnimation(1.0f, 0.0f).apply {
                    duration = 150
                }
                isTextViewVisible = true
                appBar_tb_contactName.visibility = View.GONE
                appBar_tb_contactName.startAnimation(fadeOutAnimation)
            }


        }

        cardView_call.setOnClickListener {
            performCall(contact.phone)
        }
        cardView_call.setOnLongClickListener {
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", cv_tv_phone.text.toString())
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
            true
        }

        cardView_callViber.setOnClickListener {

        }

        cardView_msgWhatsapp.setOnClickListener {
            sendWhatsappMessage(contact.phone)
        }

        cardView_callWhatsapp.setOnClickListener {
            sendWhatsappMessage(contact.phone)
        }
        cardView_videoCallWhatsapp.setOnClickListener {
            sendWhatsappMessage(contact.phone)
        }

        cardView_msgTelegram.setOnClickListener {
            sendMsgTelegram("+213${contact.phone.toLong()}")
        }
        cardView_callTelegram.setOnClickListener {
            sendMsgTelegram("+213${contact.phone.toLong()}")
        }
        cardView_videoCallTelegram.setOnClickListener {
            sendMsgTelegram("+213${contact.phone.toLong()}")
        }


    }

    private fun bindRecentToViews() {
        preview_tv_addedAt.visibility = View.GONE
        preview_tv_contactName.text = intent.getStringExtra("fullName")
        val phone = intent.getStringExtra("phone")!!
        preview_iv_contactPhoto.setImageBitmap(img)
        cv_tv_phone.text = phone
        cv_tv_viberPhone.text = "(+213 ${phone.toLong()})"
        cv_tv_whatsAppMsgPhone.text = "+213 ${phone.toLong()}"
        cv_tv_whatsAppVideoPhone.text = "+213 ${phone.toLong()}"
        cv_tv_whatsAppVoicePhone.text = "+213 ${phone.toLong()}"
        cv_tv_telegramMsgPhone.text = "+213 ${phone.toLong()}"
        cv_tv_telegramVoicePhone.text = "+213 ${phone.toLong()}"
        cv_tv_telegramVideoPhone.text = "+213 ${phone.toLong()}"
    }

    private fun sendMsgTelegram(phone: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://t.me/$phone")
            intent.setPackage("org.telegram.messenger")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Something went wrong ,please check if you have already Telegram in your device",
                Toast.LENGTH_LONG
            ).show()
            Log.e("msgTelegram", e.message!!.toString())
        }
    }


    private fun sendWhatsappMessage(phone: String) {
        val url = "https://api.whatsapp.com/send?phone=$phone"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

    private fun performCall(number: String) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
        this.startActivity(intent)
    }

    private fun bindContactToViews(contact: Contact) {
        val fullName = "${contact.firstName} ${contact.lastName}"
        preview_tv_contactName.text = fullName
        iv_favorite.background = if (contact.favorite) {
            ContextCompat.getDrawable(this, R.drawable.ic_star_filled_24)
        } else {
            ContextCompat.getDrawable(this, R.drawable.ic_star_24)
        }
        preview_iv_contactPhoto.setImageBitmap(contact.photo)
        preview_tv_addedAt.text = "Added at ${contact.addedAt}"

        cv_tv_phone.text = contact.phone
        cv_tv_viberPhone.text = "(+213 ${contact.phone.toLong()})"
        cv_tv_whatsAppMsgPhone.text = "+213 ${contact.phone.toLong()}"
        cv_tv_whatsAppVideoPhone.text = "+213 ${contact.phone.toLong()}"
        cv_tv_whatsAppVoicePhone.text = "+213 ${contact.phone.toLong()}"
        cv_tv_telegramMsgPhone.text = "+213 ${contact.phone.toLong()}"
        cv_tv_telegramVoicePhone.text = "+213 ${contact.phone.toLong()}"
        cv_tv_telegramVideoPhone.text = "+213 ${contact.phone.toLong()}"
    }

    private fun sendMessage(number: String) {
        val smsUri = Uri.parse("sms:$number")
        val intent = Intent(Intent.ACTION_SENDTO, smsUri)
        startActivity(intent)
    }

    private fun getContactID(): Int = intent.getIntExtra("id", -1)


    private fun getContactInformation(id: Int): Contact {
        val dbHelper = DbHelper(this)
        val contact = dbHelper.getOneContact(id)
        return contact!!
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(
            android.R.anim.slide_in_left, android.R.anim.slide_out_right
        )
    }

    override fun onResume() {
        super.onResume()
        if (fromRecent) {
            bindRecentToViews()
        } else {
            val contact = getContactInformation(contact.id!!)
            bindContactToViews(contact)
        }
    }

    private fun hasCallPhonePermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.CALL_PHONE), 101
        )
    }
}