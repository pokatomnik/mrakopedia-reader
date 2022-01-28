package com.example.mrakopediareader.viewpage

class TextZoom(
    initial: Int,
    private val min: Int,
    private val max: Int,
    private val step: Int,
    private val onChange: (zoom: Int) -> Unit
) {
    private val initialZoom: Int = if (initial < min || initial > max) min else initial

    private var zoom: Int = initialZoom

    fun zoomIn() {
        val currentZoom = zoom
        if (currentZoom + step > max) {
            return
        }
        zoom = currentZoom + step
        onChange(zoom)
    }

    fun zoomOut() {
        val currentZoom = zoom
        if (currentZoom - step < min) {
            return
        }
        zoom = currentZoom - step
        onChange(zoom)
    }

    fun reset() {
        zoom = initialZoom
        onChange(zoom)
    }

}