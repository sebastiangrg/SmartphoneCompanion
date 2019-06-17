import { Component, OnInit } from '@angular/core';
import { SyncService } from '../services/sync.service';
import { DatabaseService } from '../services/database.service';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { User } from 'firebase';
import { Observable } from 'rxjs';
import { take, map } from 'rxjs/operators';
import { Call } from '../model/Call';
import { Contact } from '../model/Contact';
import Utils from '../utils';

@Component({
  selector: 'app-call-log',
  templateUrl: './call-log.component.html',
  styleUrls: ['./call-log.component.scss']
})
export class CallLogComponent implements OnInit {

  constructor(
    private syncService: SyncService,
    private databaseService: DatabaseService,
    private authService: AuthService
  ) { }

  user: User;
  calls$: Observable<Call[]>;

  contacts: Contact[];

  ngOnInit() {
    this.authService.getUser()
      .pipe(take(1))
      .subscribe((user: User) => {
        this.user = user;
        this.getContacts();
        this.syncService.syncContacts();
        this.getCallLog();
        this.syncService.syncCallLog();
      });
  }

  private getCallLog(): void {
    this.calls$ = this.databaseService.getCallLog(this.user.uid)
      .pipe(
        map((calls: Call[]) => {
          return calls.reverse();
        }));
  }

  private getContacts(): void {
    this.databaseService.getContacts(this.user.uid)
      .subscribe((contacts: Contact[]) => {
        this.contacts = contacts;
      });
  }

  getContactName(phoneNumber: string): string {
    const cleanedPhoneNumber = Utils.cleanPhoneNumber(phoneNumber);
    const contactIndex = this.contacts.findIndex(c => c.phoneNumber === cleanedPhoneNumber);
    if (contactIndex > -1) {
      return this.contacts[contactIndex].name;
    }
    return phoneNumber;
  }

  getInitial(phoneNumber: string): string {
    const cleanedPhoneNumber = Utils.cleanPhoneNumber(phoneNumber);
    const contactIndex = this.contacts.findIndex(c => c.phoneNumber === cleanedPhoneNumber);
    if (contactIndex > -1) {
      return this.contacts[contactIndex].name.substr(0, 1);
    }
    return '#';
  }

  hasContact(phoneNumber: string): boolean {
    const cleanedPhoneNumber = Utils.cleanPhoneNumber(phoneNumber);
    const contactIndex = this.contacts.findIndex(c => c.phoneNumber === cleanedPhoneNumber);
    return contactIndex > -1;
  }
}
