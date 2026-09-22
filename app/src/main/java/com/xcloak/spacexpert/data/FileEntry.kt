package com.xcloak.spacexpert.data

import android.net.Uri

data class FileEntry(val path: String, val uri: Uri, val size: Long)
data class DuplicateGroup(val hash: String, val files: List<FileEntry>)