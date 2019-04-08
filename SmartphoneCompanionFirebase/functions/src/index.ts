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
        if (webUID) {
            const customTokenPromise = createCustomToken(context.auth)
            if (customTokenPromise) {
                const customToken = await customTokenPromise;
                return admin.database().ref("/users").child(webUID).child("token").set(customToken);
            }
        }
        console.log("Token creation error");
        return null;
    } else {
        console.log("Auth error");
        return null;
    }
});