import { Injectable } from '@angular/core';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LocalStorageService {
  constructor() { }

  clearAllInfos() {
      ;//localStorage.clear();
  }

  clearDeviceInfos() {
    if (localStorage.getItem('deviceInfo')) {
      localStorage.removeItem('deviceInfo');
    }
  }

  addDeviceInfos(port: string, decodedDeviceInfo: any) {
      console.log(decodedDeviceInfo);
      // const devices = {
      //   port: port,
      //   devices: decodedDeviceInfo
      // };
      // const devices = {};
      // devices[port] = decodedDeviceInfo;
      let deviceInfo = {};
      if (!localStorage.getItem('deviceInfo')) {
        localStorage.setItem('deviceInfo', JSON.stringify(deviceInfo));
      }
      deviceInfo = JSON.parse(localStorage.getItem('deviceInfo'));
      deviceInfo[port] = decodedDeviceInfo;
      localStorage.setItem('deviceInfo', JSON.stringify(deviceInfo));
  }

  clearDeviceDiscover() {
    if (localStorage.getItem('discover')) {
      localStorage.removeItem('discover');
    }
  }

  addDeviceDiscover(port: string, deviceDiscover: any) {
    var discoverNew = {port: port + "", deviceDiscoverInfo: JSON.stringify(deviceDiscover)};
    var discoverOld = [];

    if (!localStorage.getItem('discover')) {
      localStorage.setItem('discover', JSON.stringify(discoverOld));
    }
    discoverOld = JSON.parse(localStorage.getItem('discover')); 
    if (discoverOld.length <= 0)
    {
      discoverOld.push (discoverNew); 
    }
    else if (discoverOld.length > 0)
    {
      var found = false;
      for(var index = 0; index < discoverOld.length; index++)
      {
        var discoverInfo = discoverOld[index]; 
        if (parseInt (discoverInfo.port + "") == parseInt (port))
        {
          discoverOld[index] = discoverNew;
          found = true;
          break
        }
      }
      if (!found)
      {
        discoverOld.push(discoverNew);
      }
    }
    localStorage.setItem('discover', JSON.stringify(discoverOld));
  }

  getAvailablePorts() {
    if (!localStorage.getItem('discover')) {
      return [];
    }

    var ports = [];
    var discoverInfo = JSON.parse(localStorage.getItem('discover'));     
    for(var index = 0; index < discoverInfo.length; index++)
    {
      ports.push(discoverInfo[index].port + "");
    }
    return ports.sort(function(a, b){return a - b});
  }

  getAvailablePortsForDevice() {
    if (!localStorage.getItem('discover')) {
      return [];
    }

    let deviceDiscoverInfo = [];
    var ports = [];
    var discoverInfo = JSON.parse(localStorage.getItem('discover'));     
    for(var index = 0; index < discoverInfo.length; index++)
    {
      deviceDiscoverInfo = JSON.parse(discoverInfo[index].deviceDiscoverInfo);
      var noError = false;
      if (deviceDiscoverInfo)
      {
        for(var dindex = 0; dindex < deviceDiscoverInfo.length; dindex++)
        {
          var discoverObj = JSON.parse(JSON.stringify(deviceDiscoverInfo[dindex]));
          //console.log("getAvailablePortsForDevice>>" + JSON.stringify(discoverObj));

          var errorObj = discoverObj.error;
          if (parseInt (errorObj.errorCode) == 100)
          {
            noError = true;
          }
          else
          {
            noError = false;
          }
        }
        if (noError)
        {
          ports.push(discoverInfo[index].port + "");
        }
      }    
    }
    return ports.sort(function(a, b){return a - b});
  }

  getDevicesByPortNumber(port: string) {
    if (!localStorage.getItem('discover')) {
      return [];
    }

    let deviceDiscoverInfo = [];
    let deviceDiscoverInfoNew = [];
    var discoverInfo = JSON.parse(localStorage.getItem('discover'));     
    for(var index = 0; index < discoverInfo.length; index++)
    {
      if (parseInt (discoverInfo[index].port + "") == parseInt (port))
      {
        deviceDiscoverInfo = JSON.parse(discoverInfo[index].deviceDiscoverInfo);
        if (deviceDiscoverInfo)
        {
          for(var dindex = 0; dindex < deviceDiscoverInfo.length; dindex++)
          {
            var discoverObj = JSON.parse(JSON.stringify(deviceDiscoverInfo[dindex]));
            var digitalIdDisObj = JSON.parse(atob (discoverObj.digitalId));
            //created new obj to add digitalIdDis: digitalIdDisObj for display purpose only
            var discoverObjNew = {deviceId:discoverObj.deviceId, purpose:discoverObj.purpose, deviceSubId:discoverObj.deviceSubId, digitalId: discoverObj.digitalId, digitalIdDis: digitalIdDisObj, deviceStatus:discoverObj.deviceStatus, deviceCode:discoverObj.deviceCode, error:discoverObj.error, certification:discoverObj.certification, specVersion: discoverObj.specVersion, callbackId: discoverObj.callbackId, serviceVersion:discoverObj.serviceVersion};
            deviceDiscoverInfo[dindex] = discoverObjNew;
          }          
        }
        break;
      }
    }
    //console.log("getDevicesByPortNumber>>" + JSON.stringify(deviceDiscoverInfo));
    return JSON.parse(JSON.stringify(deviceDiscoverInfo));
  }
}
