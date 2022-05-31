package com.example.daniwebandroidlistadapterasyncdata

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException

private const val TAG = "VIEW_MODEL"

class MainViewModel : ViewModel() {
    private val httpClient = DogService.INSTANCE

    //Adapter will invoke this
    val imageUrlLoader: (String)->Unit = { breed ->
        if (!_imageUrlCache.value.containsKey(breed)){
            loadImageUrl(breed)
        }
    }

    //Self will invoke this
    var imageLoader: ((breed: String, url: String)->Unit)? = null

    private val _uiState = MutableStateFlow<List<DogUiState>>(listOf())
    val uiState = _uiState.asStateFlow()

    private val _imageUrlCache = MutableStateFlow<Map<String, String?>>(mapOf())

    //Fine-grained thread confinement. Performance penalty.
    private val mutex = Mutex()

    init {
        //Gets breeds
        viewModelScope.launch(Dispatchers.IO) {
            try {
                httpClient.getAllBreeds()?.message?.let { breeds ->
                    if (breeds.isNotEmpty()){
                        val state = breeds.keys
                            .map {
                                DogUiState(breed = it)
                            }

                        _uiState.value = state
                    }
                }
            } catch (e: IOException){
                e.printStackTrace()
            }
        }
    }

    private fun loadImageUrl(breed: String) {
        //Adding the breed key so observers know that there is already
        // pending async loading operation
        _imageUrlCache.value = _imageUrlCache.value.plus(breed to null)

        viewModelScope.launch(Dispatchers.IO){
            try {
                //Loading image URL
                httpClient.getImageUrlByBreed(breed)
                    ?.message
                    ?.let {
                        mutex.withLock {
                            //Adds url to the URL cache
                            _imageUrlCache.value = _imageUrlCache.value.plus(breed to it)
                        }

                        //Starts loading images
                        imageLoader?.invoke(breed, it)
                    }
            } catch (e: IOException){
                e.printStackTrace()
            }
        }
    }

    fun updateImage(drawable: Drawable, breed: String){
        //Updates UiState with image
        viewModelScope.launch(Dispatchers.IO) {
            mutex.withLock {
                _uiState.value = _uiState.value.map {
                    if (it.breed == breed){
                        it.copy(image = drawable)
                    } else {
                        it
                    }
                }
            }
        }
    }
}