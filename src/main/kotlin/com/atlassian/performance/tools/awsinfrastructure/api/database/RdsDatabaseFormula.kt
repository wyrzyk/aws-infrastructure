package com.atlassian.performance.tools.awsinfrastructure.api.database

import com.amazonaws.services.ec2.model.SecurityGroup
import com.amazonaws.services.ec2.model.Tag
import com.amazonaws.services.ec2.model.Vpc
import com.amazonaws.services.rds.model.DBInstance
import com.amazonaws.services.rds.model.DBSubnetGroup
import com.amazonaws.services.rds.model.ModifyDBInstanceRequest
import com.atlassian.performance.tools.aws.api.Aws
import com.atlassian.performance.tools.aws.api.ProvisionedStack
import com.atlassian.performance.tools.aws.api.SshKey
import com.atlassian.performance.tools.awsinfrastructure.listRDSInstances
import com.atlassian.performance.tools.infrastructure.api.jira.JiraHomeSource
import java.net.URI

class RdsDatabaseFormula(
        override val database: RdsDatabase,
        private val jiraHomeSource: JiraHomeSource
): DatabaseFormula {
    override fun provision(key: SshKey, jiraURI: URI, provisionedStack: ProvisionedStack, aws: Aws): DatabaseFormula.ProvisionedDatabase {
        val dbInstance : DBInstance = provisionedStack.listRDSInstances(aws).single()

        val stackNameTag: Tag = Tag("aws:cloudformation:stack-name", provisionedStack.stackName)
        val logicalIdTag: Tag = Tag("aws:cloudformation:logical-id", "DatabaseSecurityGroup")

        val securityGroup: SecurityGroup = aws.ec2
            .describeSecurityGroups().securityGroups
            .single { it.tags != null && it.tags.contains(stackNameTag) && it.tags.contains(logicalIdTag) }

        val vpc: Vpc = aws.ec2
            .describeVpcs().vpcs
            .single { it.tags != null && it.tags.contains(stackNameTag) }

        val subnetGroup: DBSubnetGroup = aws.rds
            .describeDBSubnetGroups().dbSubnetGroups
            .single { it.vpcId.equals(vpc.vpcId) }

        aws.rds.modifyDBInstance(
            ModifyDBInstanceRequest(dbInstance.dbInstanceIdentifier)
                .withDBSubnetGroupName(subnetGroup.dbSubnetGroupName)
                .withVpcSecurityGroupIds(securityGroup.groupId)
                .withApplyImmediately(true))

        var modifiedDbInstance : DBInstance = provisionedStack.listRDSInstances(aws).single()
        while(!modifiedDbInstance.dbInstanceStatus.equals("available")
            || !(modifiedDbInstance.vpcSecurityGroups.toList().size == 1 && modifiedDbInstance.vpcSecurityGroups[0].vpcSecurityGroupId.equals(securityGroup.groupId))) {
            Thread.sleep(5000)
            modifiedDbInstance = provisionedStack.listRDSInstances(aws).single()
        }

        return DatabaseFormula.ProvisionedDatabase(null)
    }
}