const functions = require("firebase-functions");
const admin = require("firebase-admin");
const { Options, IntegrationApiKeys, Environment, WebpayPlus } = require("transbank-sdk");

admin.initializeApp();

// 🔥 CONFIG NUEVA
const tx = new WebpayPlus.Transaction(new Options(
  IntegrationApiKeys.WEBPAY,
  Environment.Integration
));

// ===============================
exports.crearPago = functions.https.onRequest(async (req, res) => {

  try {
    const { amount, bookingId } = req.body;

    const response = await tx.create(
      bookingId,
      "session123",
      amount,
      `https://us-central1-${process.env.GCLOUD_PROJECT}.cloudfunctions.net/confirmarPago`
    );

    res.json(response);

  } catch (error) {
    console.error(error);
    res.status(500).send(error.toString());
  }
});

// ===============================
exports.confirmarPago = functions.https.onRequest(async (req, res) => {

  const token = req.query.token_ws;

  try {

    const response = await tx.commit(token);

    if (response.status === "AUTHORIZED") {

      await admin.firestore()
        .collection("garage")
        .doc("bookingInformation")
        .collection("bookings")
        .doc(response.buy_order)
        .update({
          paid: true,
          paymentMethod: "webpay"
        });

      res.send("✅ Pago exitoso");

    } else {
      res.send("❌ Pago rechazado");
    }

  } catch (error) {
    console.error(error);
    res.status(500).send(error.toString());
  }
});