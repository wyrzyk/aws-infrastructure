package com.atlassian.performance.tools.awsinfrastructure.api.database

import com.amazonaws.services.ec2.model.Instance
import com.amazonaws.services.ec2.model.Tag
import com.atlassian.performance.tools.aws.api.Aws
import com.atlassian.performance.tools.aws.api.ProvisionedStack
import com.atlassian.performance.tools.aws.api.SshKey
import com.atlassian.performance.tools.awsinfrastructure.api.RemoteLocation
import com.atlassian.performance.tools.awsinfrastructure.api.database.DatabaseFormula.ProvisionedDatabase
import com.atlassian.performance.tools.awsinfrastructure.findDBIPAddress
import com.atlassian.performance.tools.concurrency.api.submitWithLogContext
import com.atlassian.performance.tools.infrastructure.api.database.Database
import com.atlassian.performance.tools.infrastructure.api.jira.JiraHomeSource
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshHost
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.URI

class DockerDatabaseFormula(
        override val database: Database
) : DatabaseFormula {

    private val logger: Logger = LogManager.getLogger(this::class.java)

    override fun provision(key: SshKey, jiraURI: URI, provisionedStack: ProvisionedStack, aws: Aws): ProvisionedDatabase {

        val databaseIp = provisionedStack.findDBIPAddress(aws)
        val databaseHost = SshHost(databaseIp, "ubuntu", key.file.path)
        val databaseSsh = Ssh(databaseHost, connectivityPatience = 4)

        val setupDatabase = databaseSsh.newConnection().use {
            logger.info("Setting up database...")
            key.file.facilitateSsh(databaseIp)
            val location = database.setup(it)
            logger.info("Database is set up")
            logger.info("Starting database...")
            database.start(jiraURI, it)
            logger.info("Database is started")
            RemoteLocation(databaseHost, location)
        }

        return ProvisionedDatabase(RemoteLocation(databaseHost, setupDatabase.location))
    }

}