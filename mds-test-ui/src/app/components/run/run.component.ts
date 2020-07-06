import {Component, OnInit} from '@angular/core';
import {LocalStorageService} from '../../services/local-storage/local-storage.service';
import {ComposeRequest} from '../../dto/compose-request';
import {DataService} from '../../services/data/data.service';
import {MdsService} from '../../services/mds/mds.service';

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
    this.fetchReport();
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
      this.run.tests.forEach(
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
}
