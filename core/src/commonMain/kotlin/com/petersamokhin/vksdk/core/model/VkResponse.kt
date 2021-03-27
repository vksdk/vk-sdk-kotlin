package com.petersamokhin.vksdk.core.model

import kotlinx.serialization.*
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Base VK response
 *
 * @property response Result of an API call, if [error] is null
 * @property error Some error, if [response] is null
 */
@Serializable
public data class VkResponse<out T : Any>(
    val response: T? = null,
    val error: VkResponseError? = null
)

/**
 * Class with generics requires this awful manual serializer.
 *
 * @property responseSerializer Serializer for [T]::class
 */
@ExperimentalSerializationApi
@Serializer(forClass = VkResponse::class)
public class VkResponseTypedSerializer<T: Any>(private val responseSerializer: KSerializer<T>) : KSerializer<VkResponse<T>> {
    private val errorSerializer: KSerializer<VkResponseError?> = VkResponseError.serializer().nullable

    /**
     * Describes the structure of the serializable representation of [T], produced
     * by this serializer.
     */
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("VkResponseTypedSerializer") {
        val dataDescriptor = responseSerializer.descriptor.nullable
        element("response", dataDescriptor)
        element("error", errorSerializer.descriptor.nullable)
    }

    /**
     * Serializes the value of type [T] using the format that is represented by the given encoder.
     *
     * @param encoder Encoder
     * @param value Original value
     */
    override fun serialize(encoder: Encoder, value: VkResponse<T>) {
        encoder.beginStructure(descriptor).apply {
            encodeNullableSerializableElement(descriptor, 0, responseSerializer, value.response)
            encodeNullableSerializableElement(descriptor, 1, errorSerializer, value.error)
            endStructure(descriptor)
        }
    }

    /**
     * Deserializes the value of type [T] using the format that is represented by the given decoder.
     *
     * @param decoder Decoder
     * @return Deserialized value
     */
    override fun deserialize(decoder: Decoder): VkResponse<T> {
        val inp = decoder.beginStructure(descriptor)
        var response: T? = null
        var error: VkResponseError? = null
        loop@ while (true) {
            when (val i = inp.decodeElementIndex(descriptor)) {
                CompositeDecoder.DECODE_DONE -> break@loop
                0 -> response = inp.decodeNullableSerializableElement(descriptor, i, responseSerializer.nullable)
                1 -> error = inp.decodeNullableSerializableElement(descriptor, i, errorSerializer)
                else -> throw SerializationException("Unknown index $i")
            }
        }
        inp.endStructure(descriptor)
        return VkResponse(response, error)
    }
}