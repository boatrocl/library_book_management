package com.libraflow.library.service.report;

import com.libraflow.library.domain.entity.Reservation;
import com.libraflow.library.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Service
public class ReservationReportGenerator extends ReportGenerator {

    private final ReservationRepository reservationRepository;
    private List<Reservation> reservationData;

    public ReservationReportGenerator(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    protected void fetchData() {
        System.out.println("[ReservationReport] Fetching reservations from database...");
        this.reservationData = reservationRepository.findAll();
    }

    @Override
    protected void formatData() {
        System.out.println("[ReservationReport] Formatting data into CSV format...");
    }

    @Override
    protected void exportToFile() {
        System.out.println("[ReservationReport] Exporting reservation report as CSV...");
        try (PrintWriter writer = new PrintWriter(new FileWriter("reservation_report.csv"))) {
            writer.println("Reservation ID,User ID,Book ID,Status,Reserved At");
            
            for (Reservation r : reservationData) {
                String userId = r.getUser() != null ? String.valueOf(r.getUser().getId()) : "Unknown";
                String bookId = r.getBook() != null ? String.valueOf(r.getBook().getId()) : "Unknown";
                
                writer.printf("%d,%s,%s,%s,%s\n", 
                        r.getId(), userId, bookId, r.getStatus().name(), r.getReservedAt());
            }
            System.out.println("[ReservationReport] Success! CSV saved to reservation_report.csv");
            
        } catch (IOException e) {
            System.err.println("Failed to generate CSV: " + e.getMessage());
        }
    }
}
