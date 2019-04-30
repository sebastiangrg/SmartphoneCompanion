import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';


admin.initializeApp();

function createCustomToken(auth: any) {
    if (!auth) return null;

    const uid = auth.uid;
    if (!uid) return null;

    return admin.auth().createCustomToken(uid);
}

function getWebToken(uid: string) {
    return admin.database().ref("/users").child(uid).child("webToken").once('value');
}

function getMobileToken(uid: string) {
    return admin.database().ref("/users").child(uid).child("mobileToken").once('value');
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

export const syncConversations = functions.https.onCall(async (data, context) => {
    if (context.auth) {
        const uid = context.auth.uid;
        const mobileToken = await getMobileToken(uid);

        const sync = "conversations";

        if (mobileToken.exists()) {
            return admin.messaging().sendToDevice(mobileToken.val(), { data: { sync: sync } });
        } else {
            console.log("Mobile token error");
            return null;
        }
    } else {
        console.log("Auth error");
        return null;
    }
});