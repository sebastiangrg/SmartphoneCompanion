import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { MainContainerComponent } from './main-container/main-container.component';
import { AuthGuard } from './services/auth.guard';
import { AnonGuard } from './services/anon.guard';
import { SignInComponent } from './sign-in/sign-in.component';
import { MessagesComponent } from './messages/messages.component';
import { ContactsComponent } from './contacts/contacts.component';

const routes: Routes = [
  {
    path: '',
    component: MainContainerComponent,
    canActivate: [AuthGuard],
    children: [
      {
        path: 'messages',
        component: MessagesComponent
      },
      {
        path: 'contacts',
        component: ContactsComponent
      },
      {
        path: '',
        redirectTo: 'messages',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: 'signIn',
    component: SignInComponent,
    canActivate: [AnonGuard]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
