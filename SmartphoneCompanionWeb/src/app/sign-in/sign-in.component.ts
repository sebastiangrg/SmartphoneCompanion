import { Component, OnInit, OnDestroy } from '@angular/core';
import { FCMService } from '../services/fcm.service';
import { DatabaseService } from '../services/database.service';
import { AuthService } from '../services/auth.service';
import { User } from 'firebase';
import { Observable, Subscription, from, of } from 'rxjs';
import { tap, concatMap, map, take } from 'rxjs/operators';
import { Router } from '@angular/router';

@Component({
  selector: 'app-sign-in',
  templateUrl: './sign-in.component.html',
  styleUrls: ['./sign-in.component.scss']
})
export class SignInComponent implements OnInit, OnDestroy {

  private messageSubscription: Subscription;

  user$: Observable<any>;

  constructor(
    private authService: AuthService,
    private databaseService: DatabaseService,
    private fcmService: FCMService,
    private router: Router
  ) { }

  ngOnInit() {
    this.initUser();
  }

  private initUser() {
    // sign in anonymously
    this.authService.signInAnonymously()
      .then()
      .catch((error: any) => {
        console.log(error);
      });

    // retrieve the user, save the webToken
    this.user$ = this.authService.getUser()
      .pipe(
        tap((user: User) => {
          if (user) {
            if (!user.phoneNumber) {
              // anonymous user
              this.fcmService.saveWebToken(user.uid);
            } else {
              // authenticated user
              this.router.navigate(['']);
            }
          } else {
            return of(null);
          }
        }));

    // subscribe to messages
    this.subscribeToMessages();
  }

  private subscribeToMessages(): void {
    this.messageSubscription = this.fcmService.getMessages()
      .subscribe((message: any) => {
        // custom token message
        if (message.data.customToken) {
          const customToken = message.data.customToken;
          this.authService.linkWithToken(customToken)
            .then()
            .catch((error: any) => {
              console.log(error);
            });
        }
      });
  }

  ngOnDestroy(): void {
    this.messageSubscription.unsubscribe();
  }
}
