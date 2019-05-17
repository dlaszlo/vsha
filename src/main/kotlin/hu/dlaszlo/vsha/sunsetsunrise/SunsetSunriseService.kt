package hu.dlaszlo.vsha.sunsetsunrise

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.*
import kotlin.math.*

@Component
class SunsetSunriseService {

    @Value("\${longitude}")
    var longitude: Double = 0.0

    @Value("\${latitude}")
    var latitude: Double = 0.0

    @Value("\${timeZone}")
    lateinit var timeZone: String

    @Value("\${zenith}")
    lateinit var zenith: Zenith

    private fun adjust(d: Double, min: Double, max: Double): Double {
        return when {
            d >= max -> d - max
            d < min -> d + max
            else -> d
        }
    }

    private fun calculate(
        date: LocalDate,
        longitude: Double,
        latitude: Double,
        zenith: Zenith,
        timeZone: String,
        sunRising: Boolean
    ): LocalTime? {

        // 1. first calculate the day of the year
        val n = date.dayOfYear

        // 2. convert the longitude to hour value and calculate an approximate time
        val lngHour = longitude / 15

        val t = when {
            sunRising -> n + ((6 - lngHour) / 24)
            else -> n + ((18 - lngHour) / 24)
        }

        // 3. calculate the Sun's mean anomaly
        val m = (0.9856 * t) - 3.289

        // 4. calculate the Sun's true longitude
        val l = adjust(
            m + (1.916 * sin(m * PI / 180)) + (0.020 * sin(2 * m * PI / 180)) + 282.634,
            0.0, 360.0
        )

        // 5a. calculate the Sun's right ascension
        var ra = adjust(
            atan(0.91764 * tan(l * PI / 180)) * 180 / PI,
            0.0, 360.0
        )

        // 5b. right ascension value needs to be in the same quadrant as L
        val lq = (floor(l / 90)) * 90
        val raq = (floor(ra / 90)) * 90
        ra += (lq - raq)

        // 5c. right ascension value needs to be converted into hours
        ra /= 15

        // 6. calculate the Sun's declination
        val sinDec = 0.39782 * sin(l * PI / 180)
        val cosDec = cos(asin(sinDec))

        // 7a. calculate the Sun's local hour angle
        val cosH =
            (cos(zenith.degrees * PI / 180) - (sinDec * sin(latitude * PI / 180))) / (cosDec * cos(latitude * PI / 180))

        // cosH >  1: the sun never rises on this location (on the specified date)
        // cosH < -1: the sun never sets on this location (on the specified date)
        if (cosH > 1 || cosH < -1) {
            throw IllegalArgumentException()
        }

        // 7b. finish calculating H and convert into hours
        val h = when {
            sunRising -> 360 - (acos(cosH) * 180 / PI)
            else -> (acos(cosH) * 180 / PI)
        } / 15

        // calculate local mean time of rising/setting
        val lt = h + ra - (0.06571 * t) - 6.622

        // adjust back to UTC
        val ut = adjust(lt - lngHour, 0.0, 24.0)

        // convert UT value to local time zone of latitude/longitude
        val utcTime = LocalTime.ofSecondOfDay((ut * 3600).toLong())
        val utcZoned = ZonedDateTime.of(date, utcTime, ZoneId.of("UTC"))
        val localZoned = utcZoned.withZoneSameInstant(ZoneId.of(timeZone))

        return localZoned.toLocalTime()
    }

    fun isDaylight(): Boolean {
        val localDateTime = LocalDateTime.now(ZoneId.of(timeZone))
        val sunRise = calculate(localDateTime.toLocalDate(), longitude, latitude, zenith, timeZone, true)
        val sunSet = calculate(localDateTime.toLocalDate(), longitude, latitude, zenith, timeZone, false)

        logger.info("Napkelte: {}, Napnyugta: {}, Típus: {}, {}'", sunRise, sunSet, zenith.name, zenith.degrees)

        return when {
            sunRise == null -> false
            sunSet == null -> true
            else -> localDateTime.toLocalTime().isAfter(sunRise) && !localDateTime.toLocalTime().isAfter(sunSet)
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(SunsetSunriseService::class.java)!!
    }

}

