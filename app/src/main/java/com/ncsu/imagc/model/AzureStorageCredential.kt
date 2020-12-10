package com.ncsu.imagc.model

import com.microsoft.azure.storage.StorageUri
import java.net.URI

data class AzureStorageCredential(val sasToken: String, val uri: StorageUri)