import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Ticket {
    String ticketId;
    String licensePlate;
    String vehicleType;
    String slotId;
    long entryTime; 
    boolean isVIP;
    boolean isLost;

    public Ticket(String ticketId, String licensePlate, String vehicleType, String slotId, long entryTime, boolean isVIP) {
        this.ticketId = ticketId;
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.slotId = slotId;
        this.entryTime = entryTime;
        this.isVIP = isVIP;
        this.isLost = false;
    }
}

class ParkingSlot {
    String slotId;
    String allowedType; // "BIKE", "CAR_SUV", "TRUCK", "EV"
    boolean isOccupied;

    public ParkingSlot(String slotId, String allowedType) {
        this.slotId = slotId;
        this.allowedType = allowedType;
        this.isOccupied = false;
    }
}

public class ParkingManagement {
    private List<ParkingSlot> slots = new ArrayList<>();
    private Map<String, Ticket> activeTickets = new HashMap<>();
    private List<String> parkedVehicles = new ArrayList<>(); 

    // System Rules Constants
    private static final double FINE_LOST_TICKET = 500.0;
    private static final double EV_CHARGING_FLAT_FEE = 150.0;
    private static final double PEAK_MULTIPLIER = 1.5;

    public ParkingManagement() {
        // Initialize strict allocation grid layout limits for automated testing triggers
        slots.add(new ParkingSlot("B1", "BIKE")); 
        slots.add(new ParkingSlot("C1", "CAR_SUV"));
        slots.add(new ParkingSlot("C2", "CAR_SUV"));
        slots.add(new ParkingSlot("T1", "TRUCK"));
        slots.add(new ParkingSlot("E1", "EV"));
    }

    private String getRequiredSlotType(String vehicleType) {
        String type = vehicleType.toUpperCase();
        if (type.equals("BIKE")) return "BIKE";
        if (type.equals("CAR") || type.equals("SUV")) return "CAR_SUV";
        if (type.equals("TRUCK")) return "TRUCK";
        if (type.equals("ELECTRIC VEHICLE") || type.equals("EV")) return "EV";
        return "UNKNOWN";
    }

    private double getBaseHourlyRate(String vehicleType) {
        switch (vehicleType.toUpperCase()) {
            case "BIKE": return 20.0;
            case "CAR": return 40.0;
            case "SUV": return 60.0;
            case "TRUCK": return 100.0;
            case "ELECTRIC VEHICLE": case "EV": return 50.0;
            default: return 0.0;
        }
    }

    // VEHICLE ENTRY RULE IMPLEMENTATION
    public String registerVehicleEntry(String licensePlate, String vehicleType, boolean isVIP, long entryTime) {
        if (parkedVehicles.contains(licensePlate)) {
            return "REJECTED: Duplicate Vehicle Detected Inside Lot";
        }

        String targetSlotType = getRequiredSlotType(vehicleType);
        if (targetSlotType.equals("UNKNOWN") || getBaseHourlyRate(vehicleType) == 0.0) {
            return "REJECTED: Unsupported Vehicle Classification Type";
        }

        // Automatic Smart Slot Allocation Engine
        ParkingSlot allocatedSlot = null;
        for (ParkingSlot slot : slots) {
            if (!slot.isOccupied && slot.allowedType.equals(targetSlotType)) {
                allocatedSlot = slot;
                break;
            }
        }

        if (allocatedSlot == null) {
            return "REJECTED: Target Parking Lot Section Full";
        }

        allocatedSlot.isOccupied = true;
        parkedVehicles.add(licensePlate);
        
        String ticketId = "TKT-" + licensePlate + "-" + entryTime;
        Ticket newTicket = new Ticket(ticketId, licensePlate, vehicleType, allocatedSlot.slotId, entryTime, isVIP);
        activeTickets.put(ticketId, newTicket);

        return "SUCCESS: Parking Allocated at Slot " + allocatedSlot.slotId + " | TicketID: " + ticketId;
    }

    // VEHICLE EXIT & DYNAMIC BILLING ALGORITHM IMPLEMENTATION
    public String registerVehicleExit(String ticketId, long exitTime, boolean isTicketLost, boolean isPeakHour) {
        Ticket ticket = activeTickets.get(ticketId);
        if (ticket == null) {
            return "REJECTED: Invalid or Unregistered Ticket Token ID";
        }

        ticket.isLost = isTicketLost;
        long timeDifferenceMs = exitTime - ticket.entryTime;
        double dynamicHours = (double) timeDifferenceMs / (1000.0 * 60.0 * 60.0);

        // Early Exit Condition: Round up configuration
        if (dynamicHours < 1.0) {
            dynamicHours = 1.0;
        }

        double baseRate = getBaseHourlyRate(ticket.vehicleType);
        double netParkingFee = dynamicHours * baseRate;

        // Peak Hour Surcharge Surcharge Multiplier
        if (isPeakHour) {
            netParkingFee *= PEAK_MULTIPLIER;
        }

        // EV Utility Infrastructure Charging Add-on
        if (getRequiredSlotType(ticket.vehicleType).equals("EV")) {
            netParkingFee += EV_CHARGING_FLAT_FEE;
        }

        // VIP Client Incentives Discount Deductions (50% off base parking fees)
        if (ticket.isVIP) {
            netParkingFee *= 0.5;
        }

        // Lost Documentation Recovery Penalty Addition
        if (ticket.isLost) {
            netParkingFee += FINE_LOST_TICKET;
        }

        // State Machine Rollbacks Resource Release
        for (ParkingSlot slot : slots) {
            if (slot.slotId.equals(ticket.slotId)) {
                slot.isOccupied = false;
                break;
            }
        }
        parkedVehicles.remove(ticket.licensePlate);
        activeTickets.remove(ticketId);

        return String.format("SUCCESS: Exit Approved for Slot %s | Total Parking Charges Fee: Rs. %.2f", ticket.slotId, netParkingFee);
    }

    // STANDALONE LOCAL INDEPENDENT RUNNER
    public static void main(String[] args) {
        System.out.println("======================================================");
        System.out.println("     SMART PARKING SYSTEM LOCAL DEVELOPMENT LOGS      ");
        System.out.println("======================================================");
        ParkingManagement pmInstance = new ParkingManagement();
        long currentClock = System.currentTimeMillis();

        System.out.println("-> Action: Processing Entry for Standard Car");
        String entryResult = pmInstance.registerVehicleEntry("TN-01-AB-1234", "Car", false, currentClock);
        System.out.println("   Status: " + entryResult);

        if (entryResult.contains("TicketID: ")) {
            String token = entryResult.split("TicketID: ")[1];
            System.out.println("\n-> Action: Normal Exit Processing after 3 Hours");
            System.out.println("   Status: " + pmInstance.registerVehicleExit(token, currentClock + (1000 * 60 * 60 * 3), false, false));
        }
    }
}
