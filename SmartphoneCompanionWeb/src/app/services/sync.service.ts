import { Injectable } from '@angular/core';
import { AngularFireFunctions } from '@angular/fire/functions';

@Injectable({
  providedIn: 'root'
})
export class SyncService {

  constructor(private afFunctions: AngularFireFunctions) { }

  public syncConversation(thread: number) {
    const callable = this.afFunctions.httpsCallable('syncConversation');

    return callable({ thread: thread.toString() });
  }

  public syncLastMessages() {
    const callable = this.afFunctions.httpsCallable('syncLastMessages');

    return callable({});
  }

  public syncContacts() {
    const callable = this.afFunctions.httpsCallable('syncContacts');

    return callable({});
  }


}
