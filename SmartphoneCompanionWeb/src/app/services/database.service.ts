import { Injectable } from '@angular/core';
import { AngularFireDatabase } from '@angular/fire/database';

@Injectable({
  providedIn: 'root'
})
export class DatabaseService {

  constructor(private afDatabase: AngularFireDatabase) { }

  public getUserTokenReferece(uid: string): firebase.database.Reference {
    return this.afDatabase.database.ref('users').child(uid).child('token');
  }
}
