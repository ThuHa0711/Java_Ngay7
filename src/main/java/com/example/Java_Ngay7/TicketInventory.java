package com.example.Java_Ngay7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TicketInventory {
    private final ConcurrentHashMap<TicketType, AtomicInteger> tickets = new ConcurrentHashMap<>();
    private final List<String> bookingLogs = Collections.synchronizedList(new ArrayList<>());

    // Khởi tạo số lượng vé ban đầu
    public TicketInventory() {
        tickets.put(TicketType.VIP, new AtomicInteger(50));
        tickets.put(TicketType.STANDARD, new AtomicInteger(100));
        tickets.put(TicketType.EARLY_BIRD, new AtomicInteger(30));
        tickets.put(TicketType.STUDENT, new AtomicInteger(20));
    }

    // Đặt vé
    public boolean bookTicket(TicketType type, String user) throws InvalidUserInfoException, TicketSoldOutException {
        if (user == null || user.isEmpty()) {
            throw new InvalidUserInfoException("Thông tin người dùng không hợp lệ");
        }

        AtomicInteger remaining = tickets.get(type);

        if (remaining.get() > 0) {
            int updatedCount = remaining.decrementAndGet();
            if (updatedCount >= 0) {
                bookingLogs.add(user + " đã đặt loại vé " + type);
                return true;
            } else {
                remaining.incrementAndGet();
            }
        }
        throw new TicketSoldOutException("Vé " + type + " đã bán hết");
    }

    // Lấy số lượng vé còn lại
    public int getRemainingTickets(TicketType type) {
        return tickets.get(type).get();
    }

    // Lấy toàn bộ log đặt vé
    public List<String> getLogs() {
        return bookingLogs;
    }

    // Mô phỏng xử lý thanh toán
    public void processPayment(String user) throws PaymentGatewayException {
        try {
            // Giả lập quá trình thanh toán
            if (Math.random() > 0.5) {
                throw new Exception("Hết thời gian chờ cổng thanh toán");
            }
            System.out.println("Thanh toán thành công cho người dùng " + user);
        } catch (Exception e) {
            throw new PaymentGatewayException("Thanh toán thất bại cho người dùng " + user, e);
        }
    }

    // Mô phỏng multi-thread
    public static void main(String[] args) {
        TicketInventory inventory = new TicketInventory();
        TicketType[] types = TicketType.values();
        for (int i = 0; i < 10; i++) {
            final int userId = i;
            final TicketType ticketType = types[i % types.length];
            new Thread(() -> {
                String user = "User" + userId;
                try {
                    boolean booked = inventory.bookTicket(ticketType, user);
                    if (booked) {
                        System.out.println(user + " đã đặt vé " + ticketType + " thành công");
                        System.out.println("Số lượng vé " + ticketType + " còn lại: " + inventory.getRemainingTickets(ticketType));
                        inventory.processPayment(user);
                    }
                } catch (InvalidUserInfoException | TicketSoldOutException | PaymentGatewayException e) {
                    System.out.println(e.getMessage());
                }
            }).start();
        }


        // Đợi các thread hoàn tất
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Hiển thị log
        System.out.println("\nBooking Logs:");
        inventory.getLogs().forEach(System.out::println);
    }
}
