package com.petersamokhin.vksdk.core.callback

/**
 * Callback for network calls
 */
interface Callback<in R: Any> {
    /**
     * Called when the network call is successful
     *
     * @param result Result of a call
     */
    fun onResult(result: R)

    /**
     * Called when the network call failed
     *
     * @param error Some error
     */
    fun onError(error: Exception)
}