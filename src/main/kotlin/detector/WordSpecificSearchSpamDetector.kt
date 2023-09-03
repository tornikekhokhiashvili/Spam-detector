package detector

import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Word specific spam detector takes markers that is usually words from spam letter.
 * Default value is 'spam' and 'advertisement'.
 * Detection should be performed asynchronously in the scope provided in constructor.
 * Text is treated as a spam if there is at least one marker presents.
 * In case markers is empty throw IllegalStateException because detector cannot be created without markers.
 */
class WordSpecificSearchSpamDetector(
    private val scope: CoroutineScope = GlobalScope,
    private val spamMarkers: List<String> = listOf("spam", "advertisement")
) : SpamDetector {
    init {
        if (spamMarkers.isEmpty()) {
            throw IllegalStateException("Cannot create WordSpecificSearchSpamDetector without spam markers.")
        }
    }

    /**
     * Performs spam detection. For testing purpose add synthetic delay to processing.
     * Delay should be equal to size of text to process multiplied by 10 (each symbols takes 10 millisecond to process).
     * Detection is case-insensitive, so having marker 'spam' both texts 'Spam' and 'spam' are treated as spam.
     * @param text to process
     * @return deferred true if text contains any spam marker.
     */
    override suspend fun detectAsync(text: String): Deferred<Boolean> {
        return scope.async {
            val spamDetected = AtomicBoolean(false)

            // Calculate the delay based on the text size
            val delay = text.length * DELAY_PER_SYMBOL

            // Simulate asynchronous processing with delay
            delay(delay)

            // Check if any of the spam markers are present in the text (case-insensitive)
            for (marker in spamMarkers) {
                if (text.contains(marker, ignoreCase = true)) {
                    spamDetected.set(true)
                    break // Exit early if a spam marker is found
                }
            }

            spamDetected.get()
        }
    }

    private companion object {
        const val DELAY_PER_SYMBOL = 10L
    }
}