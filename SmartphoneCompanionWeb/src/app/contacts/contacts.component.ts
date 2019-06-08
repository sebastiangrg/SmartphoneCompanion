import { Component, OnInit } from '@angular/core';
import { SyncService } from '../services/sync.service';
import { DatabaseService } from '../services/database.service';
import { AuthService } from '../services/auth.service';
import { User } from 'firebase';
import { Observable } from 'rxjs';
import { take, map } from 'rxjs/operators';
import { Contact } from '../model/Contact';

@Component({
  selector: 'app-contacts',
  templateUrl: './contacts.component.html',
  styleUrls: ['./contacts.component.scss']
})
export class ContactsComponent implements OnInit {

  constructor(
    private syncService: SyncService,
    private databaseService: DatabaseService,
    private authService: AuthService
  ) { }

  user: User;
  contacts$: Observable<Contact[]>;
  filter: string;

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

  message(person: string | Contact) {
    if (person instanceof Contact) {

    } else {

    }
  }
}
