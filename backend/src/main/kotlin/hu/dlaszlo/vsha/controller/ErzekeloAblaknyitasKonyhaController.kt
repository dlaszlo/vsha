package hu.dlaszlo.vsha.controller

import hu.dlaszlo.vsha.config.ErzekeloAblaknyitasKonyha
import org.springframework.hateoas.Resource
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/erzekeloAblaknyitasKonyha")
class ErzekeloAblaknyitasKonyhaController : ErzekeloAblaknyitasKonyha() {

    class State(var deviceState: DeviceState, var actionState: Boolean)

    @RequestMapping(produces = ["application/hal+json"])
    fun getDeviceState(): Resource<State> {
        return getState(true)
    }

    private fun getState(actionState: Boolean): Resource<State> {
        return Resource(
            State(state, actionState),
            linkTo(methodOn(this::class.java).getDeviceState()).withSelfRel()
        )
    }


}