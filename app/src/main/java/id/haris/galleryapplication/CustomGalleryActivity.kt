package id.haris.galleryapplication

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CustomGalleryActivity : AppCompatActivity() {

    private val TAG = "CustomGalleryActivity";

    private lateinit var customGalleryFolderViewModel: CustomGalleryFolderViewModel
    private lateinit var spinner: Spinner
    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private lateinit var folders: List<CustomGalleryFolder>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomGalleryAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_gallery)

        initComponents()

        subscribeObservers()
        subscribeListener()

        customGalleryFolderViewModel.getFolders(3)
    }

    private fun initComponents() {
        customGalleryFolderViewModel = ViewModelProvider(this).get(CustomGalleryFolderViewModel::class.java)

        folders = ArrayList()
        val folder: List<String> = ArrayList()
        spinnerAdapter = ArrayAdapter(this, R.layout.custom_item_gallery_spinner_selected, folder)
        spinnerAdapter.setDropDownViewResource(R.layout.custom_item_spinner_dropdown)
        spinner = findViewById(R.id.customGallerySpinner)
        spinner.adapter = spinnerAdapter

        recyclerView = findViewById(R.id.customGalleryRecycler)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(20)
        recyclerView.addItemDecoration(GridSpacingItemDecoration(3, 5, true))
        adapter = CustomGalleryAdapter()
        recyclerView.adapter = adapter
    }

    private fun subscribeObservers() {
        customGalleryFolderViewModel.getFiles().observe(this, Observer { files ->
            if (files != null) {
                adapter.setModel(files)
                adapter.notifyDataSetChanged()
            }
        })

        customGalleryFolderViewModel.getFolders().observe(this, Observer { folders ->
            if (folders != null) {
                this.folders = folders
            }
        })

        customGalleryFolderViewModel.getFolderNames().observe(this, Observer { folders ->
            if (folders != null) {
                spinnerAdapter.addAll(folders)
                spinnerAdapter.notifyDataSetChanged()

            }
        })

        customGalleryFolderViewModel.getUpdatedFile().observe(this, Observer { file ->
            if (file != null) {
                adapter.updateModel(file)
            }
        })
    }

    private fun subscribeListener() {
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                val folder = CustomGalleryFolder("-1", spinner.getItemAtPosition(position).toString(), 3)
                val index = folders.indexOf(folder)
                customGalleryFolderViewModel.getFiles(3, folders[index].id)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        adapter.setOnClickListener(object : CustomGalleryAdapter.OnClickListener {
            override fun onClick(position: Int, model: CustomGallery) {
                val select = when {
                    model.select -> false
                    else -> true
                }
                model.select = select
                customGalleryFolderViewModel.updateFile(model)
            }
        })
    }

    fun back(view: View) {}
    fun selectFiles(view: View) {}
}
