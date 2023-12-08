import 'package:get/get.dart';
import 'package:test_app_flutter/core/controller/location_controller.dart';


class InitBinding extends Bindings {
  @override
  void dependencies() {
    Get.lazyPut(() => LocationController());
  }

}