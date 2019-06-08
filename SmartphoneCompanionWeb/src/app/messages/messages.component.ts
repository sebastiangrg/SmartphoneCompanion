import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { User } from 'firebase';
import { Observable, of } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { Contact } from '../model/Contact';
import { SMSMessage } from '../model/SMSMessage';
import { AuthService } from '../services/auth.service';
import { DatabaseService } from '../services/database.service';
import { SyncService } from '../services/sync.service';
import Utils from '../utils';

@Component({
  selector: 'app-messages',
  templateUrl: './messages.component.html',
  styleUrls: ['./messages.component.scss']
})
export class MessagesComponent implements OnInit, AfterViewChecked {

  constructor(
    private syncService: SyncService,
    private databaseService: DatabaseService,
    private authService: AuthService
  ) {
    this.selectedConversation = -1;
  }

  user: User;

  contacts: Contact[];

  lastMessages$: Observable<SMSMessage[]>;

  conversation$: Observable<SMSMessage[]>;
  selectedConversation: number;

  ngOnInit() {
    this.conversation$ = of([]);
    this.authService.getUser()
      .pipe(take(1))
      .subscribe((user: User) => {
        this.user = user;
        this.getContacts();
        this.getLastMessages();
        this.syncService.syncLastMessages();
        this.syncService.syncContacts();
      });
  }

  ngAfterViewChecked() {
    const container = document.getElementById('conversation');
    container.scrollTop = container.scrollHeight;
  }

  private getLastMessages(): void {
    this.lastMessages$ = this.databaseService.getLastMessages(this.user.uid).valueChanges()
      .pipe(
        map((messages: SMSMessage[]) => {
          return messages.sort((a, b) => b.datetime.time - a.datetime.time);
        })
      );
  }

  private getContacts(): void {
    this.databaseService.getContacts(this.user.uid)
      .subscribe((contacts: Contact[]) => {
        this.contacts = contacts;
      });
  }

  private getConversation(thread: number): void {
    this.conversation$ = this.databaseService.getConversation(this.user.uid, thread).valueChanges()
      .pipe(
        map((conversation: SMSMessage[]) => {
          return conversation.sort((a, b) => a.datetime.time - b.datetime.time);
        }),
      );
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

  selectConversation(thread: number): void {
    this.selectedConversation = thread;
    this.syncService.syncConversation(thread);
    this.getConversation(thread);
  }
}
