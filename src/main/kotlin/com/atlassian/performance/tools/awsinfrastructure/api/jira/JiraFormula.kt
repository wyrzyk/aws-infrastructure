package com.atlassian.performance.tools.awsinfrastructure.api.jira

import com.atlassian.performance.tools.aws.api.*
import com.atlassian.performance.tools.awsinfrastructure.api.database.OracleDatabase
import com.atlassian.performance.tools.awsinfrastructure.api.database.RdsDatabase
import com.atlassian.performance.tools.infrastructure.api.database.Database
import com.atlassian.performance.tools.infrastructure.api.database.MySqlDatabase
import java.lang.IllegalArgumentException
import java.util.concurrent.Future

interface JiraFormula {
    fun provision(
        investment: Investment,
        pluginsTransport: Storage,
        resultsTransport: Storage,
        key: Future<SshKey>,
        roleProfile: String,
        aws: Aws
    ): ProvisionedJira

    fun getCloudFormationTemplateForDatabase(database: Database): String {
        if (RdsDatabase::class.isInstance(database)) {
            return "rds.yaml"
        } else {
            return "mysql-ec2.yaml"
        }
    }
}

class ProvisionedJira(
    val jira: Jira,
    val resource: Resource
) {
    override fun toString(): String {
        return "ProvisionedJira(jira=$jira, resource=$resource)"
    }
}

