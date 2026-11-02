package com.example

import com.squareup.moshi.JsonReader
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun `test moshi json reader gracefully consumes null fields without leaving unconsumed tokens`() {
        val json = """
            [
                {
                    "stream_id": 101,
                    "name": "Channel With Null EPG",
                    "epg_channel_id": null,
                    "category_id": "1",
                    "stream_icon": null,
                    "rating": null,
                    "tv_archive": null,
                    "container_extension": null
                },
                {
                    "stream_id": 102,
                    "name": "Next Channel",
                    "epg_channel_id": "valid_epg",
                    "category_id": "1"
                }
            ]
        """.trimIndent()

        val reader = JsonReader.of(Buffer().writeUtf8(json))
        reader.beginArray()

        var itemsRead = 0
        while (reader.hasNext()) {
            if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                reader.beginObject()
                var streamId = 0L
                var name = ""
                var epg = ""
                while (reader.hasNext()) {
                    val field = reader.nextName()
                    when (field) {
                        "stream_id" -> {
                            streamId = when (reader.peek()) {
                                JsonReader.Token.NUMBER -> reader.nextLong()
                                JsonReader.Token.NULL -> { reader.nextNull<Unit>(); 0L }
                                else -> { reader.skipValue(); 0L }
                            }
                        }
                        "name" -> {
                            name = when (reader.peek()) {
                                JsonReader.Token.STRING -> reader.nextString()
                                JsonReader.Token.NULL -> { reader.nextNull<Unit>(); "" }
                                else -> { reader.skipValue(); "" }
                            }
                        }
                        "epg_channel_id" -> {
                            epg = when (reader.peek()) {
                                JsonReader.Token.STRING -> reader.nextString()
                                JsonReader.Token.NULL -> { reader.nextNull<Unit>(); "" }
                                else -> { reader.skipValue(); "" }
                            }
                        }
                        else -> {
                            when (reader.peek()) {
                                JsonReader.Token.NULL -> reader.nextNull<Unit>()
                                else -> reader.skipValue()
                            }
                        }
                    }
                }
                reader.endObject()
                itemsRead++
                if (itemsRead == 1) {
                    assertEquals(101L, streamId)
                    assertEquals("Channel With Null EPG", name)
                    assertEquals("", epg)
                } else if (itemsRead == 2) {
                    assertEquals(102L, streamId)
                    assertEquals("Next Channel", name)
                    assertEquals("valid_epg", epg)
                }
            } else {
                reader.skipValue()
            }
        }
        reader.endArray()

        assertEquals(2, itemsRead)
    }
}
