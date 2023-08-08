@file:Suppress("DEPRECATION")

package com.erendogan.konumgunluk.view

import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationListenerCompat
import androidx.room.Room
import com.erendogan.konumgunluk.R
import com.erendogan.konumgunluk.databinding.ActivityMapsBinding
import com.erendogan.konumgunluk.model.Konum
import com.erendogan.konumgunluk.roomDB.DB
import com.erendogan.konumgunluk.roomDB.KonumDAO
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListenerCompat
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private val konumIzin = "android.permission.ACCESS_FINE_LOCATION"
    private var secilenEnlem : Double? = null
    private var secilenBoylam : Double? = null
    private lateinit var db: DB
    private lateinit var dao:KonumDAO
    private val compositeDisposable = CompositeDisposable()
    private lateinit var gelenKonum: Konum

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        register()
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        db = Room.databaseBuilder(this,DB::class.java,"Konum").build()
        dao = db.konumDAO()

    }

    private fun register(){
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if (it){
                if (ActivityCompat.checkSelfPermission(this,konumIzin)!= PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Konum izni gereklidir",Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this,"Konum izni gereklidir",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (intent.hasExtra("Konum")){
            mMap.clear()
            val intent = intent
            gelenKonum = if (Build.VERSION.SDK_INT>=33){
                intent.getSerializableExtra("Konum",Konum::class.java) as Konum
            }
            else{
                intent.getSerializableExtra("Konum") as Konum
            }

            binding.button.visibility = View.GONE

            binding.textIsim.isEnabled = false
            binding.textIsim.hint = ""
            binding.textIsim.setText(gelenKonum.isim)

            val konum = LatLng(gelenKonum.enlem,gelenKonum.boylam)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(konum,15f))
            mMap.addMarker(MarkerOptions().position(konum).title(binding.textIsim.toString()))
        }

        else {
            mMap.clear()
            binding.button2.visibility = View.GONE

            mMap.setOnMapLongClickListener(this@MapsActivity)
            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
            locationListener = LocationListenerCompat { location ->
                println(location.toString())
                val konum = LatLng(location.latitude,location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(konum,15f))
                mMap.addMarker(MarkerOptions().position(konum).title("Konum"))
            }
            if (ActivityCompat.checkSelfPermission(this, konumIzin)!= PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,konumIzin)){
                    Snackbar.make(binding.root,"Konum İzni Gereklidir",Snackbar.LENGTH_INDEFINITE).setAction("Evet") {
                        permissionLauncher.launch(konumIzin)}.show()
                }
                else{
                    permissionLauncher.launch(konumIzin)
                }
            }
            else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,100,5f,locationListener)
                val sonKonum = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (sonKonum!=null){
                    val konum = LatLng(sonKonum.latitude,sonKonum.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(konum,10f))
                    mMap.addMarker(MarkerOptions().position(konum).title("Konum"))
                }
            }

        }

    }

    override fun onMapLongClick(p0: LatLng) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(p0))
        secilenEnlem = p0.latitude
        secilenBoylam = p0.longitude

    }

    fun kaydet (view : View){
        if (binding.textIsim.text.toString().isBlank()){
            Toast.makeText(this,"Lütfen İsim Giriniz",Toast.LENGTH_SHORT).show()
        }

        if (secilenEnlem !=null && secilenBoylam!=null) {
            val konum =  Konum(binding.textIsim.text.toString(), secilenEnlem!!,secilenBoylam!!)
            compositeDisposable.add(
                dao.ekle(konum)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handlerResponse)
            )
        }
        else{
            Toast.makeText(this,"Lütfen Konum Seçiniz",Toast.LENGTH_SHORT).show()
        }
    }

    private fun handlerResponse(){
        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    fun sil (view : View){
        compositeDisposable.add(
            dao.sil(gelenKonum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handlerResponse)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

}