package ru.is2si.sisi.presentation.points.point

import android.os.SystemClock
import ru.is2si.sisi.base.BasePresenter
import ru.is2si.sisi.base.device.location.Location
import ru.is2si.sisi.base.rx.RxSchedulers
import ru.is2si.sisi.domain.UseCase.None
import ru.is2si.sisi.domain.files.GetCurrentLocation
import ru.is2si.sisi.domain.files.SaveFilePathToQueue
import ru.is2si.sisi.domain.files.SubscribeUpdateLocation
import ru.is2si.sisi.presentation.model.LocationView
import ru.is2si.sisi.presentation.model.asView
import javax.inject.Inject

class PointPresenter @Inject constructor(
        private val rxSchedulers: RxSchedulers,
        private val saveFilePathToQueue: SaveFilePathToQueue,
        private val getCurrentLocation: GetCurrentLocation,
        private val subscribeUpdateLocation: SubscribeUpdateLocation
) : BasePresenter<PointContract.View>(), PointContract.Presenter {

    @Volatile
    private var isGetLocationInProgress = false
    @Volatile
    private var lastLocationUpdate: Long = SystemClock.elapsedRealtime()

    override var location: LocationView? = null
    override var isAccuracy: Boolean = false


    override fun start() {
        view.checkPermission()
    }

    override fun onCameraClick(isTest: Boolean) {
        if (isTest)
            locationData()
        else
            view.openCamera()
    }

    override fun permissionOk() {
        getLocation()
    }

    override fun addToPhotosQueue(photoPath: String) {
        disposables += saveFilePathToQueue.execute(SaveFilePathToQueue.Params(photoPath))
                .subscribeOn(rxSchedulers.io)
                .observeOn(rxSchedulers.ui)
                .subscribe(::locationData) { view.showError(it.message, it) }
    }

    private fun locationData() {
        getAccuracyLocation(false)
        // TODO: Red_byte 2019-10-28 remove if not use
//        if (isAccuracy) getAccuracyLocation(false)
//        else notAccuracy()
    }

    override fun getAccuracyLocation(isCenter: Boolean) {
        var counter = 0
        val locations = mutableListOf<LocationView>()
        disposables += subscribeUpdateLocation.execute(None)
                .subscribeOn(rxSchedulers.io)
                .observeOn(rxSchedulers.ui)
                .take(ACCURACY_COUNT.toLong())
                .doOnComplete {
                    if (isCenter)
                        view.showTestCoordinates(accuracy(locations))
                    else
                        view.showPhotoTestAccuracyCoordinates(accuracy(locations), locations.first())
                }
                .doOnNext {
                    counter++
                    val location = it.asView()
                    locations.add(location)
                    view.showTestAccuracyCoordinates(location, counter, isCenter)
                }
                .subscribe({ }) { view.showError(it.message, it) }
    }

    // TODO: Red_byte 2019-10-29 remove if not use
    private fun notAccuracy() {
        view.showLoading()
        disposables += getCurrentLocation.execute(None)
                .map(Location::asView)
                .subscribeOn(rxSchedulers.io)
                .observeOn(rxSchedulers.ui)
                .subscribe(view::showPhotoData) { view.showError(it.message, it) }
    }

    private fun accuracy(locations: List<LocationView>): LocationView {
        val latitudeList = locations
                .map { it.latitude }
                .sorted()
                .subList(1, ACCURACY_COUNT - 1)
        val longitudeList = locations
                .map { it.longitude }
                .sorted()
                .subList(1, ACCURACY_COUNT - 1)
        val latitude = latitudeList.sum() / latitudeList.size
        val longitude = longitudeList.sum() / longitudeList.size
        return LocationView(latitude, longitude)
    }

    override fun getLocation() {
        if (isGetLocationInProgress)
            return
        isGetLocationInProgress = true
        disposables += getCurrentLocation.execute(None)
                .map(Location::asView)
                .doOnSuccess { location = it }
                .doOnSuccess { lastLocationUpdate = SystemClock.elapsedRealtime() }
                .doAfterTerminate { isGetLocationInProgress = false }
                .subscribeOn(rxSchedulers.io)
                .observeOn(rxSchedulers.ui)
                .subscribe(view::showTestCoordinates) { /* no-op */ }
    }

    companion object {
        private const val ACCURACY_COUNT = 4
    }
}