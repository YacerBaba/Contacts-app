package owner.yacer.contactapp.Models

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.favorite_item.view.*
import owner.yacer.contactapp.Activities.PreviewActivity
import owner.yacer.contactapp.R

class FavoriteAdapter(val context: Context,var listFavorites:List<Contact>) : RecyclerView.Adapter<FavoriteAdapter.FavViewHolder>() {
    inner class FavViewHolder(view: View):RecyclerView.ViewHolder(view){
        val photo = view.favorite_photo
        val name = view.favorite_name
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.favorite_item,
            parent,
            false
        )
        return FavViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
        val fullName = "${listFavorites[position].firstName} ${listFavorites[position].lastName}"
        holder.name.text = fullName
        holder.photo.setImageBitmap(listFavorites[position].photo)
        holder.itemView.setOnClickListener {
            Intent(context, PreviewActivity::class.java).also {
                it.putExtra("id",listFavorites[position].id)
                it.putExtra("firstName",listFavorites[position].firstName)
                it.putExtra("lastName",listFavorites[position].lastName)
                it.putExtra("phone",listFavorites[position].phone)
                it.putExtra("photo",listFavorites[position].photo)
                it.putExtra("address",listFavorites[position].address)
                it.putExtra("city",listFavorites[position].city)
                it.putExtra("favorite",listFavorites[position].favorite)
                it.putExtra("fromRecent","no")
                context.startActivity(it)
                (context as Activity).overridePendingTransition(
                    R.anim.slide_in_right,R.anim.slide_out_left
                )
            }

        }
    }

    override fun getItemCount(): Int  = listFavorites.size
}