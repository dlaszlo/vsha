package hu.dlaszlo.vsha.graphql

import com.coxautodev.graphql.tools.GraphQLSubscriptionResolver
import hu.dlaszlo.vsha.graphql.dto.DeviceInfo
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.observables.ConnectableObservable
import org.reactivestreams.Publisher
import org.springframework.stereotype.Service

@Service
class SubscriptionResolver : GraphQLSubscriptionResolver {

    var publisher: Flowable<DeviceInfo>? = null

    var emitter: ObservableEmitter<DeviceInfo>? = null

    init {
        val observable: Observable<DeviceInfo> =
            Observable.create { emitter ->
                this@SubscriptionResolver.emitter = emitter
            }
        val connectableObservable: ConnectableObservable<DeviceInfo> = observable.share().publish()
        connectableObservable.connect()
        publisher = connectableObservable.toFlowable(BackpressureStrategy.BUFFER)
    }

    fun updateDeviceInfo(deviceId: String, name: String, online: Boolean, powerOn: Boolean) {
        val deviceInfo = DeviceInfo(
            deviceId,
            name,
            online,
            powerOn
        )
        emitter?.onNext(deviceInfo)
    }

    fun subscribeDeviceInfoUpdate(deviceId: String): Publisher<DeviceInfo> {
        return publisher!!.filter { d: DeviceInfo -> d.deviceId == deviceId }
    }
}