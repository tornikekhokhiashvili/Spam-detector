package detector
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

            val spamDetected = AtomicBoolean(false)

            val deferredResults = parts.map { part ->
                async {
                    val isSpam = spamDetector.detectAsync(part).await()
                    if (isSpam) {
                        spamDetected.set(true)
                        coroutineContext.cancelChildren()
                    }
                    isSpam
                }
            }
            val results = deferredResults.awaitAll()

            if (spamDetected.get()) {
//                throw SpamDetectedException()
            }
            results.all { it == false }
        }
    }

    private fun splitTextIntoParts(text: String): List<String> {
        return letterProcessor.splitLetter(text)
    }
}




