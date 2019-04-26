import { Injectable } from '@angular/core';
import { AngularFireAuth } from '@angular/fire/auth';
import { auth, User } from 'firebase/app';
import { Observable } from 'rxjs';
import { AngularFireDatabase } from '@angular/fire/database';


@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private afAuth: AngularFireAuth, private afDatabase: AngularFireDatabase) { }

  public async signInAnonymously(): Promise<auth.UserCredential> {
    return this.afAuth.auth.signInAnonymously();
  }

  public linkWithToken(token: any): Promise<auth.UserCredential> {
    const user = this.afAuth.auth.currentUser;
    // delete the user data
    this.afDatabase.database.ref('/users').child(user.uid).remove().then().catch(err => console.log(err));
    // sign in using the received custom token
    return this.afAuth.auth.signInWithCustomToken(token);
  }

  public getUser(): Observable<User> {
    return this.afAuth.user;
  }

  public signOut(): Promise<void> {
    return this.afAuth.auth.signOut();
  }
}
