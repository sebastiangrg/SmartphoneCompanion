import { Injectable } from '@angular/core';
import { AngularFireMessaging } from '@angular/fire/messaging';
import { Observable } from 'rxjs';
import { mergeMapTo, take } from 'rxjs/operators';
import { DatabaseService } from './database.service';

@Injectable({
  providedIn: 'root'
})
export class FCMService {

  constructor(
    private afMessaging: AngularFireMessaging,
    private databaseService: DatabaseService
  ) { }

  public getTokenChanges(): Observable<string> {
    return this.afMessaging.tokenChanges;
  }

  public requestPermission(): Observable<string> {
    return this.afMessaging.requestPermission
      .pipe(mergeMapTo(this.afMessaging.tokenChanges));
  }

  public getMessages(): Observable<any> {
    return this.afMessaging.messages;
  }

  public saveWebToken(uid: string): void {
    this.requestPermission()
      .pipe(take(1))
      .subscribe(
        (token: string) => {
          console.log('Permission granted! Token: ' + token);
          this.databaseService.saveWebToken(uid, token)
            .then()
            .catch((error: any) => {
              console.log(error);
            });
        },
        (error) => {
          console.error(error);
        });
  }
}
