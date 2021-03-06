package `in`.devco.cr.util

import `in`.devco.cr.BuildConfig
import `in`.devco.cr.R
import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider

interface LocationListener {
    fun onPermissionGranted()
    fun onLocationFound(location: Location)
    fun onLocationNotFound()
}

fun checkLocationPermissions(activity: FragmentActivity, locationListener: LocationListener): Disposable {
    val rxPermissions = RxPermissions(activity)
    rxPermissions.setLogging(true)

    return rxPermissions
        .requestEachCombined(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        .subscribe { permission ->
            when {
                permission.granted -> locationListener.onPermissionGranted()
                else -> displayDialog(activity)
            }
        }
}

fun getLocationUpdate(context: Context, locationListener: LocationListener): Disposable {
    val reactiveLocationProvider = ReactiveLocationProvider(context)

    val locationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setFastestInterval(30000)
        .setInterval(60000)

    return reactiveLocationProvider
        .checkLocationSettings(
            LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)
                .build()
        )
        .subscribeOn(Schedulers.newThread())
        .flatMap { reactiveLocationProvider.getUpdatedLocation(locationRequest) }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ location ->
            locationListener.onLocationFound(location)
        }) { throwable ->
            locationListener.onLocationNotFound()
            Log.e("ERROR", "${throwable.message}")
        }
}

fun getLocation(context: Context, locationListener: LocationListener): Disposable {
    val reactiveLocationProvider = ReactiveLocationProvider(context)

    val locationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setFastestInterval(200)
        .setInterval(300)
        .setNumUpdates(1)

    return reactiveLocationProvider
        .checkLocationSettings(
            LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)
                .build()
        )
        .subscribeOn(Schedulers.newThread())
        .flatMap { reactiveLocationProvider.getUpdatedLocation(locationRequest) }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ location ->
            locationListener.onLocationFound(location)
        }) { throwable ->
            locationListener.onLocationNotFound()
            Log.e("ERROR", "${throwable.message}")
        }
}

fun displayDialog(context: Context) {
    MaterialDialog.Builder(context)
        .cancelable(false)
        .canceledOnTouchOutside(false)
        .title(R.string.title_settings_dialog)
        .content(R.string.rationale_ask_again)
        .positiveText(R.string.allow)
        .onPositive { _, _ ->
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                context.startActivity(this)
            }
        }
        .show()
}