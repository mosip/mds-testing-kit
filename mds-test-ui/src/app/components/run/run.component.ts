import {Component, OnInit, ViewChild, ElementRef} from '@angular/core';
import {LocalStorageService} from '../../services/local-storage/local-storage.service';
import {ComposeRequest} from '../../dto/compose-request';
import {DataService} from '../../services/data/data.service';
import {MdsService} from '../../services/mds/mds.service';
import { DOCUMENT } from '@angular/common';

@Component({
  selector: 'app-run',
  templateUrl: './run.component.html',
  styleUrls: ['./run.component.css']
})
export class RunComponent implements OnInit {
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
  streamURL: string;
  currentTestId: string;

  @ViewChild('stream') stream: ElementRef;



  constructor(
    private localStorageService: LocalStorageService,
    private dataService: DataService,
    private mdsService: MdsService
  ) {
    this.JSON = JSON;
    this.Object = Object;
  }

  ngOnInit(): void {
    console.log(history.state.data);
    this.run = history.state.data;
    this.availablePorts = this.localStorageService.getAvailablePorts();
    //this.fetchReport();
    this.panelOpenState = false;
  }

  fetchReport() {
   this.dataService.getTestReport(this.run.runId).subscribe(
      body => {
        this.testReportObject = body;
        console.log(body);
      },
      error => window.alert(error)
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
      /* this.run.tests.forEach(
        test => {
            this.dataService.composeRequest(this.run.runId, test, deviceDto).subscribe(
              body => {
                console.log(body);
                this.requests.push(body);
                this.requestMds(body, this.run.runId, test);
              },
              error => window.alert(error)
            );
        }
      ); */
      this.dataService.composeAllRequests(this.run.runId, deviceDto).subscribe(
        body => {
          this.requests = [];
          this.testReportObject = body;
          console.log(body);
        },
        error => window.alert(error)
      );
      console.log("Finished capturing MDM requests");
      /* this.run.tests.forEach(
      test => {
          let testresult = this.testReportObject.testReport[test];
          if(testresult && testresult.requestData) {
              this.testReportObject.testReport[test].currentState = "Initiating request to Device";
              let mdsRequest = JSON.parse(testresult.requestData);
              let mdsResponse = this.mdsService.request(mdsRequest);
              this.testReportObject.testReport[test].currentState = "Request to Device completed";
          }
      }); */
  }


  getMDMResponse(request, runId, testId) {
      let mdmResponse = this.testReportObject.testReport[testId].responseData;
      if(this.isMDMResponseCaptured(testId) || this.testReportObject.testReport[testId].responseData) {
       console.log("Nothing to do as MDM response is already captured");
      }
      else if(this.mdmInitiated === true) {
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
                    console.log(response);
                    this.dataService.validateResponse(runId, testId, request, response).subscribe(
                      result => {
                        this.testReportObject = result;
                        console.log('result:' + result);
                      },
                      error => window.alert(error)
                    );
                  },
                error => window.alert(error)
              );
              console.log("Finished capturing MDM Responses >>>>> " + testId);
              this.mdmInitiated = false;
      }
    }

    getStreamImgTagId(testId) {
      let id = testId.split(' ').join('-');
      return id;
    }

    startStreaming(testId) {
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
    }

  /* getMDSResponse1(request, runId, testId) {
    let mdmResponse = this.testReportObject.testReport[testId].responseData;
    if(this.isMDMResponseCaptured(testId) || this.testReportObject.testReport[testId].responseData) {
     console.log("Nothing to do as MDM response is already captured");
    }
    else if(this.mdmInitiated === true) {
     console.log("MDM request is currently going on .....");
    }
    else {
      let self = this;

      console.log("Initiating request to Device >>>> " + testId);
      this.requests.push(testId);
      this.mdmInitiated = true;
      this.startStream(JSON.parse(request));
      this.mdsService.request(JSON.parse(request)).subscribe(
                response => {
                  console.log(response);
                  this.dataService.validateResponse(runId, testId, request, response).subscribe(
                    result => {
                      this.testReportObject = result;
                      console.log('result:' + result);
                      setTimeout(()=> {    //<<<---    using ()=> syntax
                            self.streamingInitiated = false;
                            self.streamURL = "";
                       }, 3000);
                    },
                    error => window.alert(error)
                  );
                },
              error => window.alert(error)
            );
            console.log("Finished capturing MDM Responses >>>>> " + testId);
            this.mdmInitiated = false;
            //this.streamingInitiated = false;
            //this.streamURL = "";
    }
  } */

 /*  startStream(request) {
    if(request.verb.toLowerCase() === "rcapture") {
        console.log("Starting stream for rcapture test case");
        this.streamURL = "http://127.0.0.1:" + this.currentPort + "/stream?deviceId=" +this.selectedDevice.deviceId + "&deviceSubId=3";
        this.streamingInitiated = true;
        console.log("Streaming response >>>>>> stream src : " + this.streamURL);
        //this.video.nativeElement.src = streamSrc;
        //this.video.nativeElement.play();
    }
  } */

  getStreamUrl(testId) {
    return this.testReportObject.testReport[testId].streamUrl;
  }

  isMDMResponseCaptured(testId) {
      let mdmCaptured = false;
      for(let i=0; i<this.requests.length;i++) {
        if(this.requests[i] === testId) {
          mdmCaptured = true;
        }
      }
      return mdmCaptured;
  }

  getMDSResponse(request, runId, testId) {
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
    }

  requestMds(request, runId, testId) {
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
  }

  OnPortSelect(value: any) {
    this.currentPort = value;
    this.devices = this.localStorageService.getDevicesByPortNumber(value);
  }

  getPassedValidators(value: any) {
    if(value !== undefined) {
      let total = 0;
      for (let i = 0; i<value.length; i++) {
        if(value[i].status == 'Passed')
          total = total + 1;
      }
      return total + ' out of ' + value.length + ' validations Passed';
    }
    return '0 out of 0 Passed';
  }
}
