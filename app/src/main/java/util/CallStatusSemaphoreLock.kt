package util

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Semaphore


class CallStatusSemaphoreLock {
    private var uniquieId: String? = null
    private val queue: ConcurrentLinkedQueue<Any> = ConcurrentLinkedQueue()
    private val lock: Semaphore = Semaphore(1)

}