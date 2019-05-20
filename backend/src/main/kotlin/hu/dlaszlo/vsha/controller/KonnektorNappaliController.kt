package hu.dlaszlo.vsha.controller

import hu.dlaszlo.vsha.config.KonnektorNappali
import org.springframework.hateoas.Link
import org.springframework.hateoas.Resource
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/konnektorNappali")
class KonnektorNappaliController : KonnektorNappali() {

    class State(var deviceState: DeviceState, var actionState: Boolean)

    private fun getState(actionState: Boolean): Resource<State> {
        val links = arrayListOf<Link>()
        links.add(linkTo(methodOn(this::class.java).getDeviceState()).withSelfRel())
        if (state.online) {
            if (state.powerOn) {
                links.add(linkTo(methodOn(this::class.java).powerOffRest()).withRel("powerOff"))
            } else {
                links.add(linkTo(methodOn(this::class.java).powerOnRest()).withRel("powerOn"))
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

    @RequestMapping("/powerOn", produces = ["application/hal+json"])
    fun powerOnRest(): Resource<State> {
        val actionState = powerOn()
        Thread.sleep(1000)
        return getState(actionState)
    }

    @RequestMapping("/powerOff", produces = ["application/hal+json"])
    fun powerOffRest(): Resource<State> {
        val actionState = powerOff()
        Thread.sleep(1000)
        return getState(actionState)
    }

}