package hu.dlaszlo.vsha.backend.graphql

import graphql.kickstart.tools.GraphQLSubscriptionResolver
import hu.dlaszlo.vsha.backend.device.SwitchState
import hu.dlaszlo.vsha.backend.graphql.dto.DeviceInfo
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.observables.ConnectableObservable
import org.reactivestreams.Publisher
import org.springframework.stereotype.Service

@Service
class SubscriptionResolver : GraphQLSubscriptionResolver {

    final var publisher: Flowable<DeviceInfo>? = null

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

    fun updateDeviceInfo(deviceId: String, state: SwitchState) {
        val deviceInfo = DeviceInfo(
            deviceId,
            state.displayOrder,
            state.groupName,
            state.mqttName,
            state.name,
            state.online,
            state.powerOn
        )
        emitter?.onNext(deviceInfo)
    }

    fun subscribeDeviceInfoUpdate(deviceId: String): Publisher<DeviceInfo> {
        return publisher!!.filter { d: DeviceInfo -> d.deviceId == deviceId }
    }
}