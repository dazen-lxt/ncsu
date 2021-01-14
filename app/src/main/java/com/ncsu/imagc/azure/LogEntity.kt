package com.ncsu.imagc.azure

import com.microsoft.azure.storage.table.TableServiceEntity

class LogEntity(partitionKey: String, rowKey: String): TableServiceEntity(partitionKey, rowKey) {
    var userName: String = ""
    var type: String = ""
    var description: String = ""
}