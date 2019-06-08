import { Injectable } from '@angular/core';
import { AngularFireDatabase, AngularFireList } from '@angular/fire/database';
import { SMSMessage } from '../model/SMSMessage';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import Utils from '../utils';
import { Contact } from '../model/Contact';

@Injectable({
  providedIn: 'root'
})
export class DatabaseService {

  constructor(private afDatabase: AngularFireDatabase) { }

  public saveWebToken(uid: string, token: string): Promise<any> {
    return this.afDatabase.database.ref('users')
      .child(uid)
      .child('webToken')
      .set(token);
  }

  public getLastMessages(uid: string): AngularFireList<SMSMessage> {
    return this.afDatabase.list<SMSMessage>('users/' + uid + '/lastMessages');
  }

  public getContacts(uid: string): Observable<Contact[]> {
    return this.afDatabase.object<object>('users/' + uid + '/contacts')
      .valueChanges()
      .pipe(
        map((res: object) => {
          const contactsMap = new Map<string, string>();
          if (res) {
            Object.entries(res).forEach((c: [string, string]) => {
              const name = c[1];
              const phoneNumber = Utils.cleanPhoneNumber(c[0]);
              contactsMap.set(phoneNumber, name);
            });
          }
          const contacts = new Array<Contact>();
          contactsMap.forEach((value, key) => {
            contacts.push(new Contact(value, key));
          });
          return contacts;
        })
      );
  }

  public getConversation(uid: string, thread: number): AngularFireList<SMSMessage> {
    return this.afDatabase.list<SMSMessage>('users/' + uid + '/messages/' + thread);
  }
}
