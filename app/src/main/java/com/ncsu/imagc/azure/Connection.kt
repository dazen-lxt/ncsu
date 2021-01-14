package com.ncsu.imagc.azure

import com.microsoft.azure.storage.StorageUri
import com.ncsu.imagc.model.AzureStorageCredential
import java.net.URI

object Connection {
    val weedsapp = AzureStorageCredential("?sv=2019-12-12&ss=bfqt&srt=sco&sp=rwdlacupx&se=2021-03-04T04:26:50Z&st=2020-12-03T20:26:50Z&spr=https&sig=OSokTwa6AE5O%2FGWEnj9Eptld%2B4KIwKlZom%2BfJfD3J%2F8%3D",
        StorageUri(URI("https://weedsapp.blob.core.windows.net/")))
    val weedsmedia =  AzureStorageCredential("?sv=2019-10-10&ss=bfqt&srt=sco&sp=rwdlacup&se=2021-03-04T08:45:02Z&st=2020-12-04T00:45:02Z&spr=https&sig=O2ud3EFHplFGlU2lPX6AOVVlu8lpYhYImbbs%2Bo6LQ98%3D",
        StorageUri(URI("https://weedsmedia.blob.core.usgovcloudapi.net/")))
    val azureConnectionString = "SharedAccessSignature=sv=2019-12-12&ss=btqf&srt=sco&st=2021-01-08T18%3A18%3A00Z&se=2026-01-10T18%3A18%3A00Z&sp=rwdxlacup&sig=Hx7nKhR3OGMsoJEJ7Ebkn48Spt6Tc8CN2VLMXBU006A%3D;BlobEndpoint=https://weedsmedia.blob.core.usgovcloudapi.net/;FileEndpoint=https://weedsmedia.file.core.usgovcloudapi.net/;QueueEndpoint=https://weedsmedia.queue.core.usgovcloudapi.net/;TableEndpoint=https://weedsmedia.table.core.usgovcloudapi.net/;"
}