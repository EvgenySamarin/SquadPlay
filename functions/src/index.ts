/**
 * Import function triggers from their respective submodules:
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 *
 * Start writing functions
 * https://firebase.google.com/docs/functions/typescript
 */

import { setGlobalOptions } from "firebase-functions/v2";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import * as admin from "firebase-admin";

admin.initializeApp();

setGlobalOptions({
    maxInstances: 3,        // container limits for all functions
    timeoutSeconds: 15,     // default timeout
    memory: "128MiB",       // RAM config
});

export const sendEventNotification = onDocumentCreated("events/{eventId}", async (event) => {
    // here is a push logic
});
