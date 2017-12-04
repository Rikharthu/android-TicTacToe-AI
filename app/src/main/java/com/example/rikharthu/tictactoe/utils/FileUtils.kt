package com.example.rikharthu.tictactoe.utils

import android.content.Context
import android.text.TextUtils
import junit.framework.Assert
import java.io.IOException


fun getAllFilesInAssetByExtension(context: Context, path: String, extension: String): Array<String>? {
    Assert.assertNotNull(context)

    try {
        val files = context.assets.list(path)

        if (TextUtils.isEmpty(extension)) {
            return files
        }

        val filesWithExtension = ArrayList<String>()

        for (file in files) {
            if (file.endsWith(extension)) {
                filesWithExtension.add(file)
            }
        }

        return filesWithExtension.toTypedArray()
    } catch (e: IOException) {
        // TODO Auto-generated catch block
        e.printStackTrace()
    }

    return null
}