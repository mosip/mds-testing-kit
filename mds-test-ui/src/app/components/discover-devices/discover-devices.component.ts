import { Component, OnInit } from '@angular/core';
import {MdsService} from '../../services/mds/mds.service';
import {DataService} from '../../services/data/data.service';
import {LocalStorageService} from '../../services/local-storage/local-storage.service';
import {MatSelectChange} from '@angular/material/select';
import {MatSnackBar} from '@angular/material/snack-bar';

import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ModalComponent } from '../../modal/modal.component';

@Component({
  selector: 'app-discover-devices',
  templateUrl: './discover-devices.component.html',
  styleUrls: ['./discover-devices.component.css']
})
export class DiscoverDevicesComponent implements OnInit {
  discoveryResponse: any;
  infoResponse: any;
  availablePorts: string[];
  devices = [];
  scanning: boolean;
  mdshost: string;

  constructor(
    private mdsService: MdsService,
    private dataService: DataService,
    private localStorageService: LocalStorageService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.scanning = false;
    this.availablePorts = this.localStorageService.getAvailablePorts();
    this.mdshost = '';
  }

  discover(port: string) {
    this.mdsService.discover(this.mdshost, port).subscribe(
      response => this.discoveryResponse = response,
      error => this.openDialog("Alert", error)
    );
  }

  getInfo(port: string) {
    this.mdsService.getInfo(this.mdshost, port).subscribe(
      response => {
        this.infoResponse = response;
        this.dataService.decodeDeviceInfo(response).subscribe(
          decodedDeviceInfo => this.localStorageService.addDeviceInfos(port, decodedDeviceInfo),
          error => this.openDialog("Alert", error)
        );
      },
      error => this.openDialog("Alert", error)
    );
  }

  OnPortSelect(port) {
    this.devices = this.localStorageService.getDevicesByPortNumber(port);
  }

  scan(host) {

    if(host === '') {
      window.alert("Please enter SBI host")
    }

    this.mdshost = host;
    this.scanning = true;
    this.mdsService.scan(this.mdshost).subscribe(
      value => {},
      error => {},
      () => {
        this.scanning = false;
        this.availablePorts = this.localStorageService.getAvailablePorts();
        if(this.availablePorts && !this.availablePorts.length)
          this.openDialog("Alert", "Scan Complete, No devices discovered.");
        else
          this.openDialog("Message", "Scan Complete");
      }
    );
  }

  openSnackBar(message: string, action: string) {
    this.snackBar.open(message, action, {
      duration: 2000,
    });
  }

  openDialog(title: string, message: string): void {
          this.dialog.open(ModalComponent, {
            width: '40%',
            data: {'title': title, 'message' : message }
          });
      }

}
