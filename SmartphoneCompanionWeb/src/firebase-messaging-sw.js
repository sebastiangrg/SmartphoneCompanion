// only on firebase hosting
importScripts('/__/firebase/5.5.6/firebase-app.js');
importScripts('/__/firebase/5.5.6/firebase-messaging.js');
importScripts('/__/firebase/init.js');

var messaging = firebase.messaging();

// function that receives the messages
messaging.setBackgroundMessageHandler(function (payload) {
  console.log('Received background message ', payload);

  var notificationTitle = 'Smartphone Companion';
  var notificationOptions = {
    body: payload.data,
    icon: '/firebase-logo.png'
  };

  // do not show a notification in case the message is the custom token used to sign in
  if (payload.data.customToken) {
    return null;
  }

  return self.registration.showNotification(notificationTitle,
    notificationOptions);
});