package hu.dlaszlo.vsha.graphql

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import hu.dlaszlo.vsha.config.KapcsoloFolyoso
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class MutationResolver : GraphQLMutationResolver {

    @Autowired
    lateinit var applicationContext: ApplicationContext

    fun powerOnKapcsoloFolyoso(): Boolean {
        return applicationContext.getBean(KapcsoloFolyoso::class.java).powerOn()
    }

    fun powerOffKapcsoloFolyoso(): Boolean {
        return applicationContext.getBean(KapcsoloFolyoso::class.java).powerOff()
    }

}