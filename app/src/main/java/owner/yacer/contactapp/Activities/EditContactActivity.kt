package owner.yacer.contactapp.Activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_create_new_contact_activty.*
import kotlinx.android.synthetic.main.activity_edit_contact.*
import owner.yacer.contactapp.Models.Contact
import owner.yacer.contactapp.Models.DbHelper
import owner.yacer.contactapp.R

class EditContactActivity : AppCompatActivity() {
    private val REQUEST_CODE = 77
    lateinit var dbHelper: DbHelper
    var imgBitmap:Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_contact)
        dbHelper = DbHelper(this)

        val contact = getContactInfo()
        bindContactData(contact)

        EditAct_btn_save.setOnClickListener {

            var address:String? = null
            var city:String? = null
            if(EditAct_et_contactFirstName.editText!!.text.toString().isEmpty()){
                EditAct_et_contactFirstName.error = "Enter the first name"
                EditAct_et_contactFirstName.requestFocus()
                return@setOnClickListener
            }
            if(EditAct_et_contactPhone.editText!!.text.toString().isEmpty()){
                EditAct_et_contactPhone.error = "Enter the phone number"
                EditAct_et_contactPhone.requestFocus()
                return@setOnClickListener
            }
            if(EditAct_et_contactAddress.editText!!.text.toString().isNotEmpty()){
                address = EditAct_et_contactAddress.editText!!.text.toString()
            }
            if(EditAct_et_contactCity.editText!!.text.toString().isNotEmpty()){
                city = EditAct_et_contactCity.editText!!.text.toString()
            }
            val firstName = EditAct_et_contactFirstName.editText!!.text.toString()
            val lastName = EditAct_et_contactLastName.editText!!.text.toString()
            val phone  = EditAct_et_contactPhone.editText!!.text.toString()
            val photo = if(imgBitmap!=null) imgBitmap else contact.photo
            val editedContact = Contact(contact.id,firstName,lastName, phone, photo, address, city)
            val success = dbHelper.editContact(contact = editedContact)
            if(success){
                Toast.makeText(this,"Changes saved",Toast.LENGTH_SHORT).show()
                finish()
            }


        }
        editAct_btn_cancel.setOnClickListener {
            finish()
        }
    }

    private fun getContactInfo():Contact{
        val id = intent.getIntExtra("id",-1)
        val firstName = intent.getStringExtra("firstName")
        val lastName = intent.getStringExtra("lastName")
        val phone = intent.getStringExtra("phone")
        val address = intent.getStringExtra("address")
        val city = intent.getStringExtra("city")
        val photo = intent.getParcelableExtra<Bitmap>("photo")
        val favorite = intent.getBooleanExtra("favorite",false)
        return Contact(id,firstName!!,lastName,phone!!,photo,address,city,favorite)
    }

    private fun bindContactData(contact: Contact){
        Glide.with(this).load(contact.photo).into(EditAct_selectPicture)
        EditAct_et_contactFirstName.editText!!.setText(contact.firstName)
        EditAct_et_contactLastName.editText!!.setText(contact.lastName)
        EditAct_et_contactPhone.editText!!.setText(contact.phone)
        EditAct_et_contactAddress.editText!!.setText(contact.address)
        EditAct_et_contactCity.editText!!.setText(contact.city)
    }
    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null){
            val selectedImageUri = data.data!!
            Glide.with(this).load(selectedImageUri).into(createAct_selectPicture)
            val inputStream = contentResolver.openInputStream(selectedImageUri) // open an input stream to the Uri
            imgBitmap = BitmapFactory.decodeStream(inputStream) // decode the input stream to a bitmap
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(
            androidx.appcompat.R.anim.abc_slide_in_top, androidx.appcompat.R.anim.abc_slide_out_bottom
        )
    }
}