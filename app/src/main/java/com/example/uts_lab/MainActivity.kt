package com.example.uts_lab

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import com.example.uts_lab.databinding.ActivityMainBinding

data class ImageData(val imageUrl: String = "", val uploadTime: Long = 0)

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter
    private val imageList = mutableListOf<ImageData>()
    private lateinit var database: DatabaseReference
    private val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnHistory: Button = findViewById(R.id.btn_history)
        btnHistory.setOnClickListener {
            val intent = Intent(this, history_PageActivity::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ImageAdapter(imageList)
        recyclerView.adapter = adapter

        // Firebase Database Reference
        database = FirebaseDatabase.getInstance().getReference("uploads")

        // Button to open camera
        findViewById<Button>(R.id.btnOpenCamera).setOnClickListener {
            openCamera()
        }

        // Fetch images from Firebase Realtime Database
        fetchImagesFromDatabase()
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            uploadImageToFirebase(imageBitmap)
        }
    }

    private fun uploadImageToFirebase(bitmap: Bitmap) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                saveImageDataToDatabase(uri.toString(), System.currentTimeMillis())
            }
        }.addOnFailureListener {
            Log.e("Firebase", "Upload failed: ${it.message}")
        }
    }

    private fun saveImageDataToDatabase(imageUrl: String, timestamp: Long) {
        val uploadId = database.push().key ?: return
        val uploadData = ImageData(imageUrl, timestamp)

        database.child(uploadId).setValue(uploadData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Firebase", "Data saved successfully")
            } else {
                Log.e("Firebase", "Data save failed")
            }
        }
    }

    private fun fetchImagesFromDatabase() {
        database.orderByChild("uploadTime").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                imageList.clear()
                for (dataSnapshot in snapshot.children) {
                    val imageData = dataSnapshot.getValue(ImageData::class.java)
                    if (imageData != null) {
                        imageList.add(imageData)
                    }
                }
                imageList.reverse() // Reverse to show the latest first
                adapter.notifyDataSetChanged()

                Log.d("Firebase", "Number of images: ${imageList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
            }
        })
    }
}

class ImageAdapter(private val imageList: List<ImageData>) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val imageView: android.widget.ImageView = itemView.findViewById(R.id.imageView)
        val textViewDate: android.widget.TextView = itemView.findViewById(R.id.textViewDate)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ImageViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageData = imageList[position]

        Glide.with(holder.itemView.context)
            .load(imageData.imageUrl)
            .into(holder.imageView)

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = sdf.format(Date(imageData.uploadTime))
        holder.textViewDate.text = formattedDate
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}
