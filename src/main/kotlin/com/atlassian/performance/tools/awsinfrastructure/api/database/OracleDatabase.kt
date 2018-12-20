package com.atlassian.performance.tools.awsinfrastructure.api.database

import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI

class OracleDatabase(
        override val rdsDBSnapshotIdentifier: String
): RdsDatabase() {

    override fun setup(ssh: SshConnection): String = ""

    override fun start(jira: URI, ssh: SshConnection) {

    }

}