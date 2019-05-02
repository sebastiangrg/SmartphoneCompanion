import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';


admin.initializeApp();

function createCustomToken(auth: any): Promise<string> | null {
    if (!auth) return null;

    const uid = auth.uid;
    if (!uid) return null;

    return admin.auth().createCustomToken(uid);
}

function getWebToken(uid: string): Promise<any> {
    return admin.database().ref("/users").child(uid).child("webToken").once('value');
}

function getMobileToken(uid: string): Promise<any> {
    return admin.database().ref("/users").child(uid).child("mobileToken").once('value');
}

async function sendSyncMessage(auth: any, data: any): Promise<any> {
    if (auth) {
        const uid = auth.uid;
        const mobileToken = await getMobileToken(uid);

        if (mobileToken.exists()) {
            return admin.messaging().sendToDevice(mobileToken.val(), { data: data });
        } else {
            console.log("Mobile token error");
            return null;
        }
    } else {
        console.log("Auth error");
        return null;
    }
}

export const pairWithUID = functions.https.onCall(async (data, context) => {
    if (context.auth) {
        const webUID = data.webUID

        // check if there is a user with the scanned uid
        return admin.auth().getUser(webUID)
            .then(async (_userRecord: admin.auth.UserRecord) => {
                const customTokenPromise = createCustomToken(context.auth)
                if (customTokenPromise) {
                    const customToken = await customTokenPromise;
                    const webToken = await getWebToken(webUID);

                    return admin.messaging().sendToDevice(webToken.val(), { data: { customToken: customToken } })
                }
                return null;
            })
            .catch(err => {
                console.log(err);
                console.log("User not found for the scanned UID");
                return null;
            })
    } else {
        console.log("Auth error");
        return null;
    }
});

export const syncLastMessages = functions.https.onCall(async (data, context) => {
    return sendSyncMessage(context.auth, { syncLastMessages: "true" });
});

export const syncContacts = functions.https.onCall(async (data, context) => {
    return sendSyncMessage(context.auth, { syncContacts: "true" });
});

export const syncConversation = functions.https.onCall(async (data, context) => {
    if (data.thread) {
        return sendSyncMessage(context.auth, { syncConversation: data.thread });
    }
    return null
});