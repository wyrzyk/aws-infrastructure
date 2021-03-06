package com.atlassian.performance.tools.awsinfrastructure

import com.atlassian.performance.tools.aws.api.StorageLocation
import com.atlassian.performance.tools.infrastructure.api.dataset.DatasetPackage
import com.atlassian.performance.tools.infrastructure.api.dataset.FileArchiver
import com.atlassian.performance.tools.jvmtasks.api.TaskTimer.time
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration

internal data class S3DatasetPackage(
    private val artifactName: String,
    private val location: StorageLocation,
    private val unpackedPath: String? = null,
    private val downloadTimeout: Duration
) : DatasetPackage {

    private val stdOut = "-"

    override fun download(
        ssh: SshConnection
    ): String {
        val unzipCommand = FileArchiver().pipeUnzip(ssh)
        val downloadFileCommand = AwsCli().downloadFileCommand(location, ssh, artifactName, stdOut)

        time("download") {
            ssh.execute("$downloadFileCommand | $unzipCommand", downloadTimeout)
        }

        return unpackedPath!!
    }
}