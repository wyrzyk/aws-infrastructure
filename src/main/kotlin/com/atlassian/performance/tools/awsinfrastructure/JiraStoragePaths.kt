package com.atlassian.performance.tools.awsinfrastructure

/**
 * Paths used inside the Jira Storage Bucket, which is used to provision Jira nodes
 */
internal object JiraStoragePaths {
    const val APPS = "installed-plugins"
    const val COLLECTD_CONFIGS = "collectd.conf.d"
    const val COLLECTD_JARS = "collectdjars"
}