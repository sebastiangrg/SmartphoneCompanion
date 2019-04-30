import { Injectable } from '@angular/core';
import { AngularFireFunctions } from '@angular/fire/functions';

@Injectable({
  providedIn: 'root'
})
export class SyncService {

  constructor(private afFunctions: AngularFireFunctions) { }

  public syncConversations() {
    const callable = this.afFunctions.httpsCallable('syncConversations');

    return callable({});
  }
}
