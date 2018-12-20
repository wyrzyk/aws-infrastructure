package com.atlassian.performance.tools.awsinfrastructure.api.database

import com.atlassian.performance.tools.aws.api.Aws
import com.atlassian.performance.tools.aws.api.ProvisionedStack
import com.atlassian.performance.tools.aws.api.SshKey
import com.atlassian.performance.tools.awsinfrastructure.api.RemoteLocation
import com.atlassian.performance.tools.infrastructure.api.database.Database
import com.atlassian.performance.tools.infrastructure.api.jira.JiraHomeSource
import java.net.URI

interface DatabaseFormula {
    val database: Database

    fun provision(key: SshKey, jiraURI: URI, provisionedStack: ProvisionedStack, aws: Aws): ProvisionedDatabase

    class ProvisionedDatabase(val remoteDatabaseDataLocation: RemoteLocation?) {

    }
}