import {setGlobalOptions} from "firebase-functions/v2";
import {onDocumentCreated} from "firebase-functions/v2/firestore";
import * as admin from "firebase-admin";
import * as logger from "firebase-functions/logger";

admin.initializeApp();

setGlobalOptions({
  maxInstances: 3,
  timeoutSeconds: 15,
  memory: "128MiB",
  enforceAppCheck: true,
});

export const sendEventNotification = onDocumentCreated(
  "events/{eventId}",
  async (event) => {
    const eventSnapshot = event.data;
    if (!eventSnapshot) {
      logger.warn("No snapshot data found for event creation trigger.");
      return;
    }

    const eventData = eventSnapshot.data();
    const eventId = event.params.eventId;
    const groupId = eventData.groupId;
    const creatorId = eventData.creatorId;
    const title = eventData.title;

    if (!groupId) {
      logger.error("Event missing groupId. Cannot dispatch notification.", {
        eventId,
      });
      return;
    }

    let groupTitle = "Squad";
    try {
      const groupDoc = await admin
        .firestore()
        .collection("groups")
        .doc(groupId)
        .get();

      if (groupDoc.exists) {
        groupTitle = groupDoc.data()?.title || "Squad";
      } else {
        logger.warn(`Group ${groupId} not found, fallback to default title.`);
      }
    } catch (err) {
      logger.error(`Failed to fetch group ${groupId}:`, err);
    }

    const notificationTitle = `New event in ${groupTitle}`;
    const notificationBody = title || "A new event was created";

    const message: admin.messaging.Message = {
      topic: groupId,
      notification: {
        title: notificationTitle,
        body: notificationBody,
      },
      data: {
        eventId: eventId || "",
        groupId: groupId,
        creatorId: creatorId || "",
      },
    };

    try {
      const messageId = await admin.messaging().send(message);
      logger.info(`Successfully sent event push notification: ${messageId}`, {
        eventId,
        groupId,
        creatorId,
      });
    } catch (err) {
      logger.error("Error sending push notification to topic:", {
        groupId,
        error: err,
      });
    }
  },
);
