package com.app.gallery.data.extension

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


fun ContentResolver.queryFlow(
    uri: Uri,
    projection: Array<String>? = null,
    queryArgs: Bundle? = Bundle(),
) = callbackFlow {

    launch(Dispatchers.IO) {
        runCatching {
            trySend(
                query(uri, projection, queryArgs, null)
            )
        }
    }

    awaitClose {
    }
}.conflate()

fun <T> Flow<Cursor?>.mapEachRow(
    projection: Array<String>,
    mapping: (Cursor, Array<Int>) -> T,
) = map {
    it.mapEachRow(projection, mapping)
}

fun <T> Cursor?.mapEachRow(
    projection: Array<String>,
    mapping: (Cursor, Array<Int>) -> T,
) = this?.use { cursor ->
    if (!cursor.moveToFirst()) {
        return@use emptyList<T>()
    }

    val indexCache = projection.map { column ->
        cursor.getColumnIndexOrThrow(column)
    }.toTypedArray()

    val data = mutableListOf<T>()
    do {
        data.add(mapping(cursor, indexCache))
    } while (cursor.moveToNext())

    data.toList()
} ?: emptyList()
