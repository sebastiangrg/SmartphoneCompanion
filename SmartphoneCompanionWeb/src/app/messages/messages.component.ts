import { Component, OnInit } from '@angular/core';
import { SyncService } from '../services/sync.service';
import { DatabaseService } from '../services/database.service';
import { SMSMessage } from '../model/SMSMessage';
import { take } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { User } from 'firebase';
import Utils from '../utils';

@Component({
  selector: 'app-messages',
  templateUrl: './messages.component.html',
  styleUrls: ['./messages.component.scss']
})
export class MessagesComponent implements OnInit {

  selectedConversation: number;
  lastMessages: SMSMessage[];
  contactNames: Map<string, string>;
  user: User;

  constructor(
    private syncService: SyncService,
    private databaseService: DatabaseService,
    private authService: AuthService
  ) {
    this.contactNames = new Map<string, string>();
  }

  ngOnInit() {
    this.authService.getUser()
      .pipe(take(1))
      .subscribe((user: User) => {
        this.user = user;
        this.getContacts(user.uid);
        this.getLastMessages(user.uid);
        this.syncService.syncLastMessages();
      });
  }

  private getLastMessages(uid: string): void {
    this.databaseService.getLastMessages(uid).valueChanges()
      .pipe(take(1))
      .subscribe((messages: SMSMessage[]) => {
        this.lastMessages = messages.sort((a, b) => b.datetime.time - a.datetime.time);
        this.selectedConversation = this.lastMessages.length > 0 ? this.lastMessages[0].thread : -1;
      });
  }

  private getContacts(uid: string): void {
    this.databaseService.getContacts(uid)
      .pipe(take(1))
      .subscribe((contacts: Map<string, string>) => {
        this.contactNames = contacts;
      });
  }

  getContactName(phoneNumber: string): string {
    const cleanedPhoneNumber = Utils.cleanPhoneNumber(phoneNumber);
    if (this.contactNames.has(cleanedPhoneNumber)) {
      return this.contactNames.get(cleanedPhoneNumber);
    }
    return phoneNumber;
  }

  getInitial(phoneNumber: string): string {
    const cleanedPhoneNumber = Utils.cleanPhoneNumber(phoneNumber);
    if (this.contactNames.has(cleanedPhoneNumber)) {
      return this.contactNames.get(cleanedPhoneNumber).substr(0, 1);
    }
    return '#';
  }

  selectConversation(thread: number): void {
    this.selectedConversation = thread;
    this.syncService.syncConversation(thread);
  }
}