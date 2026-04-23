# Add Owner Functionality - Detailed Bug Report

## Report Metadata
- **Project:** `petclinic-api-tests`
- **Area:** Owner API (`POST /petclinic/api/owners`)
- **Test source:** `src/test/java/com/collaboration/petclinic/api/tests/OwnerAPITestWithAI.java`
- **Base API configuration:** `src/test/java/com/collaboration/petclinic/api/base/BaseTest.java`
  - `baseURI = http://localhost`
  - `port = 9966`
  - `basePath = /petclinic/api`
- **Execution context:** IntelliJ JUnit run of `OwnerAPITestWithAI`
- **Observed failure pattern:** several tests expected `400`/`409` but API returned `201`

---

## Executive Summary
The Add Owner endpoint accepts invalid payloads that should be rejected based on the provided business rules.

### Business rules provided
1. Required fields: `firstName`, `lastName`, `address`, `city`, `telephone`
2. `firstName` and `lastName`: letters only
3. `address`: letters, numbers, special characters allowed
4. `telephone`: numbers only

### Confirmed defects from failed tests
- Required fields are not consistently enforced for blank/whitespace values.
- Name format validation (letters-only) is not enforced for first and last name.
- Duplicate owner creation is currently accepted.

---

## BUG-1 - Required fields accept blank/whitespace values

- **ID:** `BUG-1`
- **Severity:** High
- **Status:** Confirmed by failing automated test
- **Related test:** `shouldRejectWhenRequiredFieldIsBlankOrWhitespace`
- **Location:** `src/test/java/com/collaboration/petclinic/api/tests/OwnerAPITestWithAI.java:153`
- **Observed assertion failure:**
  - Expected status: `400`
  - Actual status: `201`

### Why this is a bug
Required fields must not accept blank (`""`) or whitespace-only (`"   "`) values. Accepting such values creates logically incomplete owner records.

### Impact
- Data quality degradation in owner records.
- UI and downstream services may display empty-looking mandatory values.
- Increases risk of unusable or duplicate-like data.

### Likely root cause
Validation likely checks only null/missing (`@NotNull`) and not blank content (`@NotBlank` or trim-aware checks).

### Recommendation
- Enforce non-blank validation for all required string fields.
- Ensure validation also rejects whitespace-only strings.
- Return `400 Bad Request` with field-level error details.

---

## BUG-2 - `firstName` letters-only rule is not enforced

- **ID:** `BUG-2`
- **Severity:** High
- **Status:** Confirmed by failing automated test
- **Related test:** `shouldRejectWhenFirstNameIsNotLettersOnly`
- **Location:** `src/test/java/com/collaboration/petclinic/api/tests/OwnerAPITestWithAI.java:161`
- **Observed assertion failure:**
  - Expected status: `400`
  - Actual status: `201`

### Why this is a bug
The agreed validation rule is that first name must contain letters only. The endpoint accepted values that should violate this rule.

### Impact
- Invalid identity data stored in the system.
- Inconsistent search/filter/reporting behavior if non-letter characters are accepted unpredictably.

### Likely root cause
Missing or permissive regex/validator for `firstName` in request DTO/entity validation.

### Recommendation
- Add strict letters-only validation for `firstName`.
- Keep rule behavior explicit in API documentation and error responses.
- Add unit/integration tests around boundary cases and invalid character sets.

---

## BUG-3 - `lastName` letters-only rule is not enforced

- **ID:** `BUG-3`
- **Severity:** High
- **Status:** Confirmed by failing automated test
- **Related test:** `shouldRejectWhenLastNameIsNotLettersOnly`
- **Location:** `src/test/java/com/collaboration/petclinic/api/tests/OwnerAPITestWithAI.java:169`
- **Observed assertion failure:**
  - Expected status: `400`
  - Actual status: `201`

### Why this is a bug
The same letters-only rule applies to last name. API acceptance of invalid characters violates this requirement.

### Impact
- Last-name data integrity issues.
- Increased risk of poor matching, duplicate handling, and inconsistent display.

### Likely root cause
Missing or permissive regex/validator for `lastName`.

### Recommendation
- Apply the same strict validation strategy used for `firstName` to `lastName`.
- Return deterministic `400` responses for validation failures.

---

## BUG-4 - Duplicate owner creation is allowed

- **ID:** `BUG-4`
- **Severity:** Medium-High (depends on product policy)
- **Status:** Confirmed by failing automated test
- **Related test:** `shouldHandleDuplicateCreateRequestConsistently`
- **Location:** `src/test/java/com/collaboration/petclinic/api/tests/OwnerAPITestWithAI.java:232`
- **Observed assertion failure:**
  - Expected status: `400` or `409`
  - Actual status: `201` (second create request)

### Why this is a bug
If duplicate owner creation should be prevented, the second identical create request must be rejected.

### Impact
- Duplicate records in production data.
- Confusing user behavior and potential issues in owner lookup/workflows.

### Likely root cause
No uniqueness/business-rule check at service level and/or no database uniqueness constraints for chosen duplicate key strategy.

### Recommendation
- Define explicit duplicate policy (which fields define identity for duplicate detection).
- Enforce duplicate protection in service + persistence layer.
- Return clear conflict/validation response (`409` or `400`, consistently).

---

## Reproduction Notes
1. Start Spring Petclinic REST backend at `http://localhost:9966/petclinic/api`.
2. Run `OwnerAPITestWithAI` from IntelliJ JUnit runner.
3. Observe failures where expected `400/409` are returned as `201`.

### Scope note
Repeated stack traces in output are mainly from parameterized test iterations and represent repeated manifestations of the same core defects.

---

## Suggested Ticket Split
- **Ticket 1:** `BUG-1` Required field non-blank enforcement
- **Ticket 2:** `BUG-2`/`BUG-3` Name letters-only validation enforcement
- **Ticket 3:** `BUG-4` Duplicate owner policy + enforcement

---

## Exit Criteria for Fix Verification
- All `OwnerAPITestWithAI` validation tests pass for `POST /owners`.
- Invalid create requests return `400` with stable error structure.
- Duplicate scenario returns consistent rejection status based on agreed policy.
- No invalid payload creates persisted records.

