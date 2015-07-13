package com.pagerduty.eris

import com.google.common.util.concurrent.{ListenableFuture, MoreExecutors}

import scala.concurrent.{Promise, Future}
import scala.util.control.NonFatal


/**
 * Provides a convenient conversion between Futures used in Astyanax and Scala futures.
 */
object FutureConversions {

  /**
   * Easy conversion between Astyanax and Twitter futures.
   */
  implicit def googleListenableToScalaFuture[T](listenable: ListenableFuture[T]): Future[T] = {
    val promise = Promise[T]()

    listenable.addListener(
      new Runnable() { def run(): Unit = {
        try { promise.success(listenable.get) }
        catch { case NonFatal(e) => promise.failure(e) }
      }},
      MoreExecutors.directExecutor())

    promise.future
  }
}
