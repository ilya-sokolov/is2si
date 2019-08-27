package ru.is2si.sisi.presentation.points

import ru.is2si.sisi.base.BasePresenter
import ru.is2si.sisi.base.rx.RxSchedulers
import ru.is2si.sisi.domain.UseCase.None
import ru.is2si.sisi.domain.points.GetSelectPoints
import ru.is2si.sisi.domain.points.RemoveSelectPoint
import ru.is2si.sisi.domain.points.SaveSelectPoint
import ru.is2si.sisi.presentation.model.PointView
import ru.is2si.sisi.presentation.model.asDomain
import ru.is2si.sisi.presentation.model.asView
import javax.inject.Inject

class PointsPresenter @Inject constructor(
        private val rxSchedulers: RxSchedulers,
        private val getSelectPoints: GetSelectPoints,
        private val saveSelectPoint: SaveSelectPoint,
        private val removeSelectPoint: RemoveSelectPoint
) : BasePresenter<PointsContract.View>(), PointsContract.Presenter {

    override fun start() {
        getPoints()
    }

    override fun getPoints() {
        view.showLoading()
        disposables += getSelectPoints.execute(None())
                .map { it.map { selectPoint -> selectPoint.asView() } }
                .subscribeOn(rxSchedulers.io)
                .observeOn(rxSchedulers.ui)
                .subscribe({
                    view.showMain()
                    view.showPoints(it)
                }) { view.showError(it.message) }
    }

    override fun addPoint(pointName: String) {
        view.showLoading()
        disposables += saveSelectPoint.execute(SaveSelectPoint.Params(pointName.toInt()))
                .map { it.map { point -> point.asView() } }
                .subscribeOn(rxSchedulers.io)
                .observeOn(rxSchedulers.ui)
                .subscribe({
                    view.showMain()
                    view.showPoints(it)
                }) {
                    view.showMain()
                    view.showSnack(it.message)
                }
    }

    override fun removePoint(point: PointView, position: Int) {
        view.showLoading()
        disposables += removeSelectPoint.execute(RemoveSelectPoint.Param(point.asDomain()))
                .map { it.map { point -> point.asView() } }
                .subscribeOn(rxSchedulers.io)
                .observeOn(rxSchedulers.ui)
                .subscribe({
                    view.showMain()
                    view.showPoints(it)
                }) { view.showError(it.message) }
    }

}