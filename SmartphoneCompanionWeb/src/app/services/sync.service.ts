import { Injectable } from '@angular/core';
import { AngularFireFunctions } from '@angular/fire/functions';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SyncService {

  constructor(private afFunctions: AngularFireFunctions) { }

  public syncConversation(thread: number): Observable<any> {
    const callable = this.afFunctions.httpsCallable('syncConversation');

    return callable({ thread: thread.toString() });
  }

  public syncLastMessages(): Observable<any> {
    const callable = this.afFunctions.httpsCallable('syncLastMessages');

    return callable({});
  }

  public syncContacts(): Observable<any> {
    const callable = this.afFunctions.httpsCallable('syncContacts');

    return callable({});
  }

  public syncCallLog(): Observable<any> {
    const callable = this.afFunctions.httpsCallable('syncCallLog');

    return callable({});
  }

  public sendSMSMessage(phoneNumber: string, content: string) {
    if (!this.isMessageValid(content)) {
      return of(null);
    }
    const callable = this.afFunctions.httpsCallable('sendSMSMessage');

    return callable({ content, phoneNumber });
  }

  private isMessageValid(content: string): boolean {
    return content.length && content.indexOf('<') === -1;
  }
}
