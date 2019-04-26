import { Injectable } from '@angular/core';
import { AngularFireDatabase } from '@angular/fire/database';

@Injectable({
  providedIn: 'root'
})
export class DatabaseService {

  constructor(private afDatabase: AngularFireDatabase) { }

  public saveWebToken(uid: string, token: string): Promise<any> {
    return this.afDatabase.database.ref('users')
      .child(uid)
      .child('webToken')
      .set(token);
  }
}
