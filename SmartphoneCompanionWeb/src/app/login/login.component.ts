import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { User } from 'firebase';

import { AuthService } from '../services/auth.service';
import { DatabaseService } from '../services/database.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  constructor(private authService: AuthService, private databaseService: DatabaseService) { }
  user$: Observable<User>;
  uid: string;
  userTokenRef: firebase.database.Reference;

  ngOnInit() {
    this.user$ = this.authService.getUser()
      .pipe(
        tap((user: User) => {
          if (user) {
            this.uid = user.uid;
            if (!user.phoneNumber) {
              this.waitForToken();
            }
          } else {
            this.initUser();
            this.uid = null;
            this.userTokenRef = null;
          }
        })
      );
  }

  async initUser() {
    await this.authService.loginAnonymously();
  }

  async logOut() {
    await this.authService.loginAnonymously();
  }

  waitForToken() {
    this.userTokenRef = this.databaseService.getUserTokenReferece(this.uid);

    this.userTokenRef.off();
    this.userTokenRef.on('value', (snapshot: firebase.database.DataSnapshot) => {
      if (snapshot.val()) {
        console.log(snapshot.val());
        this.authService.linkWithToken(snapshot.val());
        this.userTokenRef.off();
      }
    });
  }

}
