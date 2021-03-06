import { Component, OnInit } from '@angular/core';
import { DataService } from '../../services/data/data.service';
import { map } from 'rxjs/operators';
import {Router} from '@angular/router';


@Component({
  selector: 'app-new-test',
  templateUrl: './new-test.component.html',
  styleUrls: ['./new-test.component.css']
})
export class NewTestComponent implements OnInit {
  title = 'mds-test-ui';
  tests: any;
  masterData: any;
  deviceTypes: [];
  selectedTests = [];
  selectedBiometricType: any;
  selectedDeviceType: any;
  selectedSbiVersion: any;
  selectedPurpose: any;
  email = '';
  runName = '';

  constructor(private dataService: DataService, private router: Router) {
  }


  ngOnInit() {
    this.masterData = this.dataService.getMasterData().subscribe(
      masterData => this.masterData = masterData,
      error => window.alert(error)
    );

  }

  OnBiometricSelect(event) {
    this.deviceTypes = event.value.deviceSubTypes;
  }

  OnGetTestsClicked() {
    const requestBody = {
      biometricType: this.selectedBiometricType.type,
      deviceSubType: this.selectedDeviceType,
      sbiSpecificationVersion: this.selectedSbiVersion,
      purpose: this.selectedPurpose
    };
    // console.log(requestBody);
    this.dataService.getTests(requestBody)
      .subscribe(
          tests => this.tests = tests,
        error => window.alert(error)
      );
  }

  OnCreateRunClicked() {
    const requestBody = {
      biometricType: this.selectedBiometricType.type,
      deviceSubType: this.selectedDeviceType,
      mdsSpecVersion: this.selectedSbiVersion,
      purpose: this.selectedPurpose,
      tests: this.selectedTests,
      email: this.email,
      runName: this.runName
    };
    console.log(requestBody);
    this.dataService.createRun(requestBody)
      .pipe(
        map((body: any) => {
          return body.runId;
        })
      )
      .subscribe(
          runId => {
            window.alert('created. Run ID: ' + runId);
            this.router.navigate(['/']);
          },
          error => window.alert(error)
      );
  }
}
