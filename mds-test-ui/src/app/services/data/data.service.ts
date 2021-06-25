import { Injectable } from '@angular/core';
import { throwError } from 'rxjs';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { catchError } from 'rxjs/operators';

import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ModalComponent } from '../../modal/modal.component';
import { map } from "rxjs/operators";
@Injectable({
  providedIn: 'root'
})
export class DataService {

  constructor(private httpClient: HttpClient, private dialog: MatDialog) { }

  getMasterData() {
    return this.httpClient.get(environment.base_url + 'testmanager/masterdata')
      .pipe(
        catchError(this.handleError)
      );
  }
  getTestReport(runId) {
    return this.httpClient.get(environment.base_url + 'testmanager/report/' +runId+ '/json')
      .pipe(
        catchError(this.handleError)
      );
  }
  getTests(requestBody) {
    return this.httpClient.post(environment.base_url + 'testmanager/test', requestBody)
      .pipe(
        catchError(this.handleError)
      );
  }

  createRun(requestBody) {
    return this.httpClient.post(environment.base_url + 'testmanager/createrun', requestBody)
      .pipe(
        catchError(this.handleError)
      );
  }

  getRuns(email) {
    return this.httpClient.get(environment.base_url + 'testmanager/runs/' + email)
      .pipe(
        catchError(this.handleError)
      );
  }

  download(runId,testId): any {
      return this.httpClient.get(environment.base_url + 'testrunner/download/' + runId+"/"+testId, { responseType: 'blob' }).pipe(map((response)=>{
        console.log(response);
        return {
            filename: runId+'.pdf',
            data: response
        };
    }));
  }

  decodeDeviceInfo(deviceInfoResponse: any) {
    return this.httpClient.post(environment.base_url + 'testrunner/decodedeviceinfo', deviceInfoResponse)
      .pipe(
        catchError(this.handleError)
      );
  }

  private handleError(error: HttpErrorResponse) {
    if (error.error instanceof ErrorEvent) {
      // A client-side or network error occurred. Handle it accordingly.
      console.error('An error occurred:', error.error.message);
    } else {

      console.error('An error occurred >>> ', JSON.stringify(error));

      if(error.status === 0 || error.status === 404) {
          return throwError('Not Connected to Server');
      }

      // The backend returned an unsuccessful response code.
      // The response body may contain clues as to what went wrong,
      console.error(
        `Backend returned code ${error.status}, ` +
        `body was: ${error.error}`);
    }
    if(error.status === 200 ) {
      return throwError(error);
  }
   
    // return an observable with a user-facing error message
    return throwError(error.error.message);
  }

  composeRequest(runId: string, test: string, deviceDto: { port: any; deviceInfo: any }) {
    return this.httpClient.post(environment.base_url + 'testrunner/composerequest', {
      runId,
      testId: test,
      uiInputs: [],
      deviceInfo: deviceDto

    })
      .pipe(
        catchError(this.handleError)
      );
  }

  composeAllRequests(runId: string, deviceDto: { port: any; deviceInfo: any }) {
      return this.httpClient.post(environment.base_url + 'testrunner/getallrequests', {
        runId,
        deviceInfo: deviceDto
      })
        .pipe(
          catchError(this.handleError)
        );
    }

  validateResponse(runId: any, testId: string, request: any, response: any) {
    return this.httpClient.post(environment.base_url + 'testrunner/validateresponse', {
      runId,
      testId,
      mdsResponse: JSON.stringify(response),
      mdsRequest: JSON.stringify(request),
      resultVerbosity: ''

    })
      .pipe(
        catchError(this.handleError)
      );
  }

  authTestCall(runId: any, testId: string) {
      return this.httpClient.post(environment.base_url + 'testrunner/validateauthrequest', {
        runId,
        testId
      })
        .pipe(
          catchError(this.handleError)
        );
    }

    authTestCallByUin(runId: any, testId: string,uin :any) {
      return this.httpClient.post(environment.base_url + 'testrunner/validateauthrequest', {
        runId,
        testId,
        uin
      })
        .pipe(
          catchError(this.handleError)
        );
    }
    
    openDialog(title: string, message: string): void {
              this.dialog.open(ModalComponent, {
                width: '40%',
                data: {'title': title, 'message' : message }
              });
          }
}
