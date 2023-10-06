package com.payment.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.phishing_payment_app.adapter.ContactAdapter
import com.payment.app.model.ContactModel

class ContactActivity : AppCompatActivity() {

    val contactList : MutableList<ContactModel> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var manager: RecyclerView.LayoutManager
    private lateinit var myAdapter: RecyclerView.Adapter<*>
    private lateinit var progressBar: ProgressBar
    var recCard: CardView? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        manager = LinearLayoutManager(this)
        progressBar = findViewById(R.id.progressBar)
        val showButton = findViewById<ImageView>(R.id.search_icon)
        val include = findViewById<ImageView>(R.id.iv_back)
        val title = findViewById<TextView>(R.id.title)
        val editText = findViewById<EditText>(R.id.ed_search)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALL_LOG), PackageManager.PERMISSION_GRANTED)
        val permission = Manifest.permission.READ_CONTACTS
        permissionLauncherSingle.launch(permission)
        recCard = findViewById(R.id.recCard)

        title.setText("Contacts")

        editText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do Nothing
            }

            override fun onTextChanged(query: CharSequence, start: Int, before: Int, count: Int) {
                val filteredList: MutableList<ContactModel> = ArrayList<ContactModel>()
                Log.d("query",query.toString())
                for (i in 0 until contactList.size) {
                    val text: String = contactList.get(i).toString().toLowerCase()
                    if (text.contains(query.toString().toLowerCase())) {
                        filteredList.add(contactList.get(i))
                    }
                }

                recyclerView = findViewById<RecyclerView>(R.id.contact_lis).apply{
                    myAdapter = ContactAdapter(applicationContext,filteredList)
                    layoutManager = manager
                    adapter = myAdapter
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Do Nothing
            }

        })

        showButton.setOnClickListener {

            val text = editText.text

//            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

        }

        include.setOnClickListener {
            onBackPressed()
            backActivity()
        }

    }

    private fun switchActivities() {
        val switchActivityIntent = Intent(this, PayAmountActivity::class.java)
        startActivity(switchActivityIntent)
    }

    private fun backActivity() {
        val switchActivityIntent = Intent(this, HomeActivity::class.java)
        startActivity(switchActivityIntent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPhoneContact() {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            Log.d("contactNumber", "contactNumber: ")

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 0)
        }
        else{
            progressBar.visibility = View.VISIBLE
            val handler = Handler()

            val contentResolver = getContentResolver()
            val uri: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val cursor = contentResolver.query(uri, null, null, null)
            Log.d("contactNumber", "contactNumber: " + cursor!!.count.toString())
            var image : Bitmap? = null

            if(cursor.count > 0) {
                while (cursor.moveToNext()){
                    val name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    val number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    val photo_uri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))

                    if(photo_uri != null){
//                        Log.e("photo_uri", photo_uri.toString())
//                        image = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(photo_uri))
                    }
                    else{
//                        image = photo_uri
                    }
                    contactList.add(ContactModel(name,number,image,name.substring(0,1)))
                    contactList.sortBy { it.name }
                }
            } else {
                false
            }
            Log.d("contactList",contactList.toString())
            handler.postDelayed(object :Runnable{
                override fun run() {
                    progressBar.visibility = View.INVISIBLE
                    recyclerView = findViewById<RecyclerView>(R.id.contact_lis).apply{
                        myAdapter = ContactAdapter(context,contactList)
                        layoutManager = manager
                        adapter = myAdapter
                    }
                }

            },1000)

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val permissionLauncherSingle = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
        isGranted ->
        if(isGranted){
            getPhoneContact()
//            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        }
        else{
            progressBar.visibility = View.INVISIBLE
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALL_LOG), PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, "Permission denied....", Toast.LENGTH_SHORT).show()
        }
    }

}