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

  public async loginAnonymously(): Promise<void | auth.UserCredential> {
    try {
      return this.afAuth.auth.signInAnonymously();
    } catch (error) {
      const errorCode = error.code;
      const errorMessage = error.message;

      if (errorCode === 'auth/operation-not-allowed') {
        alert('You must enable Anonymous auth in the Firebase Console.');
        console.log(errorMessage);
      } else {
        console.error(error);
      }
    }
  }

  public linkWithToken(token: any) {
    const user = this.afAuth.auth.currentUser;
    // delete the user data
    this.afDatabase.database.ref('/users').child(user.uid).remove().then().catch(err => console.log(err));
    // sign in using the received custom token
    this.afAuth.auth.signInWithCustomToken(token);
  }

  public getUser(): Observable<User> {
    return this.afAuth.user;
  }

  public logout(): Promise<void> {
    return this.afAuth.auth.signOut();
  }
}
