package id.haris.galleryapplication

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CustomGalleryActivity : AppCompatActivity() {

    private val TAG = "CustomGalleryActivity";

    private lateinit var customGalleryFolderViewModel: CustomGalleryFolderViewModel
    private lateinit var spinner: Spinner
    private lateinit var btnSelect: TextView
    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private lateinit var folders: List<CustomGalleryFolder>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomGalleryAdapter
    private lateinit var selectedFiles: MutableList<String>

    companion object {
        val GET_FILES = "${BuildConfig.APPLICATION_ID}_custom_gallery_files"
    }


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
        selectedFiles = mutableListOf()
        folders = ArrayList()
        val folder: List<String> = ArrayList()
        spinnerAdapter = ArrayAdapter(this, R.layout.custom_item_gallery_spinner_selected, folder)
        spinnerAdapter.setDropDownViewResource(R.layout.custom_item_spinner_dropdown)
        spinner = findViewById(R.id.customGallerySpinner)
        spinner.adapter = spinnerAdapter

        btnSelect = findViewById(R.id.customGallerySelect)
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
    }

    private fun subscribeListener() {
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                val folder = CustomGalleryFolder("-1", spinner.getItemAtPosition(position).toString(), 3)
                val index = folders.indexOf(folder)
                val data = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> folders[index].id
                    else -> folders[index].title
                }
                customGalleryFolderViewModel.getFiles(3, data, selectedFiles)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        adapter.setOnClickListener(object : CustomGalleryAdapter.OnClickListener {
            override fun onClick(position: Int, model: CustomGallery) {
                val select = when {
                    model.select -> false
                    else -> true
                }
                if (select) {
                    if (selectedFiles.size == 10) {
                        Toast.makeText(this@CustomGalleryActivity, "Maksimal 10 File", Toast.LENGTH_SHORT).show()
                    } else {
                        model.select = select
                        selectedFiles.add(model.path)
                        adapter.updateModel(position, model)
                    }
                } else {
                    model.select = select
                    selectedFiles.remove(model.path)
                    adapter.updateModel(position, model)
                }
                btnSelect.text = "Pilih (${selectedFiles.size})"
            }
        })
    }

    fun back(view: View) {
        onBackPressed()
    }

    fun selectFiles(view: View) {
        if (selectedFiles.size > 0) {
            val returnIntent = Intent()
            returnIntent.putStringArrayListExtra(GET_FILES, selectedFiles as ArrayList<String>)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        } else {
            Toast.makeText(this, "Minimal harus memilih 1 file", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent)
        super.onBackPressed()
    }
}
