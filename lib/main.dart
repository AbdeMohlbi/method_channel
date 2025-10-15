import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(home: HomePage());
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  static const batteryChannel = MethodChannel('samples.flutter.io/battery');
  static const eventChannel = EventChannel('samples.flutter.io/events');

  String batteryLevel = 'Unknown';
  String tick = 'No events yet';

  @override
  void initState() {
    super.initState();

    eventChannel.receiveBroadcastStream().listen(
      (event) {
        setState(() => tick = event.toString());
      },
      onError: (error) {
        setState(() => tick = 'Error: $error');
      },
    );
  }

  Future<void> getBatteryLevel() async {
    try {
      final level = await batteryChannel.invokeMethod<int>('getBatteryLevel');
      setState(() => batteryLevel = 'Battery: $level%');
    } catch (e) {
      setState(() => batteryLevel = 'Failed to get battery: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Method + Event Channel Demo')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(batteryLevel),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: getBatteryLevel,
              child: const Text('Get Battery Level'),
            ),
            const SizedBox(height: 40),
            Text(tick, style: const TextStyle(fontSize: 18)),
          ],
        ),
      ),
    );
  }
}
