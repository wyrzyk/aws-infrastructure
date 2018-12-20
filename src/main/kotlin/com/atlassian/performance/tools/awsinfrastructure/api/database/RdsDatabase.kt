package com.atlassian.performance.tools.awsinfrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.database.Database

abstract class RdsDatabase(): Database {
    internal abstract val rdsDBSnapshotIdentifier: String
}