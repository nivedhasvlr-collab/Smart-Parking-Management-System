public class ParkingQA {

    static void assertRule(int testCaseId, String description, String outputLog, String expectedValueKeyword) {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("Test Case " + testCaseId + ": " + description);
        System.out.println("  System Output Response: " + outputLog);
        
        if (outputLog.toLowerCase().contains(expectedValueKeyword.toLowerCase())) {
            System.out.println("  VERIFICATION STATUS: [ PASS ] ✅");
        } else {
            System.out.println("  VERIFICATION STATUS: [ FAIL ] ❌");
        }
    }

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("        SMART PARKING MANAGEMENT PLATFORM: SYSTEM QA RUNNER         ");
        System.out.println("======================================================================\n");

        long baselineClock = System.currentTimeMillis();
        long oneHourMs = 1000 * 60 * 60;

        // 1. Full Parking Lot Scenario Execution Validation
        ParkingManagement pm1 = new ParkingManagement();
        pm1.registerVehicleEntry("TN-01-A-1111", "Bike", false, baselineClock); // Consumes single slot B1
        String r1 = pm1.registerVehicleEntry("TN-01-A-2222", "Bike", false, baselineClock);
        assertRule(1, "Verify system block processing logic when parking sectors hit capacity", r1, "Full");

        // 2. Wrong Vehicle-Slot Matching Allocation Edge Case Validation
        ParkingManagement pm2 = new ParkingManagement();
        String r2 = pm2.registerVehicleEntry("TN-01-B-9999", "Rocket-Ship", false, baselineClock);
        assertRule(2, "Intercept and refuse unknown custom unmapped vehicle profiles configurations", r2, "Unsupported");

        // 3. Duplicate Vehicle Protection Loop Validation
        ParkingManagement pm3 = new ParkingManagement();
        pm3.registerVehicleEntry("DL-03-C-1234", "Car", false, baselineClock);
        String r3 = pm3.registerVehicleEntry("DL-03-C-1234", "Car", false, baselineClock);
        assertRule(3, "Reject secondary duplicate entity entry if token plate tag is active inside lot", r3, "Duplicate");

        // 4. Lost Ticket Heavy Fine Addition Assessment
        ParkingManagement pm4 = new ParkingManagement();
        String entry4 = pm4.registerVehicleEntry("MH-02-X-5555", "Car", false, baselineClock);
        String ticketId4 = entry4.split("TicketID: ")[1];
        String r4 = pm4.registerVehicleExit(ticketId4, baselineClock + (oneHourMs * 2), true, false); 
        assertRule(4, "Apply flat penal charge penalty addition structure to lost documentation claims", r4, "Rs. 580.00"); // (2hr*40) + 500 = 580

        // 5. Early Exit Minimum Billing Protection
        ParkingManagement pm5 = new ParkingManagement();
        String entry5 = pm5.registerVehicleEntry("KA-51-M-4444", "SUV", false, baselineClock);
        String ticketId5 = entry5.split("TicketID: ")[1];
        String r5 = pm5.registerVehicleExit(ticketId5, baselineClock + (1000 * 60 * 5), false, false); // 5 mins exit
        assertRule(5, "Enforce baseline integer round up of 1 hour pricing metric on rapid checkout", r5, "Rs. 60.00"); // Minimum 1 hr charge = 60

        // 6. Overnight Multi-Day Parking Duration Verification
        ParkingManagement pm6 = new ParkingManagement();
        String entry6 = pm6.registerVehicleEntry("KA-03-Z-7777", "Truck", false, baselineClock);
        String ticketId6 = entry6.split("TicketID: ")[1];
        String r6 = pm6.registerVehicleExit(ticketId6, baselineClock + (oneHourMs * 26), false, false); 
        assertRule(6, "Scale billing arrays accurately along overnight or extensive holding horizons", r6, "Rs. 2600.00"); // 26 * 100 = 2600

        // 7. Peak-Hour Pricing Multiplier Application Evaluation
        ParkingManagement pm7 = new ParkingManagement();
        String entry7 = pm7.registerVehicleEntry("TS-09-E-8888", "Car", false, baselineClock);
        String ticketId7 = entry7.split("TicketID: ")[1];
        String r7 = pm7.registerVehicleExit(ticketId7, baselineClock + (oneHourMs * 2), false, true); 
        assertRule(7, "Enforce dynamic inflation markup coefficient during rush velocity spikes", r7, "Rs. 120.00"); // (2hr*40)*1.5 = 120

        // 8. EV Charging infrastructure Charge Processing
        ParkingManagement pm8 = new ParkingManagement();
        String entry8 = pm8.registerVehicleEntry("EV-LOT-9999", "EV", false, baselineClock);
        String ticketId8 = entry8.split("TicketID: ")[1];
        String r8 = pm8.registerVehicleExit(ticketId8, baselineClock + (oneHourMs * 2), false, false); 
        assertRule(8, "Add utility premium values automatically for EV grid infrastructure charges", r8, "Rs. 250.00"); // (2hr*50) + 150 = 250
    }
}
