import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';


admin.initializeApp();

function createCustomToken(context: functions.EventContext) {
    const auth = context.auth;
    if (!auth) return null;

    const uid = auth.uid;
    if (!uid) return null;

    return admin.auth().createCustomToken(uid);
}

export const linkUsers = functions.database.ref("/users/{userId}/webUID").onCreate(async (snapshot, context) => {
    const customTokenPromise = createCustomToken(context)
    if (customTokenPromise) {
        const customToken = await customTokenPromise;

        const webUID = snapshot.val();

        // remove the uid
        admin.database().ref("/users/" + context.params.userId + "/webUID").remove()
            .then()
            .catch(err => {
                console.log(err)
            });

        // set the token
        return admin.database().ref("/users").child(webUID).child("token").set(customToken);
    }
    return null;
});