import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';


admin.initializeApp();

function createCustomToken(auth: any) {
    if (!auth) return null;

    const uid = auth.uid;
    if (!uid) return null;

    return admin.auth().createCustomToken(uid);
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
                    return admin.database().ref("/users").child(webUID).child("token").set(customToken);
                }
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