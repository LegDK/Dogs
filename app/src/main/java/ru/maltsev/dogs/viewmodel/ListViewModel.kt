package ru.maltsev.dogs.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import ru.maltsev.dogs.model.DogBreed
import ru.maltsev.dogs.model.DogDatabase
import ru.maltsev.dogs.model.DogsApiService
import ru.maltsev.dogs.util.NotificationsHelper
import ru.maltsev.dogs.util.SharedPreferencesHelper

class ListViewModel(application: Application): BaseViewModel(application) {

    private val dogsService = DogsApiService()
    private val disposable = CompositeDisposable()
    private var prefHelper = SharedPreferencesHelper.invoke(getApplication())
    private var refreshTime = 10 * 60 * 1000 * 1000 * 1000L

    val dogs = MutableLiveData<List<DogBreed>>()
    val dogsLoadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun refresh() {
        checkCacheDuration()
        val updateTime = prefHelper.getUpdateTime()
        if (updateTime != null && updateTime != 0L && System.nanoTime() - updateTime < refreshTime) {
            fetchFromDatabase()
        } else {
            fetchFromRemote()
        }
    }

    private fun checkCacheDuration() {
        val cachePref = prefHelper.getCacheDuration()

        try {
            val cachePreferenceInt = cachePref?.toInt() ?: (5 * 60)
            refreshTime = cachePreferenceInt.times( 60 * 1000 * 1000 * 1000L)
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
    }

    fun refreshBypassCache() {
        fetchFromRemote()
    }

    fun filterByDatabase(searchQuery: String) {
        launch {
            val dogs = DogDatabase.invoke(getApplication()).dogDao().getFilteredDogs(searchQuery)
            dogsRetrieved(dogs)
        }
    }

    private fun fetchFromDatabase() {
        loading.value = true
        launch {
            val dogs = DogDatabase.invoke(getApplication()).dogDao().getAllGods()
            dogsRetrieved(dogs)
            Toast.makeText(getApplication(), "Dogs Retrieved from database", Toast.LENGTH_SHORT).show()
            NotificationsHelper(getApplication()).createNotification()
        }
    }

    private fun fetchFromRemote() {
        loading.value = true
        disposable.add(
            dogsService.getDogs()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object: DisposableSingleObserver<List<DogBreed>>() {
                    override fun onSuccess(dogList: List<DogBreed>) {
                        storeDogsLocally(dogList)
                    }

                    override fun onError(e: Throwable) {
                        loading.value = false
                        dogsLoadError.value = true
                        e.printStackTrace()
                    }

                })
        )
    }

    private fun dogsRetrieved(dogList: List<DogBreed>) {
        dogs.value = dogList
        loading.value = false
        dogsLoadError.value = false
    }

    private fun storeDogsLocally(list: List<DogBreed>) {
        launch {
            val dogDao = DogDatabase.invoke(getApplication()).dogDao()
            dogDao.deleteAllDogs()
            val result = dogDao.insertAll(*list.toTypedArray())
            var i = 0;
            while(i < list.size) {
                list[i].uuid = result[i].toInt()
                ++i
            }
            dogsRetrieved(list)
        }
        prefHelper.saveUpdateTime(System.nanoTime())
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}