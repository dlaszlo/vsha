package hu.dlaszlo.vsha.graphql

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import hu.dlaszlo.vsha.config.KapcsoloFolyoso
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class Query : GraphQLQueryResolver {

    @Autowired
    lateinit var applicationContext: ApplicationContext

    fun getStateKapcsoloFolyoso() : hu.dlaszlo.vsha.config.KapcsoloFolyoso.DeviceState
    {
        return applicationContext.getBean(KapcsoloFolyoso::class.java).state
    }

}