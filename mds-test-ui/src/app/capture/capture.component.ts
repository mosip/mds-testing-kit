import { Component, Inject } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MdsService } from '../services/mds/mds.service';

import { ModalComponent } from '../modal/modal.component';
import { DataService } from '../services/data/data.service';
export interface DialogData {
  timeOut: string;
  requestScore: string,
  previousHash: string,
  testReportObject1: any;
  testId: string;
  request:any;
}

@Component({
  selector: 'app-module',
  templateUrl: 'capture.compnent.html',
  styleUrls: ['./capture.component.css']
})
export class DialogOverviewCaptureDialog {

  validationResult: any;
  keyRotatOrderIdList = [3011, 3012, 3013];
  keyRotatOrderIdListRcapture = [3014];
  successErrorCodeOfAuth = "IDA-BIA-001";
  succesAuth = "authStatus=true";
  failErrorCodeOfAuth = "IDA-MPA-002";
  validCertButton = false;
  expiredCertButton = false;
  afterKeyRotationButton = false;
  loading = false;
  authloading = false;
  readyButton = false;
  busyButton = false;
  notReadyButton = false;
  notRegisteredButton = false;
  statusButton = false;

  data1: DialogData;
  timeOut = 0;
  testReportObject: any;
  constructor(
    private mdsService: MdsService,
    private dataService: DataService,
    private dialog: MatDialog,
    public dialogRef: MatDialogRef<DialogOverviewCaptureDialog>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData) {
      this.data1 = data;
      this.data1.requestScore = '40';
      this.data1.timeOut = '10000'
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  
  openDialog(title: string, message: string): void {
    this.authloading = false
    this.dialog.open(ModalComponent, {
      width: '80%',
      data: { 'title': title, 'message': message }
    });
  }

//testReportObject.testReport[key].requestData,testReportObject.testReport[key].runId, key,null

getMDSResponse(){
  this.authloading=true;
  let myObj = JSON.parse(this.data1.request);
  myObj.body=JSON.parse(myObj.body);
  let bio1=myObj.body.bio[0];
  bio1.requestedScore = this.data.requestScore;
  bio1.previousHash=this.data.previousHash;
  myObj.body.bio[0]=bio1;

  myObj.body.timeout=this.data.timeOut;
  myObj.body.bio[0].requestedScore=this.data.requestScore;
  myObj.body.bio[0].previousHash=this.data.previousHash;
 console.log(this.data1.testReportObject1);
  this.mdsService.request(myObj).subscribe(
    response => {
      this.validateMDSResponse(this.data1.testReportObject1.runId, this.data1.testId,
        myObj, response, status);
    },
    error => {
      this.validateMDSResponse(this.data1.testReportObject1.runId, 
         this.data1.testId, myObj, error, status);
    }
  );
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
        this.dialogRef.close();
        this.authloading = false;
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

  startAuthTest(timeOut: any) {
    this.authloading = true;

    this.timeOut = timeOut;
    this.dataService.authTestCallByUin(this.data1.testReportObject1.testReport[this.data1.testId].runId, this.data1.testId, timeOut).subscribe(
      result => {
        //window.alert(JSON.stringify(result))
        console.log("Auth Response", JSON.stringify(result))
        this.authloading = false;
      },
      // error => console.log("Auth Error Response", error)
      error => this.openDialog("Auth Response", JSON.stringify(error.error.text))

    );

  }

  getButtonName() {
    let method = JSON.parse(this.data1.request).verb;
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
}