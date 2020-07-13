import {Component, OnInit, ViewChild, ElementRef} from '@angular/core';
import {LocalStorageService} from '../../services/local-storage/local-storage.service';
import {ComposeRequest} from '../../dto/compose-request';
import {DataService} from '../../services/data/data.service';
import {MdsService} from '../../services/mds/mds.service';
import { DOCUMENT } from '@angular/common';
import * as jwt_decode from 'jwt-decode';
import { DomSanitizer } from '@angular/platform-browser';


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
  currentTestId: string;

  mdsStream: any;
  @ViewChild('stream') stream: ElementRef;

  videoSourceBuffer;

  imgUrl: string = 'http://127.0.0.1:4501/stream?deviceId=1&deviceSubId=1';
  imageToShow: any;
  isImageLoading: boolean;

  constructor(
    private localStorageService: LocalStorageService,
    private dataService: DataService,
    private mdsService: MdsService,
    private _sanitizer: DomSanitizer
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
      console.log("Finished capturing MDS requests");
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


  getMDSResponse(request, runId, testId) {
      let mdmResponse = this.testReportObject.testReport[testId].responseData;
      /* if(this.isMDSResponseCaptured(testId) || this.testReportObject.testReport[testId].responseData) {
       console.log("Nothing to do as MDS response is already captured");
      }
      else */ if(this.mdmInitiated === true) {
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
                  this.validateMDSResponse(runId, testId, request, response);
                },
                error => { this.validateMDSResponse(runId, testId, request, error); }
              );
              console.log("Finished capturing MDS Responses >>>>> " + testId);
              this.mdmInitiated = false;
      }
    }

    validateMDSResponse(runId, testId, request, response) {
          this.dataService.validateResponse(runId, testId, request, response).subscribe(
                      result => {
                        this.testReportObject = result;
                        console.log('result:' + result);
                      },
                      error => window.alert(error)
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
     switch(method.toUpperCase()) {
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

  getJWTDecoded(token){
    return jwt_decode(token);
  }

  createImageFromBlob(image: Blob) {
     let reader = new FileReader();
     reader.addEventListener("load", () => {
        this.imageToShow = reader.result;
     }, false);

     if (image) {
        reader.readAsDataURL(image);
     }
    }

    getImageFromService() {
        this.isImageLoading = true;
        this.mdsService.getImage(this.imgUrl).subscribe(data => {
          this.createImageFromBlob(data);
          this.isImageLoading = false;
        }, error => {
          this.isImageLoading = false;
          console.log(error);
        });
    }

    startStreaming(testId) {
       this.getImageFromService();
    }

    stopStreaming(testId) {
      let element = document.getElementById(this.getStreamImgTagId(testId));
      if(element) {
        (<HTMLImageElement>element).setAttribute("src", "");
      }
    }

    getSanitizedSafeURLResource(data) {
      return this._sanitizer.bypassSecurityTrustHtml(data);
    }
}
