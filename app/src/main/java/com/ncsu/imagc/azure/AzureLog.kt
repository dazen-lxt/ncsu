package com.ncsu.imagc.azure

import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.StorageCredentials
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature
import com.microsoft.azure.storage.table.CloudTable
import com.microsoft.azure.storage.table.CloudTableClient
import com.microsoft.azure.storage.table.TableOperation
import org.jetbrains.anko.doAsync
import java.lang.Exception
import java.util.*


object AzureLog {
    private val storageCredential = CloudStorageAccount.parse(Connection.azureConnectionString)
    private val cloudTableClient = storageCredential.createCloudTableClient()
    var userName: String = ""
    fun saveLog(logType: LogType) {
        doAsync {
            try {
                val cloudTable: CloudTable = cloudTableClient.getTableReference("logs")
                val logEntity = LogEntity("weed", UUID.randomUUID().toString())
                logEntity.userName = userName
                when(logType) {
                    is LogType.Login -> logEntity.type = "Login"
                    is LogType.Error -> {
                        logEntity.type = "Error"
                        logEntity.description = logType.message
                    }
                    is LogType.Info -> {
                        logEntity.type = "Info"
                        logEntity.description = logType.message
                    }
                }
                val insertLog = TableOperation.insertOrReplace(logEntity)
                cloudTable.execute(insertLog)
            } catch (e:Exception) {
                e.printStackTrace()
            }

        }
    }
    sealed class LogType {
        object Login: LogType()
        data class Error(val message: String): LogType()
        data class Info(val message: String): LogType()
    }
}