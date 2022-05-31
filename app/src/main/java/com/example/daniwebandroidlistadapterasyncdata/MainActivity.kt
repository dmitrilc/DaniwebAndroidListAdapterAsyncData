package com.example.daniwebandroidlistadapterasyncdata

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "MAIN_ACTIVITY"

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel>()

    //Re-usable request builder
    private val imageRequestBuilder = ImageRequest.Builder(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
            Performing the image loading in Activity code because
            Coil requires a context. Can also use AndroidViewModel if
            you want the ViewModel to do the image loading as well.
        */
        val imageLoader: (breed: String, url: String)->Unit = { breed, url ->
            val request = imageRequestBuilder
                .data(url)
                .build()

            lifecycleScope.launch(Dispatchers.IO){
                imageLoader.execute(request).drawable?.also {
                    //Sends image to ViewModel so it can update the UiState
                    viewModel.updateImage(it, breed)
                }
            }
        }

        //Pass the callback to ViewModel
        viewModel.imageLoader = imageLoader

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView_dog)
        val dogAdapter = DogAdapter(viewModel.imageUrlLoader).also {
            recyclerView.adapter = it
        }

        lifecycleScope.launch {
            viewModel.uiState.collect {
                //Submit list so ListAdapter can calculate the diff
                dogAdapter.submitList(it)
            }
        }
    }
}