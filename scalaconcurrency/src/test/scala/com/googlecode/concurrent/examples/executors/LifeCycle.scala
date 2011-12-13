package com.googlecode.concurrent.examples.executors
import com.googlecode.concurrent.ExecutorServiceManager

/**
 * @author kostantinos.kougios
 *
 * 13 Dec 2011
 */
object LifeCycle extends App {

	// An executor will be created. The executor will have a pool of 5 threads.
	// for every list item, it will execute the function using the executor (separate thread).
	// It will collect the results and then shutdown the executor.
	val results = ExecutorServiceManager.lifecycle(5, List(5, 10, 15, 20, 25, 30, 35, 40)) { param =>
		100 + param
	}
	println(results) // List(105, 110, 115, 120, 125, 130, 135, 140)
}