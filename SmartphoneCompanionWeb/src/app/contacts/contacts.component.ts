import { Component, OnInit } from '@angular/core';
import { SyncService } from '../services/sync.service';
import { DatabaseService } from '../services/database.service';
import { AuthService } from '../services/auth.service';
import { User } from 'firebase';
import { Observable } from 'rxjs';
import { take, map } from 'rxjs/operators';
import { Contact } from '../model/Contact';
import { Router } from '@angular/router';

@Component({
  selector: 'app-contacts',
  templateUrl: './contacts.component.html',
  styleUrls: ['./contacts.component.scss']
})
export class ContactsComponent implements OnInit {

  constructor(
    private syncService: SyncService,
    private databaseService: DatabaseService,
    private authService: AuthService,
    private router: Router
  ) { }

  user: User;
  contacts$: Observable<Contact[]>;
  filter: string;
  selectedContact: Contact;
  draft: string;

  ngOnInit() {
    this.filter = '';

    this.authService.getUser()
      .pipe(take(1))
      .subscribe((user: User) => {
        this.user = user;
        this.getContacts();
        this.syncService.syncContacts();
      });
  }

  private getContacts(): void {
    this.contacts$ = this.databaseService.getContacts(this.user.uid)
      .pipe(
        map((contacts: Contact[]) => {
          return contacts.sort((a: Contact, b: Contact) => a.name > b.name ? 1 : -1);
        }));
  }

  filterContacts(contacts: Contact[]) {
    return contacts.filter((c: Contact) =>
      c.name.toLowerCase().indexOf(this.filter.toLowerCase()) > -1
      || c.phoneNumber.indexOf(this.filter) > -1);
  }

  isValidPhoneNumber(phoneNumber: string) {
    return phoneNumber && phoneNumber.match(/^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\s\./0-9]*$/);
  }

  selectContact(person: string | Contact) {
    if (person instanceof Contact) {
      this.selectedContact = person;
    } else {
      this.selectedContact = new Contact(person, person);
    }
  }

  closeModal() {
    this.selectedContact = null;
  }

  sendMessage(phoneNumber: string): void {
    if (this.draft.length) {
      this.syncService.sendSMSMessage(phoneNumber, this.draft);
      this.draft = '';
      this.router.navigate(['messages']);
    }
  }
}
