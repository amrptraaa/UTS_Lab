package com.example.uts_lab

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uts_lab.databinding.ActivityHistoryPageBinding
import com.google.firebase.database.*

class history_PageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryPageBinding
    private lateinit var database: DatabaseReference
    private lateinit var historyList: MutableList<historyData>
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menggunakan View Binding
        binding = ActivityHistoryPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyList = mutableListOf()
        adapter = HistoryAdapter(historyList)
        binding.historyRecyclerView.adapter = adapter

        // Inisialisasi Firebase Realtime Database di path "uploads"
        database = FirebaseDatabase.getInstance().getReference("uploads")

        // Memuat data dari Firebase
        fetchHistoryData()
    }

    private fun fetchHistoryData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                historyList.clear() // Kosongkan list sebelum menambahkan data baru
                for (dataSnapshot in snapshot.children) {
                    val historyData = dataSnapshot.getValue(historyData::class.java)
                    historyData?.let { historyList.add(it) }
                }
                // Notifikasi bahwa data sudah berubah
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Tangani kesalahan
            }
        })
    }
}
