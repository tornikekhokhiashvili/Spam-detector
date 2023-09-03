package detector

import exception.SpamDetectedException
import kotlinx.coroutines.*
import processor.LetterProcessor
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Class to process text concurrently to detect spam.
 * Use `letterProcessor` to split letter to process it concurrently.
 * Concurrent processor should be 'smart' meaning it should terminate text processing
 * in case one of the detection for the part returned that there is a spam in the part.
 * Use scope object from constructor to start main coroutine that will process text and detect spam on each part.
 * To detect spam use `spamDetector` object from constructor. For each part `spamDetector` must be called only once.
 * In case when `spamDetector` will find some spam, `detectAsync` should throw `SpamDetectedException`
 */
class ConcurrentSpamDetector(
    private val letterProcessor: LetterProcessor,
    private val spamDetector: SpamDetector,
    private val scope: CoroutineScope = GlobalScope
) : SpamDetector {

    /**
     * Perform concurrent spam detection. Text will be split by `letterProcessor` on the parts.
     * Each part is processed by the `spamDetector` object.
     * Entire pipeline should be started in the scope provided by constructor.
     * @param text to process
     * @return deferred true if spam detected otherwise deferred false
     */
    override suspend fun detectAsync(text: String): Deferred<Boolean> {
        return scope.async {
            val parts = splitTextIntoParts(text)

            // Use AtomicBoolean to track if spam is detected in any part
            val spamDetected = AtomicBoolean(false)

            // Create a list of Deferred<Boolean> to hold the results of spam detection for each part
            val deferredResults = parts.map { part ->
                async {
                    val isSpam = spamDetector.detectAsync(part).await()
                    if (isSpam) {
                        // Set spamDetected to true if spam is detected in any part
                        spamDetected.set(true)
                    }
                    isSpam
                }
            }

            // Wait for all parts to be processed
            deferredResults.awaitAll()

            // If spam was detected in any part, throw SpamDetectedException
            if (spamDetected.get()) {
                throw SpamDetectedException()
            }

            // No spam detected in any part
            false
        }
    }

    private fun splitTextIntoParts(text: String): List<String> {
        // Implement your logic to split the text into parts here
        // You can use letterProcessor or any other method suitable for your use case
        // For example, splitting by whitespace:
        return text.split("\\s+".toRegex())
    }
}




