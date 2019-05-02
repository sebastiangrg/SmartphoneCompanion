import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from 'firebase';
import { AuthService } from '../services/auth.service';
import { DatabaseService } from '../services/database.service';
import { FCMService } from '../services/fcm.service';
import { Router } from '@angular/router';
import { tap, take } from 'rxjs/operators';
import { SyncService } from '../services/sync.service';

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
    private syncService: SyncService,
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

  syncLastMessages() {
    this.syncService.syncLastMessages()
      .pipe(
        take(1)
      )
      .subscribe(
        (res: any) => {
          console.log(res);
        });
  }

  syncContacts() {
    this.syncService.syncContacts()
      .pipe(
        take(1)
      )
      .subscribe(
        (res: any) => {
          console.log(res);
        });
  }

  syncConversation() {
    const thread = 258;
    this.syncService.syncConversation(thread)
      .pipe(
        take(1)
      )
      .subscribe(
        (res: any) => {
          //console.log(res);
        });
  }

  signOut() {
    this.authService.signOut();
    this.router.navigate(['signIn']);
  }
}
