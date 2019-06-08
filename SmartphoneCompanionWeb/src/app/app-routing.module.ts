import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ContactsComponent } from './contacts/contacts.component';
import { MainContainerComponent } from './main-container/main-container.component';
import { MessagesComponent } from './messages/messages.component';
import { AnonGuard } from './services/anon.guard';
import { AuthGuard } from './services/auth.guard';
import { SignInComponent } from './sign-in/sign-in.component';

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
