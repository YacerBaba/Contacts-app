package owner.yacer.contactapp.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import owner.yacer.contactapp.Models.Contact
import owner.yacer.contactapp.Models.ContactAdapter
import owner.yacer.contactapp.Models.DbHelper
import owner.yacer.contactapp.R

class MainActivity : AppCompatActivity() {
    lateinit var adapter: ContactAdapter
    lateinit var dbHelper: DbHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbHelper = DbHelper(this)
        setUpContactRecyclerView()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                val number = newText!!.toString().toDoubleOrNull()
                if(number==null){
                    val filteredList = dbHelper.searchByName(newText)
                    adapter.setFilteredList(filteredList)
                }else{
                    val filteredList = dbHelper.searchByPhoneNumber(newText)
                    adapter.setFilteredList(filteredList)
                }
                return true
            }

        })


        layout_addContact.setOnClickListener{
           Intent(this, CreateNewContactActivity::class.java).also{
               startActivity(it)
               overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
           }

        }
        layout_contact.setOnClickListener {

        }
        layout_recent.setOnClickListener {

        }

        layout_favorite.setOnClickListener {

        }
    }

    private fun setUpContactRecyclerView() {
        val listContacts = dbHelper.getContacts()
        adapter = ContactAdapter(this,listContacts)
        val layoutManager = LinearLayoutManager(this)
        main_rv_contacts.adapter = adapter
        main_rv_contacts.layoutManager = layoutManager
    }

    override fun onResume() {
        super.onResume()
        setUpContactRecyclerView()
    }
}