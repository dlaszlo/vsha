package hu.dlaszlo.vsha.controller

import hu.dlaszlo.vsha.config.KapcsoloKonyha
import org.springframework.hateoas.Link
import org.springframework.hateoas.Resource
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/kapcsoloKonyha")
class KapcsoloKonyhaController : KapcsoloKonyha() {

    class State(var deviceState: DeviceState, var actionState: Boolean)

    private fun getState(actionState: Boolean): Resource<State> {
        val links = arrayListOf<Link>()
        links.add(linkTo(methodOn(this::class.java).getDeviceState()).withSelfRel())
        if (state.online) {
            if (state.powerOn1) {
                links.add(linkTo(methodOn(this::class.java).powerOff1Rest()).withRel("powerOff1"))
            } else {
                links.add(linkTo(methodOn(this::class.java).powerOn1Rest()).withRel("powerOn1"))
            }
            if (state.powerOn2) {
                links.add(linkTo(methodOn(this::class.java).powerOff2Rest()).withRel("powerOff2"))
            } else {
                links.add(linkTo(methodOn(this::class.java).powerOn2Rest()).withRel("powerOn2"))
            }
        }
        return Resource(
            State(state, actionState),
            links
        )
    }

    @RequestMapping(produces = ["application/hal+json"])
    fun getDeviceState(): Resource<State> {
        return getState(true)
    }

    @RequestMapping("/powerOn1", produces = ["application/hal+json"])
    fun powerOn1Rest(): Resource<State> {
        val actionState = powerOn1()
        Thread.sleep(1000)
        return getState(actionState)
    }

    @RequestMapping("/powerOff1", produces = ["application/hal+json"])
    fun powerOff1Rest(): Resource<State> {
        val actionState = powerOff1()
        Thread.sleep(1000)
        return getState(actionState)
    }

    @RequestMapping("/powerOn2", produces = ["application/hal+json"])
    fun powerOn2Rest(): Resource<State> {
        val actionState = powerOn2()
        Thread.sleep(1000)
        return getState(actionState)
    }

    @RequestMapping("/powerOff2", produces = ["application/hal+json"])
    fun powerOff2Rest(): Resource<State> {
        val actionState = powerOff2()
        Thread.sleep(1000)
        return getState(actionState)
    }

}
