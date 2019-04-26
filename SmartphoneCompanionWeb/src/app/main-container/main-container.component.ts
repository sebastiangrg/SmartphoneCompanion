import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from 'firebase';
import { AuthService } from '../services/auth.service';
import { DatabaseService } from '../services/database.service';
import { FCMService } from '../services/fcm.service';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';

@Component({
  selector: 'app-main-container',
  templateUrl: './main-container.component.html',
  styleUrls: ['./main-container.component.scss']
})
export class MainContainerComponent implements OnInit {
  data$: Observable<any>;
  user: User;

  constructor(
    private authService: AuthService,
    private fcmService: FCMService,
    private router: Router
  ) { }

  ngOnInit() {
    this.initUser();
  }

  private initUser() {
    // retrieve the user and if he is authenticated, save the webToken and subscribe to messages
    this.data$ = this.authService.getUser()
      .pipe(
        tap((user: User) => {
          this.user = user;
          if (!user || !user.phoneNumber) {
            this.router.navigate(['signIn']);
          } else {
            this.fcmService.saveWebToken(user.uid);
          }
        }));
  }

  signOut() {
    this.authService.signOut();
    this.router.navigate(['signIn']);
  }
}
