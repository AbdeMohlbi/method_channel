package com.example.method_channel

import android.os.BatteryManager
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*

class MainActivity : FlutterActivity() {
    private val CHANNEL = "samples.flutter.io/battery"
    private val CHARGING_CHANNEL = "samples.flutter.io/events"

    private var eventSink: EventChannel.EventSink? = null
    private var timerJob: Job? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
            .setMethodCallHandler { call, result ->
                if (call.method == "getBatteryLevel") {
                    val batteryLevel = getBatteryLevel()
                    if (batteryLevel != -1) {
                        result.success(batteryLevel)
                    } else {
                        result.error("UNAVAILABLE", "Battery level not available.", null)
                    }
                } else {
                    result.notImplemented()
                }
            }

        EventChannel(flutterEngine.dartExecutor.binaryMessenger, CHARGING_CHANNEL)
            .setStreamHandler(object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    eventSink = events
                    startEmittingEvents()
                }

                override fun onCancel(arguments: Any?) {
                    stopEmittingEvents()
                    eventSink = null
                }
            })
    }

    private fun getBatteryLevel(): Int {
        val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun startEmittingEvents() {
        // Launch on IO but post results to UI thread using runOnUiThread
        timerJob = CoroutineScope(Dispatchers.IO).launch {
            var counter = 0
            while (isActive) {
                delay(1000)
                counter++
                // MUST call success(...) on the main thread
                runOnUiThread {
                    try {
                        eventSink?.success("Tick #$counter")
                    } catch (t: Throwable) {
                        // defensive: avoid crash if messenger is already closed
                    }
                }
            }
        }
    }

    private fun stopEmittingEvents() {
        timerJob?.cancel()
        timerJob = null
    }
}
