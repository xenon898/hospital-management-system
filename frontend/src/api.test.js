import assert from "node:assert/strict";
import test from "node:test";
import { ageValidationMessage, phoneValidationMessage } from "./api.js";

test("phone validation rejects unrealistic mobile numbers", () => {
  assert.equal(phoneValidationMessage("9999999999"), "Phone number is too predictable. Enter a real patient mobile number.");
  assert.equal(phoneValidationMessage("1111111111"), "Phone number is too predictable. Enter a real patient mobile number.");
  assert.equal(phoneValidationMessage("1234567890"), "Phone must be exactly 10 digits and start with 6, 7, 8, or 9.");
  assert.equal(phoneValidationMessage("5123456789"), "Phone must be exactly 10 digits and start with 6, 7, 8, or 9.");
  assert.equal(phoneValidationMessage("9876543211"), "");
});

test("age validation rejects non-realistic patient ages", () => {
  assert.equal(ageValidationMessage(0), "Age must be greater than 0.");
  assert.equal(ageValidationMessage(-1), "Age must be greater than 0.");
  assert.equal(ageValidationMessage(121), "Invalid age entered.");
  assert.equal(ageValidationMessage(35), "");
});
