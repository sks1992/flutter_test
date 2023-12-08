import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:get/get.dart';
import 'package:test_app_flutter/core/utils/constants.dart';

class LocationController extends GetxController {
  final channel = const MethodChannel(methodChannelName);

  var isLoading = false.obs;

  var locationId = "".obs;
  var latitude = "".obs;
  var longitude = "".obs;
  var time = "".obs;

  @override
  void onInit() async {
    startWorkMangerFromChannel();
    await getLastLocationFromDb();
    super.onInit();
  }

  Future<void> startWorkMangerFromChannel() async {
     await channel.invokeMethod(startWorkManger);
  }
  Future<void> getLastLocationFromDb() async {
    isLoading.value = true;
    var result = await channel.invokeMethod(getLastLocation);
    isLoading.value = false;
    var jsonData = json.decode(result);
    locationId.value = jsonData['id'] as String;
    latitude.value = jsonData['latitude'] as String;
    longitude.value = jsonData['longitude'] as String;
    time.value = jsonData['time'] as String;

    if(locationId.value.isEmpty){
      Future.delayed(const Duration(seconds: 2));
      getLastLocationFromDb();
    }
  }

  Future<void> getCurrentLocation() async {
    isLoading.value = true;
    var result = await channel.invokeMethod(getLocation);
    isLoading.value = false;
    var jsonData = json.decode(result);
    locationId.value = jsonData['id'] as String;
    latitude.value = jsonData['latitude'] as String;
    longitude.value = jsonData['longitude'] as String;
    time.value = jsonData['time'] as String;
    print(time);
  }
}
