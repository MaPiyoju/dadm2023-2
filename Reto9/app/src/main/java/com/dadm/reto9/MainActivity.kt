package com.dadm.reto9

import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.osmdroid.bonuspack.location.NominatimPOIProvider
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainActivity : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var map : MapView

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Request Location permission
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PERMISSION_GRANTED) {
            println("Location Permission GRANTED")
        } else {
            println("Location Permission DENIED")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }

        getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        setContentView(R.layout.activity_main)

        map = findViewById<MapView>(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true);

        map.mapCenter

        var mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)

        var controller = map.controller

        mMyLocationOverlay.enableMyLocation()
        mMyLocationOverlay.enableFollowLocation()
        mMyLocationOverlay.isDrawAccuracyEnabled = true
        mMyLocationOverlay.runOnFirstFix {
            runOnUiThread {
                controller.setCenter(mMyLocationOverlay.myLocation);
                controller.animateTo(mMyLocationOverlay.myLocation)
            }
        }

        val mapPoint = GeoPoint(4.637446, -74.083482)
        controller.animateTo(mapPoint)

        controller.setZoom(18)
        map.overlays.add(mMyLocationOverlay)


        mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
            val location: Location? = task.result
            if (location != null) {
                mapPoint.latitude = location.latitude
                mapPoint.longitude = location.longitude
            }
        }

        val poiProvider = NominatimPOIProvider("OSMBonusPackTutoUserAgent")
        val poiMarkers = FolderOverlay(this)
        getPOIAsync(poiProvider, mapPoint, "cafe", poiMarkers)
        getPOIAsync(poiProvider, mapPoint, "fuel", poiMarkers)
        getPOIAsync(poiProvider, mapPoint, "restaurant", poiMarkers)
        getPOIAsync(poiProvider, mapPoint, "library", poiMarkers)
        getPOIAsync(poiProvider, mapPoint, "parking", poiMarkers)
        getPOIAsync(poiProvider, mapPoint, "nursery", poiMarkers)
        getPOIAsync(poiProvider, mapPoint, "cycle parking", poiMarkers)

        map.overlays.add(poiMarkers)

        val mReceive: MapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(geoPoint: GeoPoint): Boolean {
                return false
            }
            override fun longPressHelper(p: GeoPoint): Boolean {
                return false
            }
        }
        val overlayEvents = MapEventsOverlay(mReceive)
        map.overlays.add(overlayEvents)

        map.postInvalidate()
    }

    fun getPOIAsync(poiProvider: NominatimPOIProvider, mapPoint: GeoPoint, tag: String, poiMarkers: FolderOverlay) = GlobalScope.async {
        val pois = poiProvider.getPOICloseTo(mapPoint, tag, 50, 0.1)
        for (poi in pois) {

            val poiMarker = Marker(map)
            poiMarker.title = poi.mType;
            poiMarker.snippet = poi.mDescription;
            poiMarker.position = poi.mLocation;
            //poiMarker.setIcon(poiIcon);
            if (poi.mThumbnail != null){
                //poiItem.setImage(new BitmapDrawable(poi.mThumbnail));
            }
            poiMarkers.add(poiMarker);
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionsToRequest = ArrayList<String>()
        var i = 0
        while (i < grantResults.size) {
            permissionsToRequest.add(permissions[i])
            i++
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }
}