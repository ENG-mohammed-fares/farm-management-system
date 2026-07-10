package com.smartfarm;

import java.time.LocalDate;
import java.util.List;

import com.smartfarm.dao.FarmWorkerDAO;
import com.smartfarm.dao.UserDAO;
import com.smartfarm.model.Crop;
import com.smartfarm.model.FarmWorker;
import com.smartfarm.model.Field;
import com.smartfarm.model.Harvest;
import com.smartfarm.model.Transaction;
import com.smartfarm.model.User;
import com.smartfarm.service.AuthService;
import com.smartfarm.service.FarmService;
import com.smartfarm.service.TransactionService;
import com.smartfarm.service.WorkerService;
import com.smartfarm.util.DatabaseConnection;
import com.smartfarm.util.PasswordHasher;
import com.smartfarm.util.SessionManager;
import com.smartfarm.util.VerificationService;

/**
 * Integration test suite for the university Smart Farm project.
 * Run: mvn -q exec:java -Dexec.mainClass=com.smartfarm.IntegrationTest
 */
public class IntegrationTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== Smart Farm Integration Tests ===\n");

        try {
            testDbConnection();
            testAdminLogin();
            testWorkerLogin();
            testInactiveWorkerBlocked();
            testPasswordHashing();
            testVerificationCode();
            testSessionLogout();
            testFieldsAndCrops();
            testWorkersList();
            testHarvestFlow();
            testTransactions();
            testProfileUpdate();
            testFinancialSummary();
            testSignupValidation();
        } catch (Exception e) {
            fail("Unhandled exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnection.close();
        }

        System.out.println("\n=== RESULT: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testDbConnection() {
        check("DB connection", DatabaseConnection.testConnection());
    }

    private static void testAdminLogin() throws Exception {
        int[] result = UserDAO.login("admin", "12345");
        check("Admin login by name", result != null && result[1] == 1);

        result = UserDAO.login("fff@gmail.com", "12345");
        check("Admin login by email", result != null && result[1] == 1);

        AuthService.AuthResult auth = AuthService.login("admin", "12345");
        check("AuthService admin login", auth.success && auth.isAdmin);
        check("Session is admin", SessionManager.isAdmin() && SessionManager.getUserId() > 0);
        AuthService.logout();
    }

    private static void testWorkerLogin() throws Exception {
        List<FarmWorker> workers = FarmWorkerDAO.getAllWorkers();
        FarmWorker active = workers.stream().filter(FarmWorker::isActive).findFirst().orElse(null);
        check("Has at least one active worker", active != null);
        if (active == null) return;

        User user = UserDAO.getUserById(active.getUserId());
        check("Worker user exists", user != null);
        if (user == null) return;

        // Workers may still have plaintext passwords in this university DB seed
        String passwordGuess = null;
        for (String candidate : List.of("123456", "Malek1234567.", "GameDev123@", "Khara1234.")) {
            int[] login = UserDAO.login(user.getName(), candidate);
            if (login != null) {
                passwordGuess = candidate;
                break;
            }
        }

        if (passwordGuess == null) {
            // Create a disposable test worker with known password
            String name = "itest_worker_" + System.currentTimeMillis();
            String email = name + "@test.local";
            String pass = "TestPass1!";
            int userId = UserDAO.createWorker(name, email, null, pass);
            FarmWorkerDAO.assignWorker(userId, "HARVESTER", 8.0, "kg");
            AuthService.AuthResult auth = AuthService.login(email, pass);
            check("AuthService worker login (fresh)", auth.success && !auth.isAdmin);
            check("Worker session has fwId", SessionManager.getFwId() > 0);
            AuthService.logout();
        } else {
            AuthService.AuthResult auth = AuthService.login(user.getName(), passwordGuess);
            check("AuthService worker login", auth.success && !auth.isAdmin);
            check("Worker session has fwId", SessionManager.getFwId() > 0);
            AuthService.logout();
        }
    }

    private static void testInactiveWorkerBlocked() throws Exception {
        List<FarmWorker> workers = FarmWorkerDAO.getAllWorkers();
        FarmWorker inactive = workers.stream().filter(w -> !w.isActive()).findFirst().orElse(null);
        if (inactive == null) {
            System.out.println("SKIP inactive-worker-blocked (no inactive worker in DB)");
            return;
        }
        User user = UserDAO.getUserById(inactive.getUserId());
        if (user == null) {
            fail("Inactive worker user missing");
            return;
        }

        // Find a password that matches (plaintext seed or known)
        String pass = null;
        for (String candidate : List.of("123456", "m", "Malek1234567.", "GameDev123@", "Khara1234.", "Mmm2005.", "Mohammed2005.", "Engyamen2005.")) {
            int[] login = UserDAO.login(user.getName(), candidate);
            if (login != null) {
                pass = candidate;
                break;
            }
        }
        if (pass == null) {
            System.out.println("SKIP inactive-worker-blocked (unknown password for " + user.getName() + ")");
            return;
        }

        AuthService.AuthResult auth = AuthService.login(user.getName(), pass);
        check("Inactive worker cannot open worker session", !auth.success);
        check("Session cleared after inactive login attempt", SessionManager.getUserId() == -1);
    }

    private static void testPasswordHashing() {
        String hash = PasswordHasher.hash("TestPass1!");
        check("BCrypt hash created", PasswordHasher.isHashed(hash));
        check("BCrypt verify correct", PasswordHasher.verify("TestPass1!", hash));
        check("BCrypt reject wrong", !PasswordHasher.verify("WrongPass1!", hash));
        check("Plaintext verify still works for seed data", PasswordHasher.verify("12345", "12345"));
    }

    private static void testVerificationCode() {
        String key = "itest@example.com";
        String code = VerificationService.generateUniqueCode(key);
        check("OTP length is 5", code != null && code.length() == 5);
        check("OTP verify success", VerificationService.verifyCode(key, code));
        check("OTP single-use", !VerificationService.verifyCode(key, code));
    }

    private static void testSessionLogout() {
        SessionManager.login(99, "temp", true);
        SessionManager.setFwId(5);
        AuthService.logout();
        check("Logout clears userId", SessionManager.getUserId() == -1);
        check("Logout clears fwId", SessionManager.getFwId() == -1);
        check("Logout clears admin flag", !SessionManager.isAdmin());
    }

    private static void testFieldsAndCrops() {
        List<Field> fields = FarmService.getAllFields();
        check("Fields load", fields != null && !fields.isEmpty());
        check("Field count > 0", FarmService.getTotalFieldsCount() > 0);
        check("Total area > 0", FarmService.getTotalDunums() > 0);

        List<Crop> crops = FarmService.getAllCrops();
        check("Crops load", crops != null && !crops.isEmpty());
        check("Active crops count", FarmService.getActiveCropsCount() >= 0);

        String fieldName = "ITEST_Field_" + System.currentTimeMillis();
        FarmService.Result addField = FarmService.addField(fieldName, "100", "Square Meter (m\u00B2)", "GOOD", "Test Loc");
        check("Add field", addField.success);

        Field created = FarmService.getAllFields().stream()
                .filter(f -> fieldName.equals(f.getName()))
                .findFirst().orElse(null);
        check("Created field found", created != null);

        if (created != null) {
            FarmService.Result addCrop = FarmService.addCrop(created.getFieldId(), "ITEST_Crop", "VEGETABLE", LocalDate.now(), "10 kg");
            check("Add crop", addCrop.success);

            Crop crop = FarmService.getCropsByField(created.getFieldId()).stream()
                    .filter(c -> "ITEST_Crop".equals(c.getName()))
                    .findFirst().orElse(null);
            check("Created crop found", crop != null);

            if (crop != null) {
                FarmService.Result delCrop = FarmService.deleteCrop(crop.getCropId());
                check("Delete crop", delCrop.success);
            }

            FarmService.Result delField = FarmService.deleteField(created.getFieldId());
            check("Delete empty field", delField.success);
        }
    }

    private static void testWorkersList() {
        List<FarmWorker> workers = WorkerService.getAllWorkers();
        check("Workers load", workers != null && !workers.isEmpty());
        check("Total workers count", WorkerService.getTotalWorkersCount() > 0);
        check("Active workers count", WorkerService.getActiveWorkersCount() > 0);
    }

    private static void testHarvestFlow() throws Exception {
        List<Field> fields = FarmService.getAllFields();
        List<Crop> crops = FarmService.getAllCrops();
        List<FarmWorker> workers = WorkerService.getAllWorkers().stream().filter(FarmWorker::isActive).toList();
        check("Harvest prerequisites exist", !fields.isEmpty() && !crops.isEmpty() && !workers.isEmpty());
        if (fields.isEmpty() || crops.isEmpty() || workers.isEmpty()) return;

        Crop crop = crops.get(0);
        FarmWorker worker = workers.get(0);

        WorkerService.Result submit = WorkerService.submitHarvest(
                crop.getFieldId(), crop.getCropId(), worker.getFwId(),
                "1.5", "0.2", "kg", "integration-test");
        check("Submit harvest", submit.success);

        List<Harvest> pending = WorkerService.getHarvestsByStatus("PENDING");
        Harvest mine = pending.stream()
                .filter(h -> "integration-test".equals(h.getNotes()) && h.getFwId() == worker.getFwId())
                .findFirst().orElse(null);
        check("Pending harvest visible", mine != null);

        if (mine != null) {
            WorkerService.Result approve = WorkerService.approveHarvest(mine.getHarvestId());
            check("Approve harvest", approve.success);

            List<Harvest> approved = WorkerService.getHarvestsByWorker(worker.getFwId());
            boolean found = approved.stream().anyMatch(h -> h.getHarvestId() == mine.getHarvestId()
                    && "APPROVED".equals(h.getStatus()));
            check("Approved harvest status", found);
        }
    }

    private static void testTransactions() {
        TransactionService.Result sale = TransactionService.recordSale(12.5, "ITEST sale", null);
        check("Record sale", sale.success);

        TransactionService.Result purchase = TransactionService.recordPurchase(5.0, "ITEST purchase");
        check("Record purchase", purchase.success);

        List<FarmWorker> workers = WorkerService.getAllWorkers().stream().filter(FarmWorker::isActive).toList();
        if (!workers.isEmpty()) {
            TransactionService.Result pay = TransactionService.recordPayment(3.0, "ITEST payment", workers.get(0).getUserId());
            check("Record payment", pay.success);
        }

        List<Transaction> all = TransactionService.getAllTransactions();
        check("Transactions load", all != null && !all.isEmpty());

        Transaction itest = all.stream().filter(t -> t.getDescription() != null && t.getDescription().startsWith("ITEST")).findFirst().orElse(null);
        if (itest != null) {
            TransactionService.Result del = TransactionService.deleteTransaction(itest.getTransactionId());
            check("Delete transaction", del.success);
        }
    }

    private static void testProfileUpdate() {
        AuthService.AuthResult login = AuthService.login("admin", "12345");
        check("Login before profile update", login.success);
        if (!login.success) return;

        int userId = SessionManager.getUserId();
        User before = AuthService.getUserById(userId);
        check("Load admin profile", before != null);

        String originalName = before.getName();
        String originalEmail = before.getEmail();
        String originalPhone = before.getPhone();

        AuthService.AuthResult updated = AuthService.updateProfile(userId, originalName, originalEmail, originalPhone);
        check("Profile update same values", updated.success);

        AuthService.AuthResult badEmail = AuthService.updateProfile(userId, originalName, "not-an-email", originalPhone);
        check("Reject invalid email", !badEmail.success);

        AuthService.logout();
    }

    private static void testFinancialSummary() {
        TransactionService.FinancialSummary summary = TransactionService.getFinancialSummary();
        check("Financial summary loads", summary != null);
        check("Net profit = revenue - expenses",
                Math.abs(summary.netProfit - (summary.revenue - summary.expenses)) < 0.001);
    }

    private static void testSignupValidation() {
        check("Weak password rejected", AuthService.validatePasswordStrength("123") != null);
        check("Strong password accepted", AuthService.validatePasswordStrength("Strong1!") == null);
        check("Email validation", AuthService.isValidEmail("a@b.com"));
        check("Bad email rejected", !AuthService.isValidEmail("abc"));
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }

    private static void fail(String name) {
        check(name, false);
    }
}
