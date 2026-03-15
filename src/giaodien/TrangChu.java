package giaodien;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import database.DBConnection;
import javax.swing.Timer;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class TrangChu extends JFrame {
	// --- Nhóm Điều khiển Thời gian (Timing & Logic) ---
	private javax.swing.Timer timer;         // Timer tính tiền giờ cho bàn đang chọn
	private Timer realTimeTimer;            // Timer cập nhật đồng hồ hệ thống
	private long startTimestamp;            // Thời điểm bắt đầu mở bàn chơi

	// --- Nhóm Giao diện & Bảng biểu (Core UI) ---
	private JPanel contentPane, pBan;       // Khung chứa chính và khu vực danh sách bàn
	private DefaultTableModel modelHoaDon;  // Model quản lý dữ liệu bảng hóa đơn
	private int currentTableId = -1;        // ID của bàn hiện đang được chọn

	// --- Nhóm Nhập liệu & Lựa chọn (Inputs) ---
	private JComboBox<String> cbMon;        // Danh sách chọn món nước
	private JComboBox cbChuyenBan;          // Danh sách chọn bàn để chuyển
	private JSpinner spSL;                  // Ô chọn số lượng món
	private Map<String, Integer> mapDrinkId = new HashMap<>(); // Lưu ID món từ tên
	private Map<String, Double> mapGia = new HashMap<>();      // Lưu giá món từ tên

	// --- Nhóm Hiển thị Thông tin (Labels & TextFields) ---
	private JLabel lblB, lblThoiGian;       // Tên bàn đang chọn và đồng hồ đếm giờ
	private JLabel lblTienBan, lblGia, lblSl, lblTamTinh; // Các nhãn tiêu đề hiển thị
	private JTextField txtTongTien;          // Tổng thanh toán cuối cùng
	private JTextField txtGia;              // Hiển thị giá đơn vị của món
	private JTextField txtTienGio;           // Hiển thị tiền giờ tích lũy hiện tại

	// --- Nhóm Chức năng & Công cụ (Actions & Utilities) ---
	private JButton btnQTV, btnChuyenBan;   // Nút mở quản trị và nút chuyển bàn
	private DecimalFormat df = new DecimalFormat("#,###"); // Định dạng tiền tệ 1,000 VNĐ
	private JButton btnHoiAI;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                // Mặc định chạy với quyền Admin để test
                TrangChu frame = new TrangChu("ADMIN");
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public TrangChu(String role) {
        this(); // Khởi tạo giao diện
        if (role != null && !role.equalsIgnoreCase("ADMIN")) {
            btnQTV.setVisible(false);
        }
        loadDanhSachBan();
        loadDanhSachMon();
        loadBanVaoComboBox();
    }

    public TrangChu() {
        setTitle("BILLIARDS AND COFFEE");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setBounds(100, 100, 913, 600);
        setLocationRelativeTo(null);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        // PANEL DANH SÁCH BÀN
        pBan = new JPanel();
        pBan.setLayout(new GridLayout(0, 2, 10, 10));
        JScrollPane scrollBan = new JScrollPane(pBan);
        scrollBan.setBounds(10, 54, 330, 400);
        contentPane.add(scrollBan);

        // THÔNG TIN MÓN
        JLabel lblMon = new JLabel("Món:");
        lblMon.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblMon.setBounds(365, 9, 50, 25);
        contentPane.add(lblMon);

        cbMon = new JComboBox<>();
        cbMon.setBounds(410, 9, 170, 25);
        contentPane.add(cbMon);

        // 1. SỰ KIỆN CHỌN MÓN: Khi người dùng chọn một món trong danh sách
        cbMon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedMon = (String) cbMon.getSelectedItem();
                if (selectedMon != null && mapGia.containsKey(selectedMon)) {
                    double gia = mapGia.get(selectedMon);
                    // Hiển thị giá vào ô txtGia (định dạng số nguyên)
                    txtGia.setText(String.format("%.0f", gia));
                }
            }
        });

        // 2. SỰ KIỆN MỞ DANH SÁCH: Load món mới từ DB ngay khi click vào combobox
        cbMon.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                loadDanhSachMon(); // Gọi hàm cập nhật dữ liệu
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {}

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });

        txtGia = new JTextField();
        txtGia.setEditable(false);
        txtGia.setBounds(739, 10, 80, 25);
        contentPane.add(txtGia);

        spSL = new JSpinner(new SpinnerNumberModel(1, -1, 100, 1));
        spSL.setBounds(619, 10, 50, 25);
        contentPane.add(spSL);

        // NÚT CHỨC NĂNG
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                xuLyThoat();
            }
        });
        
        JButton btnThemMon = new JButton("Thêm Món");
        btnThemMon.setBounds(410, 54, 100, 35);
        contentPane.add(btnThemMon);
        btnThemMon.addActionListener(e -> xuLyThemMon());
        
        JButton btnBatDau = new JButton("Bắt Đầu");
        btnBatDau.setBounds(719, 54, 100, 35);
        contentPane.add(btnBatDau);
        btnBatDau.addActionListener(e -> xuLyBatDau());

        JButton btnThanhToan = new JButton("Thanh Toán");
        btnThanhToan.setBounds(754, 460, 135, 38);
        contentPane.add(btnThanhToan);
        btnThanhToan.addActionListener(e -> xuLyThanhToan());

        // ĐỒNG HỒ & TRẠNG THÁI
        lblB = new JLabel("Bàn đang chọn: ");
        lblB.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblB.setBounds(20, 464, 114, 30);
        contentPane.add(lblB);

        lblThoiGian = new JLabel("00:00:00");
        lblThoiGian.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblThoiGian.setForeground(Color.RED);
        lblThoiGian.setBounds(467, 464, 113, 30);
        contentPane.add(lblThoiGian);

        txtTienGio = new JTextField("0");
        txtTienGio.setFont(new Font("Tahoma", Font.BOLD, 12));
        txtTienGio.setEditable(false);
        txtTienGio.setBounds(433, 506, 113, 30);
        contentPane.add(txtTienGio);

        // BẢNG HÓA ĐƠN
        modelHoaDon = new DefaultTableModel(new Object[][]{}, new String[]{"Tên món", "Số lượng", "Giá", "Thành tiền"});
        JTable tblHoaDon = new JTable(modelHoaDon);
        JScrollPane scrollHoaDon = new JScrollPane(tblHoaDon);
        scrollHoaDon.setBounds(350, 111, 539, 343);
        contentPane.add(scrollHoaDon);

        txtTongTien = new JTextField();
        txtTongTien.setEditable(false);
        txtTongTien.setFont(new Font("Tahoma", Font.BOLD, 12));
        txtTongTien.setBounds(619, 460, 125, 38);
        contentPane.add(txtTongTien);

        btnQTV = new JButton("Quản Trị"); 
        btnQTV.setBounds(10, 10, 114, 34);
        contentPane.add(btnQTV);           
        btnQTV.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TrangQuanTri tqt = new TrangQuanTri(TrangChu.this); 
                tqt.setVisible(true);
                setVisible(false);
            }
        });
        
        JLabel lblTime = new JLabel("Thời gian chơi:");
        lblTime.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblTime.setBounds(365, 466, 125, 30);
        contentPane.add(lblTime);
        
        lblTienBan = new JLabel("Tiền bàn:");
        lblTienBan.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblTienBan.setBounds(365, 505, 125, 30);
        contentPane.add(lblTienBan);
        
        lblGia = new JLabel("Giá:");
        lblGia.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblGia.setBounds(694, 9, 50, 25);
        contentPane.add(lblGia);
        
        lblSl = new JLabel("SL:");
        lblSl.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblSl.setBounds(590, 9, 50, 25);
        contentPane.add(lblSl);
        
        lblTamTinh = new JLabel("Tạm tính:");
        lblTamTinh.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblTamTinh.setBounds(558, 465, 80, 30);
        contentPane.add(lblTamTinh);
        
        JButton btnXoaMon = new JButton("Xóa Món");
        	btnXoaMon.addActionListener(e -> {
        	    // 1. Lấy dòng được chọn từ JTable (không phải từ Model)
        	    int selectedRow = tblHoaDon.getSelectedRow();
        	    
        	    if (selectedRow == -1) {
        	        JOptionPane.showMessageDialog(this, "Vui lòng chọn món cần xóa!");
        	        return;
        	    }

        	    // 2. Xác nhận xóa
        	    int confirm = JOptionPane.showConfirmDialog(this, 
        	        "Bạn có chắc chắn muốn xóa món này không?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        	    
        	    if (confirm == JOptionPane.YES_OPTION) {
        	        try (Connection conn = DBConnection.getConnection()) {
        	            // Lấy tên món từ cột 0 để làm điều kiện xóa
        	            String tenMon = modelHoaDon.getValueAt(selectedRow, 0).toString();
        	            
        	            // 3. Xóa trong CSDL (Xóa chi tiết món thuộc Session/Order hiện tại)
        	            String sqlDelete = "DELETE od FROM order_details od " +
        	                               "JOIN drinks d ON od.drink_id = d.drink_id " +
        	                               "JOIN orders o ON od.order_id = o.order_id " +
        	                               "WHERE o.table_id = ? AND o.status = 'UNPAID' AND d.drink_name = ?";
        	            
        	            PreparedStatement ps = conn.prepareStatement(sqlDelete);
        	            ps.setInt(1, currentTableId);
        	            ps.setString(2, tenMon);
        	            
        	            int rowsAffected = ps.executeUpdate();
        	            
        	            if (rowsAffected > 0) {
        	                // 4. Load lại dữ liệu từ DB lên Table để đồng bộ và tính lại tiền
        	                loadHoaDonTuDatabase(currentTableId); 
        	                JOptionPane.showMessageDialog(this, "Đã xóa món: " + tenMon);
        	            }
        	            
        	        } catch (SQLException ex) {
        	            ex.printStackTrace();
        	            JOptionPane.showMessageDialog(this, "Lỗi khi xóa món: " + ex.getMessage());
        	        }
        	    }
        	});
        btnXoaMon.setBounds(558, 54, 111, 35);
        contentPane.add(btnXoaMon);
        
        btnChuyenBan = new JButton("Chuyển");
        btnChuyenBan.addActionListener(e -> {
            // 1. Lấy bàn ĐÍCH từ ComboBox
            String tenBanDich = (String) cbChuyenBan.getSelectedItem();
            
            // Kiểm tra nếu chưa chọn bàn hoặc chọn vào dòng tiêu đề
            if (tenBanDich == null || tenBanDich.equals("--- Chọn bàn đích ---")) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn muốn chuyển đến từ danh sách!");
                return;
            }

            // 3. Lấy ID của bàn đích từ tên
            int idBanDich = getIdBanTuTen(tenBanDich);

            // 4. Kiểm tra logic: Không chuyển vào chính nó
            if (idBanDich == currentTableId) {
                JOptionPane.showMessageDialog(this, "Bàn đích phải khác bàn hiện tại!");
                return;
            }

            // 5. Xác nhận thao tác với người dùng
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc chắn muốn chuyển toàn bộ hóa đơn sang " + tenBanDich + " không?", 
                "Xác nhận chuyển bàn", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                // 6. Gọi hàm xử lý DB đã viết ở bước trước
                chuyenBanSangBanTrong(currentTableId, idBanDich);
                
                // 7. Xóa lựa chọn bàn hiện tại sau khi chuyển xong để tránh nhầm lẫn
                currentTableId = -1;  
            }
        });
        btnChuyenBan.setBounds(230, 504, 110, 35);
        contentPane.add(btnChuyenBan);
        
        cbChuyenBan = new JComboBox();
        cbChuyenBan.setBounds(230, 464, 110, 35);
        contentPane.add(cbChuyenBan);
        //sk load bàn khi click combobox Bàn
        cbChuyenBan.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                loadBanVaoComboBox(); 
            }
            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {}
            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });
        
        
        btnHoiAI = new JButton("Hỏi AI");
        btnHoiAI.setBounds(226, 10, 114, 35);
        contentPane.add(btnHoiAI);
        btnHoiAI.addActionListener(e -> {
            String monChon = cbMon.getSelectedItem().toString();
            
            // Vô hiệu hóa nút để tránh bấm nhiều lần gây lỗi 429
            btnHoiAI.setEnabled(false);
            btnHoiAI.setText("AI đang pha...");

            new Thread(() -> {
                String ketQua = AI_Bartender.layHuongDanPhaChe(monChon);
                
                SwingUtilities.invokeLater(() -> {
                    // Hiển thị kết quả
                    JEditorPane ep = new JEditorPane("text/html", "<html><body style='font-family:Arial;padding:10px'>" + ketQua + "</body></html>");
                    ep.setEditable(false);
                    JScrollPane sp = new JScrollPane(ep);
                    sp.setPreferredSize(new java.awt.Dimension(400, 500));
                    
                    JOptionPane.showMessageDialog(this, sp, "Công thức: " + monChon, JOptionPane.PLAIN_MESSAGE);
                    
                    // Kích hoạt lại nút sau khi xong
                    btnHoiAI.setEnabled(true);
                    btnHoiAI.setText("Hỏi AI");
                });
            }).start();
        });
    }
    
    // ================= LOGIC XỬ LÝ =================

    public void loadDanhSachBan() {
        pBan.removeAll();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT table_id, table_name, status FROM billiard_tables";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("table_id");
                String ten = rs.getString("table_name");
                String status = rs.getString("status");
                JButton btn = new JButton(ten);
                btn.setBackground(status.equals("CO_KHACH") ? Color.PINK : Color.WHITE);
                btn.addActionListener(e -> {
                    currentTableId = id;
                    lblB.setText("Đang chọn: " + ten);
                    checkTrangThaiBan(id); 
                    hienThiChiTietBan(id);
                });
                pBan.add(btn);
            }
        } catch (Exception e) { e.printStackTrace(); }
        pBan.revalidate(); pBan.repaint();
    }
    
    private void checkTrangThaiBan(int tableId) {
        loadHoaDonTuDatabase(tableId);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT start_time, price_per_hour FROM billiard_sessions WHERE table_id = ? AND end_time IS NULL";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, tableId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                startRealTimeUpdate(rs.getTimestamp("start_time"), rs.getDouble("price_per_hour"));
            } else {
                if (realTimeTimer != null) realTimeTimer.stop();
                lblThoiGian.setText("00:00:00");
                txtTienGio.setText("0");
                txtTongTien.setText("0 VND");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void xuLyBatDau() {
        if (currentTableId == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn!"); return; }
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu giao dịch (Transaction)

            // 1. Tạo Orders mới (Trạng thái UNPAID)
            String sqlOrder = "INSERT INTO orders (table_id, status) VALUES (?, 'UNPAID')";
            int generatedOrderId = -1;
            try (PreparedStatement psO = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                psO.setInt(1, currentTableId);
                psO.executeUpdate();
                try (ResultSet rs = psO.getGeneratedKeys()) {
                    if (rs.next()) generatedOrderId = rs.getInt(1);
                }
            }

            // 2. Tạo Session bida liên kết với Order vừa tạo
            String sqlSession = "INSERT INTO billiard_sessions (table_id, order_id, start_time) VALUES (?, ?, NOW())";
            try (PreparedStatement psS = conn.prepareStatement(sqlSession)) {
                psS.setInt(1, currentTableId);
                psS.setInt(2, generatedOrderId);
                psS.executeUpdate();
            }

            // 3. Cập nhật trạng thái bàn sang CO_KHACH
            String sqlTable = "UPDATE billiard_tables SET status = 'CO_KHACH' WHERE table_id = ?";
            try (PreparedStatement psT = conn.prepareStatement(sqlTable)) {
                psT.setInt(1, currentTableId);
                psT.executeUpdate();
            }

            conn.commit(); // Hoàn tất mọi thay đổi
            
            loadDanhSachBan(); // Vẽ lại nút bàn
            checkTrangThaiBan(currentTableId); // Khởi động Timer
            
        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi mở bàn: " + e.getMessage());
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
        }
    }

    private void xuLyThemMon() {
        if (currentTableId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn trước!");
            return;
        }
        
        String mon = (String) cbMon.getSelectedItem();
        int slThem = (int) spSL.getValue();
        int drinkId = mapDrinkId.get(mon);
        double gia = mapGia.get(mon);

        try (Connection conn = DBConnection.getConnection()) {
            // 1. Tìm OrderID đang hoạt động (UNPAID) của bàn này
            String sqlCheckOrder = "SELECT order_id FROM orders WHERE table_id = ? AND status = 'UNPAID'";
            PreparedStatement psCheckOrder = conn.prepareStatement(sqlCheckOrder);
            psCheckOrder.setInt(1, currentTableId);
            ResultSet rsOrder = psCheckOrder.executeQuery();
            
            int orderId;
            if (rsOrder.next()) {
                orderId = rsOrder.getInt("order_id");
            } else {
                // Nếu chưa có Order thì tạo mới
                String sqlInsOrder = "INSERT INTO orders (table_id, status) VALUES (?, 'UNPAID')";
                PreparedStatement psInsOrder = conn.prepareStatement(sqlInsOrder, Statement.RETURN_GENERATED_KEYS);
                psInsOrder.setInt(1, currentTableId);
                psInsOrder.executeUpdate();
                ResultSet gk = psInsOrder.getGeneratedKeys();
                gk.next(); 
                orderId = gk.getInt(1);
            }
            
            // 2. KIỂM TRA MÓN ĐÃ CÓ TRONG CHI TIẾT HÓA ĐƠN CHƯA
            String sqlCheckDetail = "SELECT quantity FROM order_details WHERE order_id = ? AND drink_id = ?";
            PreparedStatement psCheckDetail = conn.prepareStatement(sqlCheckDetail);
            psCheckDetail.setInt(1, orderId);
            psCheckDetail.setInt(2, drinkId);
            ResultSet rsDetail = psCheckDetail.executeQuery();

            if (rsDetail.next()) {
                // NẾU ĐÃ CÓ: Cập nhật cộng dồn số lượng
                int slCu = rsDetail.getInt("quantity");
                String sqlUpdate = "UPDATE order_details SET quantity = ? WHERE order_id = ? AND drink_id = ?";
                PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate);
                psUpdate.setInt(1, slCu + slThem);
                psUpdate.setInt(2, orderId);
                psUpdate.setInt(3, drinkId);
                psUpdate.executeUpdate();
            } else {
                // NẾU CHƯA CÓ: Thêm dòng mới
                String sqlInsert = "INSERT INTO order_details (order_id, drink_id, quantity, price) VALUES (?, ?, ?, ?)";
                PreparedStatement psInsert = conn.prepareStatement(sqlInsert);
                psInsert.setInt(1, orderId);
                psInsert.setInt(2, drinkId);
                psInsert.setInt(3, slThem);
                psInsert.setDouble(4, gia);
                psInsert.executeUpdate();
            }
            
            // 3. Load lại bảng để hiển thị kết quả mới nhất
            loadHoaDonTuDatabase(currentTableId);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm món: " + e.getMessage());
        }
    }

    private void startRealTimeUpdate(Timestamp startTime, double pph) {
        // 1. Kiểm tra và dừng Timer cũ nếu đang chạy để tránh tốn tài nguyên
        if (realTimeTimer != null && realTimeTimer.isRunning()) {
            realTimeTimer.stop();
        }

        // 2. Khởi tạo Timer mới với chu kỳ 1 giây (1000ms)
        realTimeTimer = new Timer(1000, e -> {
            // Tính toán khoảng cách thời gian
            long diff = System.currentTimeMillis() - startTime.getTime();
            long totalSec = diff / 1000;

            // Kiểm tra an toàn: Nếu giây âm (do sai lệch múi giờ hệ thống) thì gán bằng 0
            if (totalSec < 0) totalSec = 0;

            // 3. Định dạng hiển thị thời gian HH:mm:ss
            long hours = totalSec / 3600;
            long minutes = (totalSec % 3600) / 60;
            long seconds = totalSec % 60;
            String timeDisplay = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            
            // Cập nhật lên Label
            lblThoiGian.setText(timeDisplay);

            // 4. Tính toán tiền giờ thực tế (Sử dụng double để có độ chính xác cao nhất)
            // Công thức: (Số giây / 3600) * Đơn giá mỗi giờ
            double tienGio = (totalSec / 3600.0) * pph;
            
            // Hiển thị tiền giờ lên giao diện (Sử dụng DecimalFormat df đã khai báo)
            txtTienGio.setText(df.format(tienGio));

            // 5. Cập nhật tổng cộng (Bao gồm tiền giờ + tiền dịch vụ hiện có)
            tinhTongCong(tienGio);
            
            // Cơ chế bảo vệ: Nếu Label bị reset về 00:00:00 bởi hàm khác, tự dừng Timer này
            if (timeDisplay.equals("00:00:00") && totalSec > 10) { 
                ((Timer)e.getSource()).stop();
            }
        });

        // 6. Bắt đầu chạy
        realTimeTimer.start();
    }

    private void loadHoaDonTuDatabase(int tableId) {
        modelHoaDon.setRowCount(0);
        String sql = "SELECT d.drink_name, od.quantity, od.price " +
                     "FROM order_details od " +
                     "JOIN drinks d ON od.drink_id = d.drink_id " +
                     "JOIN orders o ON od.order_id = o.order_id " +
                     "WHERE o.table_id = ? AND o.status = 'UNPAID'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tableId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ten = rs.getString("drink_name");
                    int sl = rs.getInt("quantity");
                    double gia = rs.getDouble("price");
                    modelHoaDon.addRow(new Object[]{ten, sl, df.format(gia), df.format(sl * gia)});
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void tinhTongCong(double tienGio) {
        double tongMon = 0;
        for (int i = 0; i < modelHoaDon.getRowCount(); i++) {
            String val = modelHoaDon.getValueAt(i, 3).toString().replace(",", "");
            tongMon += Double.parseDouble(val);
        }
        txtTongTien.setText(df.format(tongMon + tienGio) + " VNĐ");
    }

    private void xuLyThanhToan() {
        if (currentTableId == -1) return;

        int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận thanh toán cho bàn này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Dừng timer ngay lập tức
        if (realTimeTimer != null) realTimeTimer.stop();

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // BẮT ĐẦU TRANSACTION

            // --- BƯỚC 1: LẤY THÔNG TIN CẦN THIẾT (Dùng chung Connection) ---
            String gioVaoThucTe = "Chưa bắt đầu";
            double pph = 0;
            String sqlInfo = "SELECT start_time, price_per_hour FROM billiard_sessions WHERE table_id = ? AND end_time IS NULL";
            try (PreparedStatement psInfo = conn.prepareStatement(sqlInfo)) {
                psInfo.setInt(1, currentTableId);
                try (ResultSet rsInfo = psInfo.executeQuery()) {
                    if (rsInfo.next()) {
                        java.sql.Timestamp ts = rsInfo.getTimestamp("start_time");
                        gioVaoThucTe = new java.text.SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(ts);
                        pph = rsInfo.getDouble("price_per_hour");
                    }
                }
            }

            // --- BƯỚC 2: TÍNH TOÁN TIỀN ---
            String thoiGian = lblThoiGian.getText();
            double tienGio = Double.parseDouble(txtTienGio.getText().replaceAll("[, VNĐ]", "").trim());
            
            // Tính tiền nước từ model (trước khi thêm dòng Tiền Giờ vào model để tránh tính lặp)
            double tongTienNuoc = 0;
            for (int i = 0; i < modelHoaDon.getRowCount(); i++) {
                String val = modelHoaDon.getValueAt(i, 3).toString().replaceAll("[, VNĐ<html><b>/i]", "").trim();
                if(!val.isEmpty()) tongTienNuoc += Double.parseDouble(val);
            }
            double tongCuoiCung = tongTienNuoc + tienGio;

            // --- BƯỚC 3: CẬP NHẬT CƠ SỞ DỮ LIỆU ---
            
            // 3.1 Cập nhật Sessions
            String sqlUpSession = "UPDATE billiard_sessions SET end_time = NOW(), total_price = ? WHERE table_id = ? AND end_time IS NULL";
            try (PreparedStatement psUpS = conn.prepareStatement(sqlUpSession)) {
                psUpS.setDouble(1, tienGio);
                psUpS.setInt(2, currentTableId);
                psUpS.executeUpdate();
            }

            // 3.2 Cập nhật Orders
            String sqlUpOrder = "UPDATE orders SET total_amount = ?, status = 'PAID' WHERE table_id = ? AND status = 'UNPAID'";
            try (PreparedStatement psUpO = conn.prepareStatement(sqlUpOrder)) {
                psUpO.setDouble(1, tongCuoiCung);
                psUpO.setInt(2, currentTableId);
                psUpO.executeUpdate();
            }

            // 3.3 Giải phóng bàn
            try (PreparedStatement psUpT = conn.prepareStatement("UPDATE billiard_tables SET status = 'TRONG' WHERE table_id = ?")) {
                psUpT.setInt(1, currentTableId);
                psUpT.executeUpdate();
            }

            // --- BƯỚC 4: XÁC NHẬN (COMMIT) ---
            conn.commit(); 

            // --- BƯỚC 5: IN HÓA ĐƠN & GIAO DIỆN ---
            // Thêm dòng hiển thị vào Table để in cho đẹp
            modelHoaDon.addRow(new Object[] { "THỜI GIAN CHƠI", thoiGian, df.format(pph) + "/h", df.format(tienGio) });
            modelHoaDon.addRow(new Object[] { "<html><b>TỔNG CỘNG</b></html>", "", "", "<html><b style='color:red'>" + df.format(tongCuoiCung) + " VNĐ</b></html>" });

            inHoaDon(gioVaoThucTe, tienGio);
            
            JOptionPane.showMessageDialog(this, "Thanh toán thành công!\nTổng cộng: " + df.format(tongCuoiCung) + " VNĐ");

            // Làm sạch
            modelHoaDon.setRowCount(0);
            lblThoiGian.setText("00:00:00");
            txtTienGio.setText("0");
            txtTongTien.setText("0 VNĐ");
            
            loadDanhSachBan();
            checkTrangThaiBan(currentTableId);

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi thanh toán: " + e.getMessage());
        } finally {
            // ĐẢM BẢO LUÔN ĐÓNG KẾT NỐI
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
    
    private void loadDanhSachMon() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT drink_id, drink_name, price FROM drinks ORDER BY drink_name ASC";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            
            cbMon.removeAllItems();
            mapGia.clear();
            mapDrinkId.clear();
            
            while (rs.next()) {
                String tenMon = rs.getString("drink_name");
                double gia = rs.getDouble("price");
                int id = rs.getInt("drink_id");
                
                cbMon.addItem(tenMon);
                mapGia.put(tenMon, gia);
                mapDrinkId.put(tenMon, id); // Lưu ID để dùng cho nút Thêm
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    
 // Thêm tham số truyền vào để dữ liệu luôn chính xác
    private void inHoaDon(String gioVaoTruocKhiUpdate, double tienGioDaTinh) {
        if (currentTableId == -1) return;
        
        int sessionId = getActiveSessionId(currentTableId);
        String timeStamp = new java.text.SimpleDateFormat("HHmmss").format(new java.util.Date());
        String fileName = "HoaDon_Ban_" + currentTableId + "_Time_" + timeStamp + ".txt";
        
        // Sử dụng try-with-resources để tự động đóng file
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"))) {
            // 1. Tiêu đề
            writer.println("            GEN'Z BILLIARDS           ");
            writer.println("   41A/6 Mạc Thiên Tích, Ninh Kiều, CT");
            writer.println("           SĐT: 0328 271 122          ");
            writer.println("");
            writer.println("           HÓA ĐƠN THANH TOÁN         ");
            writer.println("");
            
            // 2. Thông tin chung
            writer.println("----------------------------------------");
            writer.println(lblB.getText().replace("Đang chọn: ", ""));
            
            // Sử dụng giờ vào được truyền từ hàm xử lý thanh toán
            String gioRa = new java.text.SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new java.util.Date());
            writer.println("Giờ vào: " + (gioVaoTruocKhiUpdate == null ? "Chưa bắt đầu" : gioVaoTruocKhiUpdate));
            writer.println("Giờ ra : " + gioRa);
            writer.println("----------------------------------------");

            // 3. CHI TIẾT TIỀN GIỜ
            writer.println("Thời gian chơi         Đ.giá      T.Tiền");
            writer.println("");
            
            // Lấy lại đơn giá/giờ từ CSDL (chỉ để hiển thị)
            double pph = 0;
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "SELECT price_per_hour FROM billiard_sessions WHERE table_id = ? ORDER BY session_id DESC LIMIT 1";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, currentTableId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) pph = rs.getDouble("price_per_hour");
            }
            
            String thoiLuong = (lblThoiGian.getText() == null || lblThoiGian.getText().isEmpty()) ? "00:00:00" : lblThoiGian.getText();
            writer.printf("   %-15s %10s %10s\n", 
                    thoiLuong, 
                    df.format(pph) + "/h", 
                    df.format(tienGioDaTinh));
            writer.println("----------------------------------------");

            // 4. CHI TIẾT MẶT HÀNG
            writer.println("Mặt hàng          SL    Đ.giá    T.Tiền");
            writer.println("----------------------------------------");
            
            double tongTienHang = 0;
            int countHang = 0;
            
            for (int i = 0; i < modelHoaDon.getRowCount(); i++) {
                String ten = modelHoaDon.getValueAt(i, 0).toString();
                
                // Lọc bỏ các dòng không phải mặt hàng
                if (ten.contains("<html>") || ten.toUpperCase().contains("THỜI GIAN") || ten.toUpperCase().contains("TỔNG")) {
                    continue; 
                }
                
                try {
                    String sl = modelHoaDon.getValueAt(i, 1).toString();
                    String gia = modelHoaDon.getValueAt(i, 2).toString();
                    String ttStr = modelHoaDon.getValueAt(i, 3).toString().replace(",", "").replace(" VNĐ", "");
                    
                    double thanhTien = Double.parseDouble(ttStr);
                    tongTienHang += thanhTien;
                    countHang++;
                    
                    if (ten.length() > 18) ten = ten.substring(0, 16) + "..";
                    writer.printf("%-17s %-4s %-8s %8s\n", ten, sl, gia, df.format(thanhTien));
                } catch (Exception e) {
                    // Bỏ qua dòng nếu không parse được số
                }
            }

            // 5. TỔNG CỘNG
            double tongCongFinal = tongTienHang + tienGioDaTinh;
            writer.println("----------------------------------------");
            writer.printf("Tiền hàng (%d):%25s\n", countHang, df.format(tongTienHang));
            writer.printf("Tiền giờ:  %27s\n", df.format(tienGioDaTinh));
            writer.println("========================================");
            writer.printf("TỔNG TIỀN:   %21s VNĐ\n", df.format(tongCongFinal));
            writer.println("========================================");
            writer.println("     Cảm ơn Quý khách - Hẹn gặp lại!     ");
            
            writer.flush();
            
            // Mở file sau khi ghi xong
            File file = new File(fileName);
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi in hóa đơn: " + e.getMessage());
        }
    }
    private String getStartTimeFromDB(int tableId) {
        String startTime = "00:00:00";
        try (Connection conn = DBConnection.getConnection()) {
            // Thêm %Y vào cuối để lấy đầy đủ năm (4 chữ số)
            String sql = "SELECT DATE_FORMAT(start_time, '%H:%i:%s %d/%m/%Y') FROM billiard_sessions " +
                         "WHERE table_id = ? AND end_time IS NULL";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, tableId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                startTime = rs.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return startTime;
    }
    private int getActiveSessionId(int tableId) {
        int id = 0;
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT session_id FROM billiard_sessions WHERE table_id = ? AND end_time IS NULL";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, tableId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) id = rs.getInt("session_id");
        } catch (Exception e) { e.printStackTrace(); }
        return id;
    }
    
    private void luuThanhToanVaoCSDL(int tableId) {
        // 1. Lấy số tiền từ txtTongTien và làm sạch (xóa VNĐ, thẻ HTML, dấu phẩy)
        // Điều này đảm bảo giá trị đưa vào total_amount là số thuần túy
        String cleanTongTien = txtTongTien.getText()
                .replaceAll("(?i)VNĐ|<html>|<b>|</b>|</html>|<[^>]*>", "")
                .replace(",", "")
                .trim();
        
        double soTienCuoiCung = 0;
        try {
            if (!cleanTongTien.isEmpty()) {
                soTienCuoiCung = Double.parseDouble(cleanTongTien);
            }
        } catch (NumberFormatException e) {
            System.err.println("Lỗi chuyển đổi số tiền: " + e.getMessage());
        }

        // 2. Câu lệnh SQL cập nhật trạng thái và tổng tiền cho bảng orders
        String sql = "UPDATE orders SET status = 'PAID', total_amount = ?, order_time = NOW() "
                   + "WHERE table_id = ? AND status = 'CHƯA THANH TOÁN'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDouble(1, soTienCuoiCung); // Lưu vào cột total_amount
            ps.setInt(2, tableId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Đã cập nhật total_amount: " + soTienCuoiCung + " và trạng thái PAID cho bàn " + tableId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật CSDL: " + e.getMessage());
        }
    }
    //chuyenban
    private void chuyenBanSangBanTrong(int idBanCu, int idBanMoi) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Cập nhật table_id cho hóa đơn (món ăn)
            String sqlOrder = "UPDATE orders SET table_id = ? WHERE table_id = ? AND status = 'UNPAID'";
            PreparedStatement ps1 = conn.prepareStatement(sqlOrder);
            ps1.setInt(1, idBanMoi);
            ps1.setInt(2, idBanCu);
            ps1.executeUpdate();

            // 2. Cập nhật table_id cho phiên chơi (giờ vào)
            String sqlSession = "UPDATE billiard_sessions SET table_id = ? WHERE table_id = ? AND end_time IS NULL";
            PreparedStatement ps2 = conn.prepareStatement(sqlSession);
            ps2.setInt(1, idBanMoi);
            ps2.setInt(2, idBanCu);
            ps2.executeUpdate();

            // 3. Đổi trạng thái bàn trong CSDL
            updateTrangThaiBan(idBanCu, "TRONG", conn);
            updateTrangThaiBan(idBanMoi, "CO_KHACH", conn);

            conn.commit();

            // --- XỬ LÝ GIAO DIỆN ---
            // Dừng timer ngay lập tức để đồng hồ bàn cũ không chạy nữa
            if (timer != null) timer.stop();

            loadDanhSachBan(); // Vẽ lại màu bàn
            loadBanVaoComboBox();

            // Chuyển tiêu điểm sang bàn mới
            this.currentTableId = idBanMoi;
            hienThiChiTietBan(idBanMoi); // Tự động chạy lại Timer cho bàn mới

            JOptionPane.showMessageDialog(this, "Đã chuyển bàn thành công!");

        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }
    
    public void hienThiChiTietBan(int tableId) {
        // Lấy start_time từ bảng sessions
        String sql = "SELECT start_time FROM billiard_sessions WHERE table_id = ? AND end_time IS NULL";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tableId);
            ResultSet rs = ps.executeQuery();
            // neu chuyen qua ban khong co nguoi thi mo ban
            if (rs.next()) {
                java.sql.Timestamp startTime = rs.getTimestamp("start_time");
                startTimer(startTime); // Khởi động đếm giờ
            } else {
                // dừng timer khi chuyển bàn đi
                lblThoiGian.setText("00:00:00");
                if (timer != null) timer.stop();
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }
    
    private int getOrderIdByTable(int tableId) {
        int orderId = -1;
        // status = 0 hoặc 'Chưa thanh toán' tùy theo cách bạn lưu trong DB
        String sql = "SELECT order_id FROM orders WHERE table_id = ? AND status = 'UNPAID'";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, tableId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                orderId = rs.getInt("order_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderId;
    }
    
    private void updateTrangThaiBan(int id, String status, Connection conn) throws SQLException {
        // Đảm bảo status truyền vào là 'TRONG' hoặc 'CO_KHACH' khớp với ENUM trong MySQL
        String sql = "UPDATE billiard_tables SET status = ? WHERE table_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }
    
    private int getIdBanTuTen(String tenBan) {
        int id = -1;
        String sql = "SELECT table_id FROM billiard_tables WHERE table_name = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, tenBan);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                id = rs.getInt("table_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }
    
    public void loadBanVaoComboBox() {
        // 1. Kiểm tra an toàn để tránh lỗi NullPointerException
        if (cbChuyenBan == null) {
            return;
        }

        // 2. Xóa sạch các mục cũ trong ComboBox
        cbChuyenBan.removeAllItems();
        
        // 3. (Tùy chọn) Thêm một dòng mặc định để người dùng dễ nhận biết
        //cbChuyenBan.addItem("Chuyển đến");

        // 4. Truy vấn lấy danh sách tên bàn
        String sql = "SELECT table_name FROM billiard_tables ORDER BY table_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Lấy tên bàn từ cột table_name
                String tenBan = rs.getString("table_name");
                // Thêm vào ComboBox
                cbChuyenBan.addItem(tenBan);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách bàn vào ComboBox: " + e.getMessage());
        }
    }
    
    private void startTimer(Timestamp startTimeFromDB) {
        // 1. Nếu có timer cũ đang chạy thì dừng lại để tránh xung đột
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        // 2. Lưu mốc thời gian bắt đầu từ Database
        this.startTimestamp = startTimeFromDB.getTime();

        // 3. Khởi tạo Timer mới (cứ 1000ms = 1 giây thì cập nhật Label một lần)
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long now = System.currentTimeMillis();
                long duration = now - startTimestamp; // Độ lệch thời gian

                // Tính toán Giờ:Phút:Giây
                long seconds = (duration / 1000) % 60;
                long minutes = (duration / (1000 * 60)) % 60;
                long hours = (duration / (1000 * 60 * 60));

                // Hiển thị lên Label (lblThoiGian hoặc lblTimer tùy tên bạn đặt)
                lblThoiGian.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            }
        });

        timer.start(); // Bắt đầu chạy đồng hồ
    }
    
    private void xuLyThoat() {
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Bạn có chắc chắn muốn thoát chương trình không?", 
            "Xác nhận thoát", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // Thực hiện các việc cần thiết trước khi thoát (nếu có) như đóng kết nối DB
            System.exit(0);
        }
    }
}