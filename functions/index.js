const functions = require("firebase-functions");

exports.validateCollegeDomain = functions.https.onCall(async (data) => {
  const email = (data.email || "").toLowerCase().trim();
  const allowedDomain = process.env.COLLEGE_DOMAIN || "college.edu";
  const allowed = email.endsWith(`@${allowedDomain}`);

  return {
    allowed,
    domain: allowedDomain,
  };
});

