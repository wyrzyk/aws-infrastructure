package com.atlassian.performance.tools.awsinfrastructure

import com.amazonaws.services.cloudformation.model.ListStackResourcesRequest
import com.amazonaws.services.cloudformation.model.ResourceStatus
import com.amazonaws.services.cloudformation.model.StackResourceSummary
import com.amazonaws.services.ec2.model.Tag
import com.amazonaws.services.rds.model.DBInstance
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest
import com.amazonaws.services.rds.model.Filter
import com.atlassian.performance.tools.aws.api.Aws
import com.atlassian.performance.tools.aws.api.ProvisionedStack
import java.util.*

internal fun ProvisionedStack.listRDSInstances(
    aws: Aws
): List<DBInstance> {
    return aws
        .rds
        .describeDBInstances(DescribeDBInstancesRequest().withFilters(
            Filter().withName("db-instance-id").withValues(
                filterResources(this, aws, Collections.singleton("AWS::RDS::DBInstance")).map { it.physicalResourceId }
            )
        ))
        .dbInstances
}

internal fun ProvisionedStack.findDBIPAddress(
    aws: Aws
): String {
    val dbIPAddresses: ArrayList<String> = ArrayList()
    listRDSInstances(aws).map { it.endpoint.address }.forEach { s -> dbIPAddresses.add(s) }
    listMachines().filter { it.tags.contains(Tag("jpt-database", "true")) }.forEach { s -> dbIPAddresses.add(s.publicIpAddress) }

    return dbIPAddresses.single()
}

internal fun ProvisionedStack.findJiraIPAddresses(
    aws: Aws
): List<String> {
    return listMachines().filter { it.tags.contains(Tag("jpt-jira", "true")) }.map { it.publicIpAddress }
}

private fun filterResources(
    provisionedStack: ProvisionedStack, aws: Aws, resourceTypes: Set<String>
): List<StackResourceSummary> {
    return aws
        .cloudformation
        .listStackResources(
            ListStackResourcesRequest().withStackName(provisionedStack.stackName)
        )
        .stackResourceSummaries
        .filter { resourceTypes.contains(it.resourceType) }
        .filter { it.isAlive() }
}

private fun StackResourceSummary.isAlive(): Boolean {
    return ResourceStatus.fromValue(resourceStatus) !in listOf(
        ResourceStatus.DELETE_IN_PROGRESS,
        ResourceStatus.DELETE_COMPLETE
    )
}