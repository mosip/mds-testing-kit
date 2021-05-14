import { Injectable } from '@angular/core';
import {Observable, of, throwError, Subject} from 'rxjs';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {catchError, map} from 'rxjs/operators';
import {DataService} from '../data/data.service';
import {LocalStorageService} from '../local-storage/local-storage.service';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ModalComponent } from '../../modal/modal.component';


@Injectable({
  providedIn: 'root'
})
export class MdsService {

  private mdsHost: string
  private mdsUrl: string;

  constructor(
    private httpClient: HttpClient,
    private dataService: DataService,
    private localStorageService: LocalStorageService,
    private dialog: MatDialog
  ) { }

  discover(host:string, port: string) {
    this.mdsHost = host;
    this.mdsUrl = this.mdsHost + ':' + port + '/device';
    return this.httpClient.request('MOSIPDISC', this.mdsUrl, {
      body: {
        type: 'Biometric Device'
      }
    }).pipe(
        catchError(this.handleError)
      );
  }

  getInfo(host:string, port: string) {
    this.mdsUrl = this.mdsHost + ':' + port + '/info';
    return this.httpClient.request('MOSIPDINFO', this.mdsUrl)
      .pipe(
        catchError(this.handleError)
      );
  }

  private handleError(error: HttpErrorResponse) {
    if (error.error instanceof ErrorEvent) {
      // A client-side or network error occurred. Handle it accordingly.
      console.error('An error occurred:', error.error.message);
    } else {
      // The backend returned an unsuccessful response code.
      // The response body may contain clues as to what went wrong,
      // console.error(
      //   `Backend returned code ${error.status}, ` +
      //   `body was: ${error.error}`);
    }
    // return an observable with a user-facing error message
    return throwError(
      'Something bad happened; please try again later.');
  }

  scanWithInfo() {
    // const ports = [];
    return new Observable(
      subscriber => {
        for (let i = 4501; i <= 4600; i++) {
          this.getInfo(this.mdsHost, i.toString()).subscribe(
            value => {
              this.dataService.decodeDeviceInfo(value).subscribe(
                decodedDeviceInfo => this.localStorageService.addDeviceInfos(i.toString(), decodedDeviceInfo),
                error => console.log("Failed to get decoded device info")
              );
            }
          );
        }
        subscriber.complete();
        return {unsubscribe() {}};
      }
    );
  }

  scan(host:string) {
    // const ports = [];
    return new Observable(
      subscriber => {
        for (let i = 4501; i <= 4600; i++) {
          // if (i == 4501) {
            this.discover(host, i.toString()).subscribe(
              value => {
                console.log('run' + value);
                this.localStorageService.addDeviceDiscover(i.toString(), value);
              }
            );
          // }
        }
        subscriber.complete();
        return {unsubscribe() {}};
      }
    );
  }


  async scanSync(host:string) {
    for (let i = 4501; i <= 4510; i++) {
      try {
        await this.discoverSynchronous(host, i + "");
     }
     catch (e) {
        console.log(e);
     }
    }
  }

  async discoverSynchronous(host:string, port: string) {
    await this.discoverSynchronousPromise(host, port).then(
      response => {
        // Promise successful
        if (response)
        {
          this.localStorageService.addDeviceDiscover(port, response); 
        }
      }
    ).catch(error => { console.log("promise result:err" + error) });
  }

  discoverSynchronousPromise(host:string, port: string) {
    try {
      return new Promise(resolve => {
          this.mdsHost = host;
          this.mdsUrl = this.mdsHost + ':' + port + '/device';
          let r : any;
          this.httpClient.request('MOSIPDISC', this.mdsUrl, {
            body: {
              type: 'Biometric Device'
            }
        }).subscribe(
              response => {
                  r = response;
              },
              error => {
                  // Config error handling if port and address not reolved error does not work
                  ;//return Promise.resolve(error); //this.handleErrorPromise(error);
                  resolve(r);
              },
              () => {
                  resolve(r);
              }
          )
      });
     }
   catch (e) {
      console.log(e);
   }
  }

  request(requestInfoDto: any) {
    return this.httpClient.request(requestInfoDto.verb, requestInfoDto.url, {body: requestInfoDto.body});
  }

  openDialog(title: string, message: string): void {
                this.dialog.open(ModalComponent, {
                  width: '40%',
                  data: {'title': title, 'message' : message }
                });
            }

}
