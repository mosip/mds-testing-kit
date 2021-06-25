import {Component, Inject} from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';

import { ModalComponent } from '../modal/modal.component';
import {DataService} from '../services/data/data.service';
export interface DialogData {
    uin: string;
    testReportObject1: any;
    testId:string;
  }

@Component({
    selector: 'app-module',
    templateUrl: 'auth.compnent.html',
    styleUrls: ['./auth.component.css']
  })
  export class DialogOverviewExampleDialog {
    authloading = false;

    data1 :DialogData;
    uin=5698342963;
  testReportObject: any;
    constructor(
        private dataService: DataService,
        private dialog: MatDialog,
      public dialogRef: MatDialogRef<DialogOverviewExampleDialog>,
      @Inject(MAT_DIALOG_DATA) public data: DialogData) {
          console.log(data);
         
this.data1=data;
this.data1.uin="6027349120";
      }
  
    onNoClick(): void {
      this.dialogRef.close();
    }
  

    startAuthTest(uin : any) {
      this.authloading = true;
   
        this.uin=uin;
        this.dataService.authTestCallByUin(this.data1.testReportObject1.testReport[this.data1.testId].runId, this.data1.testId,uin).subscribe(
                result => {
                     //window.alert(JSON.stringify(result))
                    console.log("Auth Response", JSON.stringify(result))
                    this.authloading = false;
                },
                // error => console.log("Auth Error Response", error)
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
  }