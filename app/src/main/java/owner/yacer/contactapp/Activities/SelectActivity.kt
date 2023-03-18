package owner.yacer.contactapp.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_select.*
import owner.yacer.contactapp.Models.Contact
import owner.yacer.contactapp.Models.DbHelper
import owner.yacer.contactapp.Models.RecentContact
import owner.yacer.contactapp.Models.SelectAdapter
import owner.yacer.contactapp.R
import java.util.*

class SelectActivity : AppCompatActivity() {
    lateinit var selectAdapter: SelectAdapter
    lateinit var listContactsNotFavorite: LinkedList<Contact>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)
        val dbHelper = DbHelper(this)
        listContactsNotFavorite = dbHelper.getNotFavorites()
        setupRecyclerView()
        select_btn_cancel.setOnClickListener {
            finish()
        }
        select_search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val list = LinkedList<Contact>()
                for (i in listContactsNotFavorite.indices) {
                    if (listContactsNotFavorite[i].firstName.lowercase().contains(newText!!) ||
                        listContactsNotFavorite[i].lastName?.lowercase()!!.contains(newText)
                    ) {
                        list.add(listContactsNotFavorite[i])
                    }
                }
                selectAdapter.listContactsNotFavorite = list
                selectAdapter.notifyDataSetChanged()
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        selectAdapter = SelectAdapter(this, listContactsNotFavorite)
        val layoutManager = LinearLayoutManager(this)
        select_rv.adapter = selectAdapter
        select_rv.layoutManager = layoutManager
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(
            android.R.anim.slide_in_left, android.R.anim.slide_out_right
        )
    }
}