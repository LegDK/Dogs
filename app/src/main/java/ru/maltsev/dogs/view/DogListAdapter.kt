package ru.maltsev.dogs.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import ru.maltsev.dogs.R
import ru.maltsev.dogs.databinding.ItemDogBinding
import ru.maltsev.dogs.model.DogBreed
import ru.maltsev.dogs.util.getProgressDrawable
import ru.maltsev.dogs.util.loadImage

class DogListAdapter(private val dogsList: ArrayList<DogBreed>) :
    RecyclerView.Adapter<DogListAdapter.DogViewHolder>(), DogClickListener {

    class DogViewHolder(var view: ItemDogBinding) : RecyclerView.ViewHolder(view.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view =
            DataBindingUtil.inflate<ItemDogBinding>(inflater, R.layout.item_dog, parent, false)
        return DogViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dogsList.size
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        holder.view.dog = dogsList[position]
        holder.view.listener = this
    }

    fun updateDogsList(newDogsList: List<DogBreed>) {
        dogsList.clear()
        dogsList.addAll(newDogsList)
        notifyDataSetChanged()
    }

    fun filterDogsList(filterString: String) {
        val newDogsList = dogsList.filter { requireNotNull(it.dogBreed?.contains(filterString)) }
        dogsList.clear()
        dogsList.addAll(newDogsList)
        notifyDataSetChanged()
    }

    override fun onDogClicked(v: View) {
        val action = ListFragmentDirections.actionDetailFragment()
        action.dogUuid = v.findViewById<TextView>(R.id.dogId).text.toString().toInt()
        Navigation.findNavController(v).navigate(action)
    }
}