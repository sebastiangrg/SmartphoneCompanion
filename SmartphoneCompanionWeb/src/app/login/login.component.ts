import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { User } from 'firebase';

import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  user$: Observable<User>;
  idToken: string;

  constructor(private authService: AuthService) { }

  ngOnInit() {
    this.authService.loginAnonymously();
    this.user$ = this.authService.getUser()
      .pipe(
        tap((user: User) => {
          console.log(user);
          user.getIdToken()
            .then((token: string) => {
              this.idToken = token;
            });
        })
      );
  }

}
