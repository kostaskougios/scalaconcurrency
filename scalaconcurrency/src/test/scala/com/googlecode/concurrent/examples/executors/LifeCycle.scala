package com.googlecode.concurrent.examples.executors
import com.googlecode.concurrent.ExecutorServiceManager

/**
 * @author kostantinos.kougios
 *
 * 13 Dec 2011
 */
object LifeCycle extends App {

	// ========================================================================================
	// example 1
	// ========================================================================================
	// An executor will be created. The executor will have a pool of 5 threads.
	// for every list item, it will execute the function using the executor (separate thread).
	// It will collect the results and then shutdown the executor. This is not the most
	// efficient use of executors since the threads are terminated. But it significantly helps
	// to do calculations in parallel and the API is very simple to use.
	val results = ExecutorServiceManager.lifecycle(5, List(5, 10, 15, 20, 25, 30, 35, 40)) { param =>
		100 + param // this should be a relative heavy calculation
	}
	println(results) // List(105, 110, 115, 120, 125, 130, 135, 140)

	// ========================================================================================
	// example 2
	// ========================================================================================
	// create an executor of 5 threads and submit the function 20 times.
	// each time the function is called, i is between 1..20. The function
	// results are appended to a collection
	val results2 = ExecutorServiceManager.lifecycle(5, 20) { i =>
		100 + i // a heavy calculation (not necessarily based on i!)
	}
	println(results2) // Vector(101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120)
}