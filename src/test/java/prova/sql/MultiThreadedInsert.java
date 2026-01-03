package prova.sql;
import java.sql.*;
import java.util.concurrent.*;

public class MultiThreadedInsert {
    
    // Database connection details
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=YourDatabase;encrypt=true;trustServerCertificate=true";
    private static final String USER = "your_username";
    private static final String PASS = "your_password";
    
    // Number of threads and records per thread
    private static final int NUM_THREADS = 10;
    private static final int RECORDS_PER_THREAD = 5;
    
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch latch = new CountDownLatch(NUM_THREADS);
        
        System.out.println("Starting " + NUM_THREADS + " threads...");
        
        // Submit tasks to thread pool
        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadId = i + 1;
            executor.submit(() -> {
                try {
                    insertRecords(threadId);
                } catch (Exception e) {
                    System.err.println("Thread " + threadId + " error: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            // Wait for all threads to complete
            latch.await();
            System.out.println("\nAll threads completed!");
            System.out.println("Total records inserted: " + (NUM_THREADS * RECORDS_PER_THREAD));
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
    
    private static void insertRecords(int threadId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            // Get database connection
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            
            // Prepare SQL statement
            String sql = "INSERT INTO YourTable (thread_id, record_num, data, timestamp) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            
            System.out.println("Thread " + threadId + " started inserting...");
            
            // Insert 5 records
            for (int i = 1; i <= RECORDS_PER_THREAD; i++) {
                pstmt.setInt(1, threadId);
                pstmt.setInt(2, i);
                pstmt.setString(3, "Data from thread " + threadId + ", record " + i);
                pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                
                pstmt.executeUpdate();
                
                System.out.println("Thread " + threadId + " inserted record " + i);
            }
            
            System.out.println("Thread " + threadId + " completed!");
            
        } finally {
            // Clean up resources
            if (pstmt != null) {
                try { pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}

/*
 * Before running this program:
 * 
 * 1. Add SQL Server JDBC driver to your classpath:
 *    Download from: https://docs.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server
 *    
 * 2. Create the table in SQL Server:
 *    
 *    CREATE TABLE YourTable (
 *        id INT IDENTITY(1,1) PRIMARY KEY,
 *        thread_id INT NOT NULL,
 *        record_num INT NOT NULL,
 *        data NVARCHAR(255),
 *        timestamp DATETIME2
 *    );
 *    
 * 3. Update the connection details:
 *    - DB_URL: your server address and database name
 *    - USER: your SQL Server username
 *    - PASS: your SQL Server password
 */