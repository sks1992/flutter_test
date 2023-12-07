import 'package:get/get.dart';

import 'ui/location_screen.dart';

class RouteName {
  static String locationScreen = "/location_screen";
  static String splashScreen = "/splash_screen";
}

class AppRoute {
  static final route = [
    GetPage(name: RouteName.locationScreen, page: () => LocationScreen()),
  ];
}
