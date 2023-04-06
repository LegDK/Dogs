package ru.maltsev.dogs.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import ru.maltsev.dogs.model.DogBreed
import ru.maltsev.dogs.model.DogDatabase

class DetailViewModel(application: Application) : BaseViewModel(application) {

    val dog = MutableLiveData<DogBreed>()

    fun fetchById(dogId: Int) {
        launch {
            dog.value = DogDatabase.invoke(getApplication()).dogDao().getDog(dogId)
        }
    }
}