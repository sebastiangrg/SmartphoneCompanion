importScripts('https://www.gstatic.com/firebasejs/5.10.1/firebase-app.js');
importScripts('https://www.gstatic.com/firebasejs/5.10.1/firebase-messaging.js');

firebase.initializeApp({
  'messagingSenderId': '329891444276'
});

const messaging = firebase.messaging();


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