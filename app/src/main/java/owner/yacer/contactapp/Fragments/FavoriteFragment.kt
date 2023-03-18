package owner.yacer.contactapp.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_favorite.*
import owner.yacer.contactapp.Activities.SelectActivity
import owner.yacer.contactapp.Models.Contact
import owner.yacer.contactapp.Models.DbHelper
import owner.yacer.contactapp.Models.FavoriteAdapter
import owner.yacer.contactapp.Models.RecentContact
import owner.yacer.contactapp.R
import java.util.*


class FavoriteFragment : Fragment(R.layout.fragment_favorite) {
    lateinit var dbHelper:DbHelper
    lateinit var listFavorites:List<Contact>
    lateinit var adapter: FavoriteAdapter
    lateinit var observer: RecyclerView.AdapterDataObserver
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DbHelper(requireContext())
        listFavorites = dbHelper.getFavorites()
        Log.e("msgFav",listFavorites.toString())
        setupRecyclerView()
        favorite_btn_add.setOnClickListener {
            Intent(requireContext(),SelectActivity::class.java).also{
                startActivity(it)
                requireActivity().overridePendingTransition(
                    R.anim.slide_in_right , R.anim.slide_out_left
                )
            }
        }
        fav_searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                val list = LinkedList<Contact>()
                    for (i in listFavorites.indices) {
                        if (listFavorites[i].firstName.lowercase().contains(newText!!)
                            ||listFavorites[i].lastName?.lowercase()!!.contains(newText) ) {
                            list.add(listFavorites[i])
                        }
                    }
                    adapter.listFavorites = list
                    adapter.notifyDataSetChanged()
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = FavoriteAdapter(requireContext(),listFavorites)
        favlayout_noFavorites.visibility = if(adapter.itemCount==0) View.VISIBLE else View.GONE
        observer = object :RecyclerView.AdapterDataObserver(){
            override fun onChanged() {
                super.onChanged()
                val count = adapter.itemCount
                favlayout_noFavorites.visibility = if(count==0) View.VISIBLE else View.GONE
            }
        }
        adapter.registerAdapterDataObserver(observer)
        val layoutManager = GridLayoutManager(requireContext(),3)
        fav_rv_contacts.adapter = adapter
        fav_rv_contacts.layoutManager = layoutManager
    }

    override fun onResume() {
        super.onResume()
        listFavorites = dbHelper.getFavorites()
        setupRecyclerView()
    }
}

