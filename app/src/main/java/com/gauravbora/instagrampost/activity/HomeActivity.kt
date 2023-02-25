package com.gauravbora.instagrampost.activity

import PostAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gauravbora.instagrampost.R
import com.gauravbora.instagrampost.modal.Post
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class HomeActivity : AppCompatActivity() {
    private  lateinit var  recyclerView :RecyclerView
    private val REQUEST_CODE = 1
    private val imageUris = mutableListOf<Uri>()
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var databaseReference: DatabaseReference

    val list = arrayListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        recyclerView= findViewById(R.id.postRecyclerView)
        databaseReference=FirebaseDatabase.getInstance().getReference("media")
        recyclerView.layoutManager=LinearLayoutManager(this)

        val pickButton = findViewById<FloatingActionButton>(com.gauravbora.instagrampost.R.id.pickButton)
        pickButton.setOnClickListener {
                pickMedia()
        }

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    for (dataSnapshot in snapshot.children) {
                        val data = dataSnapshot.getValue(String::class.java) // Read data as a string
                        if (data != null) {
                            if(data.contains("images")||data.contains("videos")) {
                                val post = Post()
                                post.url = data
                                list.add(post)
                            }
                        }
                    }
                    recyclerView.adapter=PostAdapter(this@HomeActivity,list)
                }
            }

            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(applicationContext,"Error",Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun pickMedia() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/* video/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Select Media"), REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickMedia()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            val selectedMedia = mutableListOf<Uri>()
            data?.clipData?.let {
                for (i in 0 until it.itemCount) {
                    val uri = it.getItemAt(i).uri
                    selectedMedia.add(uri)
                }
            } ?: run {
                data?.data?.let {
                    selectedMedia.add(it)
                }
            }
            imageUris.addAll(selectedMedia)
            saveMediaToFirebase(selectedMedia)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun saveMediaToFirebase(selectedMedia: MutableList<Uri>) {
        val imagesRef = storage.reference.child("images")
        val videosRef = storage.reference.child("videos")
        for (uri in selectedMedia) {
            val mediaRef = if (uri.toString().contains("image")) {
                imagesRef.child(uri.lastPathSegment!!)
            } else {
                videosRef.child(uri.lastPathSegment!!)
            }
            mediaRef.putFile(uri).addOnSuccessListener {
                mediaRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    database.reference.child("media").push().setValue(downloadUrl.toString())
//                    Toast.makeText(this,"DONE",Toast.LENGTH_SHORT).show()

                }
            }
        }
    }
}

