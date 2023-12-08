import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:test_app_flutter/app_route.dart';
import 'package:test_app_flutter/core/bindings/init_binidng.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return GetMaterialApp(
      title: 'Flutter Demo',
      debugShowCheckedModeBanner: false,
      getPages: AppRoute.route,
      initialRoute: RouteName.locationScreen,
      initialBinding: InitBinding(),
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
    );
  }
}
