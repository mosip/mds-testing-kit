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
    templateUrl: 'auth.compnent.html'
  })
  export class DialogOverviewExampleDialog {
  
    data1 :DialogData;
    uin;
  testReportObject: any;
    constructor(
        private dataService: DataService,
        private dialog: MatDialog,
      public dialogRef: MatDialogRef<DialogOverviewExampleDialog>,
      @Inject(MAT_DIALOG_DATA) public data: DialogData) {
          console.log(data);
this.data1=data;

      }
  
    onNoClick(): void {
      this.dialogRef.close();
    }
  

    startAuthTest(uin : any) {
        this.uin=uin;
        this.dataService.authTestCallByUin(this.data1.testReportObject1.testReport[this.data1.testId].runId, this.data1.testId,uin).subscribe(
                result => {
                     //window.alert(JSON.stringify(result))
                    console.log("Auth Response", JSON.stringify(result))
                     
                },
                error => console.log("Auth Response", error)
              );

    }
  }