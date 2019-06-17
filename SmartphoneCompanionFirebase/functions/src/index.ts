import * as admin from 'firebase-admin';
import * as functions from 'firebase-functions';


const FCM_OPERATION = {
    SYNC_LAST_MESSAGES: "1",
    SYNC_CONVERSATION: "2",
    SYNC_CONTACTS: "3",
    SEND_SMS: "4",
    SYNC_CALL_LOG: "5"
};

admin.initializeApp();

function createCustomToken(auth: any): Promise<string> | null {
    if (!auth || !auth.uid) return Promise.reject("Authentication error");

    return admin.auth().createCustomToken(auth.uid);
}

function getWebToken(uid: string): Promise<any> {
    return admin.database()
        .ref("/users")
        .child(uid)
        .child("webToken")
        .once('value');
}

function getMobileToken(uid: string): Promise<any> {
    return admin.database()
        .ref("/users")
        .child(uid)
        .child("mobileToken")
        .once('value');
}

async function sendSyncMessage(auth: any, data: any): Promise<any> {
    if (!auth || !auth.uid) {
        return Promise.reject("Authentication error")
    }

    return getMobileToken(auth.uid)
        .then((mobileToken: any) => {
            if (mobileToken.exists()) {
                return Promise.resolve(mobileToken.val());
            }
            return Promise.reject("Mobile Token error");
        })
        .then((mobileToken: string) => {
            return admin.messaging().sendToDevice(mobileToken, { data: data });
        });
}

export const pairWithUID = functions.https.onCall(async (data, context) => {
    if (!context.auth || !data.webUID) {
        return Promise.reject("Authentication error");
    }

    // check if there is a user with the scanned uid
    return admin.auth().getUser(data.webUID)
        .then(async (_: admin.auth.UserRecord) => {
            return Promise.all([createCustomToken(context.auth), getWebToken(data.webUID)])
                .then((values: any[]) => {
                    const customToken = values[0];
                    const webToken = values[1];

                    return admin.messaging().sendToDevice(webToken.val(), { data: { customToken: customToken } });
                })
        })
        .catch(_ => {
            return Promise.reject("User not found for the scanned UID");
        });
});

export const syncLastMessages = functions.https.onCall(async (_, context) => {
    return sendSyncMessage(context.auth, { operation: FCM_OPERATION.SYNC_LAST_MESSAGES });
});

export const syncContacts = functions.https.onCall(async (_, context) => {
    return sendSyncMessage(context.auth, { operation: FCM_OPERATION.SYNC_CONTACTS });
});

export const syncCallLog = functions.https.onCall(async (_, context) => {
    return sendSyncMessage(context.auth, { operation: FCM_OPERATION.SYNC_CALL_LOG })
})

export const syncConversation = functions.https.onCall(async (data, context) => {
    if (!data.thread) {
        return Promise.reject("Thread id not provided");
    }
    return sendSyncMessage(context.auth, { operation: FCM_OPERATION.SYNC_CONVERSATION, thread: data.thread.toString() });
});

export const sendSMSMessage = functions.https.onCall(async (data, context) => {
    if (!data.phoneNumber) {
        return Promise.reject("Phone number not provided");
    }
    if (!data.content) {
        return Promise.reject("Message content not provided");
    }
    return sendSyncMessage(context.auth, { operation: FCM_OPERATION.SEND_SMS, phoneNumber: data.phoneNumber, content: data.content });
});

export const deleteMobileToken = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        return Promise.reject("Authentication error");
    }
    return admin.database().ref('users').child(context.auth.uid).child('mobileToken').remove();
});

// DEBUGGING ONLY
export const deleteAllUsers = functions.https.onCall(async (data, context) => {
    return admin.auth().listUsers(1000).then(res => {
        const promises = res.users.map(user => admin.auth().deleteUser(user.uid));
        return Promise.all(promises)
    });
});

export const deleteUserData = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        return Promise.reject("Authentication error");
    }
    return admin.database().ref('users').child(context.auth.uid).remove();
});