import {Component, OnInit, ViewChild, ElementRef} from '@angular/core';
import {LocalStorageService} from '../../services/local-storage/local-storage.service';
import {ComposeRequest} from '../../dto/compose-request';
import {DataService} from '../../services/data/data.service';
import {MdsService} from '../../services/mds/mds.service';
import { DOCUMENT } from '@angular/common';
import * as jwt_decode from 'jwt-decode';
import { DomSanitizer } from '@angular/platform-browser';

import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ModalComponent } from '../../modal/modal.component';

import {DialogOverviewExampleDialog } from '../../auth/auth.component'

declare const start_streaming: any;
declare const stop_streaming: any;

@Component({
  selector: 'app-run',
  templateUrl: './run.component.html',
  styleUrls: ['./run.component.css']
})
export class RunComponent implements OnInit {

  uin: string;
  testId: string;
  testReportObject1: any;

  run;
  tests = [];
  selectedDevice: any;
  devices = [];
  availablePorts: any;
  currentPort: any;
  requests = [];
  objectKeys = Object.keys;
  testReportObject: any;
  panelOpenState: boolean;
  JSON: any;
  Object: any;
  mdmInitiated = false;
  streamingInitiated = false;
  currentTestId: string;
  loading = false;
  authloading = false;
  readyButton = false;
  busyButton = false;
  notReadyButton = false;
  notRegisteredButton = false;
  statusButton = false;

  constructor(
    private localStorageService: LocalStorageService,
    private dataService: DataService,
    private mdsService: MdsService,
    private _sanitizer: DomSanitizer,
    private dialog: MatDialog
  ) {
    this.JSON = JSON;
    this.Object = Object;
  }

  ngOnInit(): void {
    console.log(history.state.data);
    this.run = history.state.data;
    this.availablePorts = this.localStorageService.getAvailablePortsForDevice();
  
    // this.availablePorts = this.localStorageService.getAvailablePorts();
    //this.fetchReport();
    this.panelOpenState = false;
  }
  
  ngDoCheck(){
    this.availablePorts =[];
    this.availablePorts = this.localStorageService.getAvailablePortsForDevice();
  }
  fetchReport() {
   this.dataService.getTestReport(this.run.runId).subscribe(
      body => {
        this.testReportObject = body;
        console.log(body);
      },
      error => this.openDialog("Alert", error)
    );
  }

  isComplete(row: any) {
    if (this.run.testReport) {
      if (this.run.testReport.hasOwnProperty(row)) {
        return true;
      }
    }
    return false;
  }

  onRunClicked() {
    const composeRequest = new ComposeRequest();
    const deviceDto = {
      port: this.currentPort,
      deviceInfo: this.selectedDevice
    };
    const profileObject = JSON.parse(this.run.profile);
    if (profileObject.purpose != deviceDto.deviceInfo.purpose) {
      this.openDialog("Error", "Selected Device Purpose and Created Test Run Purpose Mismatch");
    }
    else if (profileObject.biometricType != deviceDto.deviceInfo.digitalIdDecoded.type) {
      this.openDialog("Error", "Selected Device Type and Created Test Run BiometricDeviceType Mismatch");
    }
    // else if (profileObject.deviceSubType != deviceDto.deviceInfo.digitalIdDecoded.deviceSubType) {
    //   this.openDialog("Error", "Selected DeviceSubType and Created Test Run DeviceSubType Mismatch");
    // }
    else {
      this.dataService.composeAllRequests(this.run.runId, deviceDto).subscribe(
        body => {
          this.requests = [];
          this.testReportObject = body;
          console.log(body);
        },
        error => this.openDialog("Alert", error)
      );
      console.log("Finished capturing SBI requests");
    }
  }


  getMDSResponse(request, runId, testId, status) {
    this.loading = true;
    this.statusButton = true;
    if (status == 'Ready') {
      this.readyButton = true;
    } else if (status == 'Not Ready') {
      this.notReadyButton = true;
    } else if (status == 'Busy') {
      this.busyButton = true;
    } else if (status == 'Not Registered') {
      this.notRegisteredButton = true;
    }


    let mdmResponse = this.testReportObject.testReport[testId].responseData;
    if (this.mdmInitiated === true) {
      console.log("MDM request is currently going on .....");
    }
    else {
      let self = this;
      console.log("Initiating request to Device >>>> " + testId);
      this.requests.push(testId);
      this.mdmInitiated = true;
      this.stopStreaming(testId);
      this.mdsService.request(JSON.parse(request)).subscribe(
        response => {
          this.validateMDSResponse(runId, testId, request, response, status);
          //this.loading = false;
        },
        error => {
          this.validateMDSResponse(runId, testId, request, error, status);
          //this.loading = false;
        }
      );
      console.log("Finished capturing SBI Responses >>>>> " + testId);
      this.mdmInitiated = false;
    }
  }

  validateMDSResponse(runId, testId, request, response, status) {
    this.dataService.validateResponse(runId, testId, request, response).subscribe(
      result => {
        this.testReportObject = result;
       if (testId == "Device Status") {
       let testObjectValidationResult=this.testReportObject.testReport[testId].validationResults[0].validationTestResultDtos[0];
        testObjectValidationResult.validations[1].expected = status;
          if (testObjectValidationResult.validations[1].found != status) {
            testObjectValidationResult.status = "Failed";
            testObjectValidationResult.validations[1].status="FAILED";
          testObjectValidationResult.validations[1].message='Device info response device status is invalid';
          }

          let testKeyObjectValidationResult=this.testReportObject.testReportKey[3][testId].validationResults[0].validationTestResultDtos[0];
          testKeyObjectValidationResult.validations[1].expected = status;
          if (testKeyObjectValidationResult.validations[1].found != status) {
            this.testReportObject.testReportKey[3][testId].validationResults[0].status = 'Failed';
            testKeyObjectValidationResult.validations[1].status="FAILED";
            testKeyObjectValidationResult.validations[1].message='Device info response device status is invalid';           
          }
        }
        this.loading = false;
        this.readyButton = false;
        this.busyButton = false;
        this.notReadyButton = false;
        this.notRegisteredButton = false;
        this.statusButton = false;
        //console.log('result:' + result);
      },
      error => {
        this.openDialog("Alert", error);
        this.loading = false;
        this.readyButton = false;
        this.busyButton = false;
        this.notReadyButton = false;
        this.notRegisteredButton = false;
        this.statusButton = false;
      }
    );
  }

  getStreamImgTagId(testId) {
    let id = testId.split(' ').join('-');
    return id;
  }

  /* startStreaming(testId) {
     let element = document.getElementById(this.getStreamImgTagId(testId));
     if(element) {
       (<HTMLImageElement>element).setAttribute("src", this.getStreamUrl(testId));
     }
  }

  stopStreaming(testId) {
    let element = document.getElementById(this.getStreamImgTagId(testId));
    if(element) {
      (<HTMLImageElement>element).setAttribute("src", "");
    }
  } */

  /* startStreaming(testId) {
     console.log("startStreaming invoked.... >>> " + testId);
      var self = this;
      var element = document.getElementById("test-id");
      if(element) {
        var mediaSource = new MediaSource();
        var url = URL.createObjectURL(myMediaSource);
        mediaSource.addEventListener('sourceopen', this.sourceOpen);

        self.mdsService.startMDSStream('http://127.0.0.1:4501/stream?deviceId=1&devideSubId=1');

        console.log("after startMDSStream>>>>>>>>>>.");

        myMediaSource.addEventListener('sourceopen', function () {
              console.log(myMediaSource.readyState);
              var sourceBuffer = myMediaSource.addSourceBuffer('video/mp4; codecs="avc1.64001e"');
              console.log(myMediaSource.readyState);

              self.mdsService.messages.subscribe(msg => {
                    console.log("i got a message");
                    sourceBuffer.appendBuffer(msg);
                  });
         });
      }
  }

  sourceOpen() {
      console.log("source open received");
      var mediaSource = this;
      var sourceBuffer = mediaSource.addSourceBuffer('video/mp4; codecs="avc1.42E01E, mp4a.40.2"');
  }


  uint8ToBase64(buffer) {
       var binary = '';
       var len = buffer.byteLength;
       for (var i = 0; i < len; i++) {
           binary += String.fromCharCode(buffer[i]);
       }
       var result = window.btoa( binary );
       console.log("window.btoa result >>>>>>>>>>>>>>>>>>");
       return result;
  }

  stopStreaming(testId) {
    this.mdsStream = "";
    let element = document.getElementById(this.getStreamImgTagId(testId));
    if(element) {
      (<HTMLVideoElement>element).setAttribute("src", "");
    }
  } */


  getStreamUrl(testId) {
    return this.testReportObject.testReport[testId].streamUrl;
  }

  isStreamRequired(testId) {
    return this.testReportObject.testReport[testId].streamUrl ? true : false;
  }

  isCapture(intent) {
    let method = JSON.parse(intent).verb;
    if (method == "CAPTURE") {
      return true;
    }
    return false;
  }
  isRcapture(intent) {
    let method = JSON.parse(intent).verb;
    if (method == "RCAPTURE") {
      return true;
    }
    return false;
  }

  isDeviceStatus(intent, testId) {
    let method = JSON.parse(intent).verb;
    if (method == "MOSIPDINFO" && testId == "Device Status") {
      return true;
    }
    return false;
  }
  /* isMDSResponseCaptured(testId) {
      let mdmCaptured = false;
      for(let i=0; i<this.requests.length;i++) {
        if(this.requests[i] === testId) {
          mdmCaptured = true;
        }
      }
      return mdmCaptured;
  } */

  /* getMDSResponse(request, runId, testId) {
      this.mdsService.request(request.requestInfoDto).subscribe(
          response => {
            console.log(response);
            this.dataService.validateResponse(runId, testId, request, response).subscribe(
              result => {console.log('result:' + result);
                         this.fetchReport();
              },
              error => window.alert(error)
            );
          },
        error => window.alert(error)
      );
    } */

  /* requestMds(request, runId, testId) {
    this.mdsService.request(request.requestInfoDto).subscribe(
        response => {
          console.log(response);
          this.dataService.validateResponse(runId, testId, request, response).subscribe(
            result => {console.log('result:' + result);
                       this.fetchReport();
            },
            error => window.alert(error)
          );
        },
      error => window.alert(error)
    );
  } */

  getButtonName(request) {
    let method = JSON.parse(request).verb;
    switch (method.toUpperCase()) {
      case "MOSIPDISC":
        return "Discover Devices";
      case "MOSIPDINFO":
        return "Get Device Info";
      case "CAPTURE":
        return "Initiate Capture";
      case "RCAPTURE":
        return "Initiate RCapture";
    }
    return "Initiate Request";
  }

  OnPortSelect(value: any) {
    this.currentPort = value;
    this.devices = this.localStorageService.getDevicesByPortNumber(value);
  }

  getPassedValidators(value: any) {
    if (value !== undefined) {
      let total = 0;
      for (let i = 0; i < value.length; i++) {
        if (value[i].status == 'Passed')
          total = total + 1;
      }
      return total + ' out of ' + value.length + ' validations Passed';
    }
    return '0 out of 0 Passed';
  }

  getSanitizedSafeURLResource(data) {
    return this._sanitizer.bypassSecurityTrustHtml(data);
  }

  startStreaming(testId) {
    this.stopStreaming(testId);
    let url = this.getStreamUrl(testId);
    var parts = url.split("?");
    var args = parts[1].split("&");
    start_streaming(parts[0], args[0].replace("deviceId=", ""), args[1].replace("deviceSubId=", ""),
      this.getStreamImgTagId(testId))
  }

  streamingStart(testId, request) {
    this.stopStreaming(testId);
    start_streaming(JSON.parse(request).streamUrl, JSON.parse(JSON.parse(request).body).bio[0].deviceId,
      JSON.parse(JSON.parse(request).body).bio[0].deviceSubId,
      this.getStreamImgTagId(testId))
  }

  stopStreaming(testId) {
    stop_streaming();
  }

  isAuthRequestRequired(testId) {
    return this.testReportObject.testReport[testId].enableAuthTest ? true : false;
  }

  isRequired(testId) {
    if (testId == "Device Status") {
      return false;
    }else{
      return true;
    }
  }
  startAuthTest(testId) {
    this.authloading = true;
    console.log("Starting auth test for >>> " + testId);
    this.dataService.authTestCall(this.testReportObject.testReport[testId].runId, testId).subscribe(
      result => {
        //window.alert(JSON.stringify(result))
        this.openDialog("Auth Response", JSON.stringify(result))
        this.authloading = false
      },
      error => this.openDialog("Auth Response", JSON.stringify(error.error.text))
    );

  }

  openDialog(title: string, message: string): void {
    this.authloading = false
    this.dialog.open(ModalComponent, {
      width: '40%',
      data: { 'title': title, 'message': message }
    });
  }

  startAuthTest1(testId) {
    this.authloading = true;
    let uin=0;
    console.log("Starting auth test for >>> " + testId);
    this.dataService.authTestCallByUin(this.testReportObject.testReport[testId].runId, testId,uin).subscribe(
            result => {
                 //window.alert(JSON.stringify(result))
                 this.openDialog("Auth Response", JSON.stringify(result))
                 this.authloading = false
            },
            error => this.openDialog("Auth Response", JSON.stringify(error.error.text))
    );

}

      openUINDialog(testReportObject1:any,key:any): void {
        this.testReportObject1=testReportObject1;
        this.testId=key;
        console.log(this.testId);
        console.log(this.testReportObject1);
        const dialogRef = this.dialog.open(DialogOverviewExampleDialog, {
          width: '300px',
          data: {uin: this.uin,testReportObject1:this.testReportObject1,testId : this.testId}
        }
        
        );
    
        dialogRef.afterClosed().subscribe(result => {
          console.log('The dialog was closed');
          this.uin = result;
        });
      }

  download(runId, testId) {

    this.loading = true;
    //calling service
    this.dataService.download(runId, testId).subscribe(response => {

      console.log(response);
      var binaryData = [];
      binaryData.push(response.data);
      var url = window.URL.createObjectURL(new Blob(binaryData, { type: "application/pdf" }));
      var a = document.createElement('a');
      document.body.appendChild(a);
      a.setAttribute('style', 'display: none');
      a.setAttribute('target', 'blank');
      a.href = url;
      a.download = response.filename;
      a.click();
      window.URL.revokeObjectURL(url);
      a.remove();
      this.loading = false;
    }, error => {
      this.loading = false;
      window.alert(error)
    });

  }
}
