const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";
const PARTY_ID = "party_001";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validUserProfilePayload(uid, createdAt) {
  return {
    userId: uid,
    displayName: "Ana Paula",
    email: "ana@example.com",
    familyOrHostName: "Família Paula",
    phoneWhatsApp: "11988887777",
    profileRole: "PARENT",
    profileCompleted: true,
    createdAt,
    updatedAt: createdAt,
  };
}

function validPartyPayload(uid, createdAt) {
  return {
    userId: uid,
    childName: "Sofia",
    ageTurning: 6,
    title: "Aniversário de 6 Anos da Sofia",
    subtitle: "Festa no Jardim",
    eventDateIso: "2026-12-12",
    dateDisplay: "Sábado, 12 de Dezembro",
    timeDisplay: "14h00 às 18h00",
    venueName: "Espaço Girassol",
    fullAddress: "Av. das Flores, 120 - Campinas",
    rsvpDeadline: "Confirmar até 05/12",
    hostSignature: "Com carinho, Ana e Marcos",
    createdAt,
    updatedAt: createdAt,
  };
}

function validGuestPayload(uid, partyId, createdAt) {
  return {
    userId: uid,
    partyId,
    primaryGuestName: "Tia Beatriz",
    adultsCount: 2,
    childrenCount: 1,
    phoneWhatsApp: "11999990000",
    status: "CONFIRMED",
    dietaryTags: ["APLV"],
    dietaryNotes: "Sem leite de vaca",
    source: "GUEST_FORM",
    createdAt,
    updatedAt: createdAt,
  };
}

function validGiftPayload(uid, partyId, createdAt) {
  return {
    userId: uid,
    partyId,
    name: "Bicicleta Aro 16",
    category: "TOYS",
    sizeOrModelSuggestion: "Aro 16 com rodinhas",
    details: "Cor verde ou amarela",
    reservedByGuestName: "",
    createdAt,
    updatedAt: createdAt,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write profiles, parties, guests, or gifts", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 1000);
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validUserProfilePayload(ALICE_UID, now))
  );
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("parties").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("parties").doc(PARTY_ID).set(
      validPartyPayload(ALICE_UID, now)
    )
  );
});

test("Authenticated owner can create and update own UserProfile, while list and cross-user access are denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 10000);

  const aliceProfileRef = aliceDb.collection("users").doc(ALICE_UID);
  await assertSucceeds(aliceProfileRef.set(validUserProfilePayload(ALICE_UID, created)));
  await assertSucceeds(aliceProfileRef.get());

  // Listing /users is strictly denied to prevent PII scraping
  await assertFails(aliceDb.collection("users").get());

  // Updating allowed profile fields succeeds while createdAt stays intact
  await assertSucceeds(
    aliceProfileRef.update({
      familyOrHostName: "Com carinho, Família Paula",
      profileRole: "ORGANIZER",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Invalid role or ghost field fails
  await assertFails(
    aliceProfileRef.update({
      profileRole: "ADMIN",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Cross-user profile read/write denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).set(validUserProfilePayload(ALICE_UID, created))
  );
});

test("Authenticated owner can create, read, and update party, and cross-user access is denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 10000);

  const partyRef = aliceDb.collection("users").doc(ALICE_UID).collection("parties").doc(PARTY_ID);
  await assertSucceeds(partyRef.set(validPartyPayload(ALICE_UID, created)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("parties").get());

  // Partial update succeeds while preserving createdAt
  await assertSucceeds(
    partyRef.update({
      title: "Aniversário de 6 Anos da Sofia (Atualizado)",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Immutable createdAt mutation fails
  await assertFails(
    partyRef.update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Ghost field injection fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("parties").doc("party_ghost").set({
      ...validPartyPayload(ALICE_UID, created),
      isAdmin: true,
    })
  );

  // Cross-user read & write fails
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("parties").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("parties").doc(PARTY_ID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("parties").doc(PARTY_ID).update({
      title: "Hijacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Guests subcollection enforces parent party existence, schema bounds, and owner isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  // Orphan guest without parent party must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("parties").doc("missing_party")
      .collection("guests").doc("gst_1").set(validGuestPayload(ALICE_UID, "missing_party", created))
  );

  // Create parent party first
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("parties").doc(PARTY_ID)
      .set(validPartyPayload(ALICE_UID, created))
  );

  const guestRef = aliceDb.collection("users").doc(ALICE_UID).collection("parties").doc(PARTY_ID)
    .collection("guests").doc("gst_1");
  await assertSucceeds(guestRef.set(validGuestPayload(ALICE_UID, PARTY_ID, created)));
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("parties").doc(PARTY_ID)
      .collection("guests").get()
  );

  // Update status succeeds
  await assertSucceeds(
    guestRef.update({
      status: "DECLINED",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Invalid status enum fails
  await assertFails(
    guestRef.update({
      status: "MAYBE",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Gifts subcollection enforces anti-duplicate lock on reservation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("parties").doc(PARTY_ID)
      .set(validPartyPayload(ALICE_UID, created))
  );

  const giftRef = aliceDb.collection("users").doc(ALICE_UID).collection("parties").doc(PARTY_ID)
    .collection("gifts").doc("gft_1");
  await assertSucceeds(giftRef.set(validGiftPayload(ALICE_UID, PARTY_ID, created)));

  // Reserve empty gift succeeds
  await assertSucceeds(
    giftRef.update({
      reservedByGuestName: "Tia Beatriz",
      updatedAt: new Date(Date.now() - 2000),
    })
  );

  // Overwriting an already reserved gift directly with another guest name fails
  await assertFails(
    giftRef.update({
      reservedByGuestName: "Outro Convidado",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Releasing a reserved gift back to empty string succeeds
  await assertSucceeds(
    giftRef.update({
      reservedByGuestName: "",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});
