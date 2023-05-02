// Copyright (c) 2023 Created By Dayona All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

import 'dart:convert';

import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';

class ZolozSdk {
  final MethodChannel _methodChannel = const MethodChannel("zoloz_sdk");
  Future<CheckResultResponseModel?> startZoloz({
    @required String? initServer,
    @required String? checkServer,
  }) async {
    try {
      var _res = await _methodChannel.invokeMethod(
          "startZoloz", {"init": initServer, "check": checkServer});
      return CheckResultResponseModel.fromJson(jsonDecode(_res));
    } catch (e) {
      return CheckResultResponseModel(
          result: CheckResultResponseModelResult(
              resultMessage: "Unknown Error",
              resultCode:
                  "${e.toString().substring(e.toString().indexOf("Z"))}"));
    }
  }
}

class CheckResultResponseModelExtInfo {
  String? imageContent;

  CheckResultResponseModelExtInfo({
    this.imageContent,
  });
  CheckResultResponseModelExtInfo.fromJson(Map<String, dynamic> json) {
    imageContent = json['imageContent']?.toString();
  }
  Map<String, dynamic> toJson() {
    final data = <String, dynamic>{};
    data['imageContent'] = imageContent;
    return data;
  }
}

class CheckResultResponseModelResult {
  String? resultStatus;
  String? resultCode;
  String? resultMessage;

  CheckResultResponseModelResult({
    this.resultStatus,
    this.resultCode,
    this.resultMessage,
  });
  CheckResultResponseModelResult.fromJson(Map<String, dynamic> json) {
    resultStatus = json['resultStatus']?.toString();
    resultCode = json['resultCode']?.toString();
    resultMessage = json['resultMessage']?.toString();
  }
  Map<String, dynamic> toJson() {
    final data = <String, dynamic>{};
    data['resultStatus'] = resultStatus;
    data['resultCode'] = resultCode;
    data['resultMessage'] = resultMessage;
    return data;
  }
}

class CheckResultResponseModel {
  CheckResultResponseModelResult? result;
  CheckResultResponseModelExtInfo? extInfo;

  CheckResultResponseModel({
    this.result,
    this.extInfo,
  });
  CheckResultResponseModel.fromJson(Map<String, dynamic> json) {
    result = (json['result'] != null)
        ? CheckResultResponseModelResult.fromJson(json['result'])
        : null;
    extInfo = (json['extInfo'] != null)
        ? CheckResultResponseModelExtInfo.fromJson(json['extInfo'])
        : null;
  }
  Map<String, dynamic> toJson() {
    final data = <String, dynamic>{};
    if (result != null) {
      data['result'] = result!.toJson();
    }
    if (extInfo != null) {
      data['extInfo'] = extInfo!.toJson();
    }
    return data;
  }
}
