package com.atlassian.performance.tools.awsinfrastructure

import com.amazonaws.services.ec2.model.AvailabilityZone
import com.atlassian.performance.tools.aws.api.Aws
import org.apache.logging.log4j.LogManager
import java.util.*

/**
 * Randomizing helps spread resources over a wider capacity pool.
 * We avoid `eu-central-1c` AZ because it runs out of c4.8xlarge capacity all the time.
 */
internal fun Aws.pickAvailabilityZone(excludedZones: Set<String>): AvailabilityZone {
    val logger = LogManager.getLogger(this::class.java)

    val zone = this
        .availabilityZones
        .filter { it.zoneName != "eu-central-1c" }
        .filter { !excludedZones.contains(it.zoneName) }
        .shuffled()
        .first()
    logger.debug("Picked $zone")
    return zone
}


internal fun Aws.pickAvailabilityZone(): AvailabilityZone {
    return pickAvailabilityZone(Collections.emptySet())
}