import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { LocalStorageService } from '../../services/local-storage/local-storage.service';
import { ComposeRequest } from '../../dto/compose-request';
import { DataService } from '../../services/data/data.service';
import { MdsService } from '../../services/mds/mds.service';
import { DOCUMENT } from '@angular/common';
import * as jwt_decode from 'jwt-decode';
import { DomSanitizer } from '@angular/platform-browser';

import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ModalComponent } from '../../modal/modal.component';

import { DialogOverviewExampleDialog } from '../../auth/auth.component';
import { DialogOverviewCaptureDialog } from '../../capture/capture.component'


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
  timeOut:any;
  requestScore:any;
  previousHash:any;
  keyRotatOrderIdList = [3011, 3012, 3013];

  keyRotatOrderIdListRcapture = [3014];
  successErrorCodeOfAuth = "IDA-BIA-001";
  succesAuth = "authStatus=true";
  failErrorCodeOfAuth = "IDA-MPA-002";
  validationResult: any;



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
  validCertButton = false;
  expiredCertButton = false;
  afterKeyRotationButton = false;


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

  ngDoCheck() {
    this.availablePorts = [];
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
   this.statusButton = true;
    if (status == 'Ready') {
      this.readyButton = true;
    } else if (status == 'Not Ready') {
      this.notReadyButton = true;
    } else if (status == 'Busy') {
      this.busyButton = true;
    } else if (status == 'Not Registered') {
      this.notRegisteredButton = true;
    } else if (status == 'validCertButton') {
      this.validCertButton = true;
    } else if (status == 'expiredCertButton') {
      this.expiredCertButton = true;
    } else if (status == 'afterKeyRotationButton') {
      this.afterKeyRotationButton = true;
    }


    let mdmResponse = this.testReportObject.testReport[testId].responseData;
    let method = JSON.parse(request).verb;
   
    if((method === "CAPTURE" || method==="RCAPTURE") && status == null)
    this.openCaptureDataDialog(this.testReportObject,testId,request);
    else
    this.otherInitate(testId, request, runId, status);
  }

  private otherInitate(testId: any, request: any, runId: any, status: any) {
    this.loading = true;
    
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
          let testObjectValidationResult = this.testReportObject.testReport[testId].validationResults[0].validationTestResultDtos[0];
          testObjectValidationResult.validations[1].expected = status;
          if (testObjectValidationResult.validations[1].found != status) {
            this.testReportObject.testReport[testId].validationResults[0].status = "Failed";
            testObjectValidationResult.validations[1].status = "FAILED";
            testObjectValidationResult.validations[1].message = 'Device info response device status is invalid';
          }

          let testKeyObjectValidationResult = this.testReportObject.testReportKey[3][testId].validationResults[0].validationTestResultDtos[0];
          testKeyObjectValidationResult.validations[1].expected = status;
          if (testKeyObjectValidationResult.validations[1].found != status) {
            this.testReportObject.testReportKey[3][testId].validationResults[0].status = 'Failed';
            testKeyObjectValidationResult.validations[1].status = "FAILED";
            testKeyObjectValidationResult.validations[1].message = 'Device info response device status is invalid';
          }
        }

        if (testId.includes("Key Rotation")) {
          for (let i = 0; i < this.keyRotatOrderIdList.length; i++) {
            if (this.testReportObject.testReportKey[this.keyRotatOrderIdList[i]] != null) {
              // let testKeyObjectValidationResult=this.testReportObject.testReportKey[this.keyRotatOrderIdList[i]][testId].validationResults[0].validationTestResultDtos[0];
              console.log("not null");
              this.validationResult = this.testReportObject.testReportKey[this.keyRotatOrderIdList[i]][testId].validationResults[0].validationTestResultDtos[0].validations[0];
              var reqData = JSON.parse(this.testReportObject.testReportKey[this.keyRotatOrderIdList[i]][testId].requestData)
              if (reqData.verb == 'CAPTURE') {

                let authResponse = this.validationResult.found;
                if (status == "validCertButton" || status == "afterKeyRotationButton") {
                  if (!(authResponse.includes(this.successErrorCodeOfAuth) ||
                    authResponse.includes(this.succesAuth))) {
                    this.validationResult.message = "Unexpected result returned";
                    this.validationResult.status = "FAILED";
                    this.testReportObject.testReportKey[this.keyRotatOrderIdList[i]][testId].validationResults[0].status = "Failed";
                  }
                } else if (status == "expiredCertButton") {
                  if (!(authResponse.includes(this.failErrorCodeOfAuth))) {
                    this.validationResult.message = "Unexpected result returned";
                    this.validationResult.status = "FAILED";
                    this.testReportObject.testReportKey[this.keyRotatOrderIdList[i]][testId].validationResults[0].status = "Failed";
                  }
                }
                this.testReportObject.testReportKey[this.keyRotatOrderIdList[i]][testId].validationResults[0].validationTestResultDtos[0].validations[0] = this.validationResult;
              }

            }
            // let testKeyObjectValidationResult=this.testReportObject.testReportKey[this.keyRotatOrderIdList[i]][testId].validationResults[0].validationTestResultDtos[0];
            this.validationResult = this.testReportObject.testReport[testId].validationResults[0].validationTestResultDtos[0].validations[0];
            var reqData = JSON.parse(this.testReportObject.testReport[testId].requestData);
            if (reqData.verb == 'CAPTURE') {

              let authResponse = this.validationResult.found;

              if (status == "validCertButton" || status == "afterKeyRotationButton") {
                if (!(authResponse.includes(this.successErrorCodeOfAuth) ||
                  authResponse.includes(this.succesAuth))) {
                  this.validationResult.message = "Unexpected result returned";
                  this.validationResult.status = "FAILED";
                  this.testReportObject.testReport[testId].validationResults[0].status = "Failed";
                }
              } else if (status == "expiredCertButton") {
                if (!(authResponse.includes(this.failErrorCodeOfAuth))) {
                  this.validationResult.message = "Unexpected result returned";
                  this.validationResult.status = "FAILED";
                  this.testReportObject.testReport[testId].validationResults[0].status = "Failed";
                }
              }

              this.testReportObject.testReport[testId].validationResults[0].validationTestResultDtos[0].validations[0] = this.validationResult;
            }

          }
          for (let i = 0; i < this.keyRotatOrderIdListRcapture.length; i++) {
            if (this.testReportObject.testReportKey[this.keyRotatOrderIdListRcapture[i]] != null) {
              // let testKeyObjectValidationResult=this.testReportObject.testReportKey[this.keyRotatOrderIdList[i]][testId].validationResults[0].validationTestResultDtos[0];
              console.log("not null");
              this.validationResult = this.testReportObject.testReportKey[this.keyRotatOrderIdListRcapture[i]][testId].validationResults[0].validationTestResultDtos[0].validations[0];
              var reqData = JSON.parse(this.testReportObject.testReportKey[this.keyRotatOrderIdListRcapture[i]][testId].requestData)

              if (reqData.verb == 'RCAPTURE') {
                if (status == "expiredCertButton") {
                  if (this.validationResult.status == "FAILED") {
                    this.validationResult.status = "SUCCESS";
                    this.testReportObject.testReportKey[this.keyRotatOrderIdListRcapture[i]][testId].validationResults[0].status = "Passed";

                  } else if (this.validationResult.status == "SUCCESS") {
                    this.validationResult.status = "FAILED";
                    this.testReportObject.testReportKey[this.keyRotatOrderIdListRcapture[i]][testId].validationResults[0].status = "Failed";

                  }
                }
                this.testReportObject.testReportKey[this.keyRotatOrderIdListRcapture[i]][testId].validationResults[0].validationTestResultDtos[0].validations[0] = this.validationResult;
                this.validationResult = this.testReportObject.testReport[testId].validationResults[0].validationTestResultDtos[0].validations[0];
                var reqData = JSON.parse(this.testReportObject.testReport[testId].requestData);
                if (reqData.verb == 'RCAPTURE') {
                  if (status == "expiredCertButton") {
                    if (this.validationResult.status == "FAILED") {
                      this.validationResult.status = "SUCCESS";
                      this.testReportObject.testReport[testId].validationResults[0].status = "Passed";

                    } else if (this.validationResult.status == "SUCCESS") {
                      this.validationResult.status = "FAILED";
                      this.testReportObject.testReport[testId].validationResults[0].status = "Failed";

                    }
                  }
                  this.testReportObject.testReport[testId].validationResults[0].validationTestResultDtos[0].validations[0] = this.validationResult;

                }
              }
            }
          }
        }
        this.loading = false;
        this.statusButton = false;
        this.readyButton = false;
        this.busyButton = false;
        this.notReadyButton = false;
        this.notRegisteredButton = false;
        this.statusButton = false;
        this.validCertButton = false;
        this.expiredCertButton = false;
        this.afterKeyRotationButton = false;
        //console.log('result:' + result);
      },
      error => {
        this.openDialog("Alert", error);
        this.loading = false;
        this.statusButton = false;
        this.readyButton = false;
        this.busyButton = false;
        this.notReadyButton = false;
        this.notRegisteredButton = false;
        this.statusButton = false;
        this.validCertButton = false;
        this.expiredCertButton = false;
        this.afterKeyRotationButton = false;
      }
    );
  }

  getStreamImgTagId(testId) {
    let id = testId.split(' ').join('-');
    return id;
  }

  getStreamUrl(testId) {
    return this.testReportObject.testReport[testId].streamUrl;
  }

  isStreamRequired(testId) {
    return this.testReportObject.testReport[testId].streamUrl ? true : false;
  }

  isCapture(intent, testId) {
    let method = JSON.parse(intent).verb;
    if (method == "CAPTURE" && (!testId.includes("Key Rotation"))) {
      return true;
    }
    return false;
  }
  isRcapture(intent, testId) {
    let method = JSON.parse(intent).verb;
    if (method == "RCAPTURE" && (!testId.includes("Key Rotation"))) {
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

  isAuthRequestRequired(testId) {
    return this.testReportObject.testReport[testId].enableAuthTest ? true : false;
  }

  isRequired(testId) {
    if (testId == "Device Status" || testId.includes("Key Rotation")) {
      return false;
    } else {
      return true;
    }
  }

  isKeyRotation(intent, testId) {
    let method = JSON.parse(intent).verb;
    if ((method == "CAPTURE" || method == "RCAPTURE") && testId.includes("Key Rotation")) {
      return true;
    }
    return false;
  }


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
    let uin = 0;
    console.log("Starting auth test for >>> " + testId);
    this.dataService.authTestCallByUin(this.testReportObject.testReport[testId].runId, testId, uin).subscribe(
      result => {
        //window.alert(JSON.stringify(result))
        this.openDialog("Auth Response", JSON.stringify(result))
        this.authloading = false
      },
      error => this.openDialog("Auth Response", JSON.stringify(error.error.text))
    );

  }

  openUINDialog(testReportObject1: any, key: any): void {
    this.testReportObject1 = testReportObject1;
    this.testId = key;
    console.log(this.testId);
    console.log(this.testReportObject1);
    const dialogRef = this.dialog.open(DialogOverviewExampleDialog, {
      width: '300px',
      data: { uin: this.uin, testReportObject1: this.testReportObject1, testId: this.testId }
    }

    );

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
      this.uin = result;
    });
  }



  openCaptureDataDialog(testReportObject1: any, key: any,request:any): void {
    this.testReportObject1 = testReportObject1;
    this.testId = key;
    const dialogRef = this.dialog.open(DialogOverviewCaptureDialog, {
      width: '300px',
      data: { timeOut: this.timeOut,
        requestScore:this.requestScore,
        previousHash:this.previousHash,
        testReportObject1:this.testReportObject1,
        testId: this.testId ,
        request:request}
    });
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
