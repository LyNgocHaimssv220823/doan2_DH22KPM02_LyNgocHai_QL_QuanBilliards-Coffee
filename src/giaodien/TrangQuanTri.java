package giaodien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;

import com.toedter.calendar.JDateChooser;

import database.DBConnection;

import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.event.ActionEvent;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class TrangQuanTri extends JFrame {
	// --- Nhóm Cấu trúc Hệ thống & Chuyển đổi Trang ---
	private JFrame parentFrame;             // Lưu trang chủ để quay lại khi đóng cửa sổ
	private JTabbedPane tabbedPane;         // Thành phần chứa các Tab quản lý
	private javax.swing.JPanel TabTaiKhoan, TabBan, TabMon, TabDoanhThu; // Các Panel đại diện cho từng Tab

	// --- Nhóm Quản lý Tài khoản (User Management) ---
	private JTable tableUsers;              // Bảng hiển thị danh sách người dùng
	private javax.swing.JScrollPane scrollTK; // Khung cuộn cho bảng tài khoản
	private JTextField txtUsername, txtRole; // Ô nhập tên đăng nhập và hiển thị quyền
	private JPasswordField txtPassword;      // Ô nhập mật khẩu (ẩn ký tự)
	private JComboBox<String> cbRole;        // Lựa chọn quyền hạn (Admin/Staff)
	private int selectedUserId = -1;         // Lưu ID người dùng đang được chọn

	// --- Nhóm Quản lý Bàn (Table Management) ---
	private JTable tableBan;                // Bảng hiển thị danh sách bàn bida
	private JTextField txtTenBan, txtGiaGioChoi; // Nhập tên bàn và đơn giá theo giờ
	private int selectedBanId = -1;          // Lưu ID bàn đang được chọn

	// --- Nhóm Quản lý Món (Drink Management) ---
	private JTable tableMon;                // Bảng hiển thị thực đơn
	private DefaultTableModel modelMon;     // Model dữ liệu cho bảng món
	private JTextField txtTenMon, txtGiaMon; // Nhập tên món và đơn giá nước uống

	// --- Nhóm Quản lý Doanh Thu (Revenue Management) ---
	private JTable tableDoanhThu;           // Bảng hiển thị lịch sử hóa đơn
	private DefaultTableModel modelDoanhThu; // Model dữ liệu cho bảng doanh thu
	private JDateChooser dateNgayBD, dateNgayKT; // Bộ chọn khoảng thời gian báo cáo
	private JComboBox<String> cbBan, cbMon; // Bộ lọc theo bàn hoặc món cụ thể
	private JTextField txtTongDoanhThu, txtTong; // Hiển thị tổng số tiền thu được
	private JSpinner spPhanTramThue;
	private JTextField txtTienThue;
	private DecimalFormat df = new DecimalFormat("#,###");
    

    public TrangQuanTri(JFrame parent) {
        this.parentFrame = parent;
        setTitle("TRANG QUẢN TRỊ");
        setSize(900, 550);
        setLocationRelativeTo(null);      
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (parentFrame != null) {
                    // 1. Hiện lại trang chủ
                    parentFrame.setVisible(true); 
                    
                    // 2. Ép kiểu để gọi hàm cập nhật từ Trang Chủ
                    if (parentFrame instanceof TrangChu) {
                        ((TrangChu) parentFrame).loadDanhSachBan();
                    }
                }
            }
        });
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Tài khoản", TabTaiKhoan());
        tabbedPane.addTab("Bàn", TabBan());
        tabbedPane.addTab("Món", TabMon());
        tabbedPane.addTab("Doanh thu", TabDoanhThu());

        getContentPane().add(tabbedPane);
        
        //load data ngay khi mở tab
        loadDataToTableTK();
        loadDataToTableBan();
        loadDataMon();
        loadComboBoxDT();
    }

    // ================= TAB TÀI KHOẢN =================
    private JPanel TabTaiKhoan() {
        JPanel panel = new JPanel(null);
        
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblUser.setBounds(268, 20, 100, 25);
        panel.add(lblUser);

        txtUsername = new JTextField(); 
        txtUsername.setBounds(372, 22, 150, 25);
        panel.add(txtUsername);

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblPass.setBounds(268, 60, 100, 25);
        panel.add(lblPass);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(372, 62, 150, 25);
        panel.add(txtPassword);

        JLabel lblRole = new JLabel("Quyền:");
        lblRole.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblRole.setBounds(295, 102, 67, 25);
        panel.add(lblRole);
        
        // btn tabTaiKhoan
        
        JButton btnThem = new JButton("Thêm");
        btnThem.setBounds(556, 19, 90, 30);
        btnThem.addActionListener(e -> themTaiKhoan());
        panel.add(btnThem);

        JButton btnXoa = new JButton("Xóa");
        btnXoa.setBounds(556, 59, 90, 30);
        btnXoa.addActionListener(e -> xoaTaiKhoan());
        panel.add(btnXoa);

        JButton btnSua = new JButton("Sửa");
        btnSua.addActionListener(e -> suaTaiKhoan());
        btnSua.setBounds(556, 101, 90, 30);
        panel.add(btnSua);

        // Bảng hiển thị
        tableUsers = new JTable();
        JScrollPane scrollTK = new JScrollPane(tableUsers);
        scrollTK.setBounds(20, 150, 830, 300);
        panel.add(scrollTK);
        
        txtRole = new JTextField(); 
        txtRole.setBounds(372, 104, 150, 25);
        panel.add(txtRole);
     // Sự kiện khi click vào một dòng trên bảng
        tableUsers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tableUsers.getSelectedRow(); // Lấy dòng được chọn
                if (row != -1) {
                    // Lấy ID từ cột 0 để phục vụ việc Sửa/Xóa
                    selectedUserId = Integer.parseInt(tableUsers.getValueAt(row, 0).toString());
                    
                    // Lấy Username (Cột 1)
                    txtUsername.setText(tableUsers.getValueAt(row, 1).toString());
                    
                    // Lấy Password (Cột 2)
                    txtPassword.setText(tableUsers.getValueAt(row, 2).toString());
                    
                    // Lấy Role (Cột 3)
                    txtRole.setText(tableUsers.getValueAt(row, 3).toString());
                }
            }
        });
        return panel;
    }

    // ================= TAB BÀN =================
    private JPanel TabBan() {
        JPanel panel = new JPanel(null);

        JLabel lblTenBan = new JLabel("Tên bàn:");
        lblTenBan.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblTenBan.setBounds(43, 18, 77, 25);
        panel.add(lblTenBan);

        txtTenBan = new JTextField();
        txtTenBan.setBounds(120, 20, 150, 25);
        panel.add(txtTenBan);

        JLabel lblSetGia = new JLabel("Giá giờ chơi:");
        lblSetGia.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblSetGia.setBounds(430, 21, 100, 25);
        panel.add(lblSetGia);

        txtGiaGioChoi = new JTextField();
        txtGiaGioChoi.setBounds(526, 20, 150, 25);
        panel.add(txtGiaGioChoi);

        // Các nút bấm và sự kiện
        JButton btnThemBan = new JButton("Thêm bàn");
        btnThemBan.setBounds(300, 17, 100, 30);
        btnThemBan.addActionListener(e -> themBan());
        panel.add(btnThemBan);

        JButton btnXoaBan = new JButton("Xóa bàn");
        btnXoaBan.setBounds(300, 57, 100, 30);
        btnXoaBan.addActionListener(e -> xoaBan());
        panel.add(btnXoaBan);

        JButton btnSuaBan = new JButton("Sửa");
        btnSuaBan.setBounds(300, 97, 100, 30);
        btnSuaBan.addActionListener(e -> suaBan());
        panel.add(btnSuaBan);

        JButton btnCapNhatGia = new JButton("Cập nhật giá");
        btnCapNhatGia.addActionListener(e -> capNhatGiaGioChoi());
        btnCapNhatGia.setBounds(700, 17, 152, 30);
        panel.add(btnCapNhatGia);

        // Bảng hiển thị
        tableBan = new JTable();
        JScrollPane scrollBan = new JScrollPane(tableBan);
        scrollBan.setBounds(20, 146, 832, 304);
        panel.add(scrollBan);

        
        tableBan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tableBan.getSelectedRow();
                if (row != -1) {
                    // Lấy ID từ cột 0 (table_id)
                    selectedBanId = Integer.parseInt(tableBan.getValueAt(row, 0).toString());
                    // Đổ tên lên ô nhập để sửa
                    txtTenBan.setText(tableBan.getValueAt(row, 1).toString());
                }
            }
        });
        return panel;
    }

    // ================= TAB MÓN =================
    private JPanel TabMon() {
        JPanel panel = new JPanel(null);

        JLabel lblTen = new JLabel("Tên món:");
        lblTen.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblTen.setBounds(281, 21, 79, 25);
        panel.add(lblTen);

        txtTenMon = new JTextField();
        txtTenMon.setBounds(370, 23, 150, 25);
        panel.add(txtTenMon);

        JLabel lblGia = new JLabel("Giá:");
        lblGia.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblGia.setBounds(317, 60, 43, 25);
        panel.add(lblGia);

        txtGiaMon = new JTextField();
        txtGiaMon.setBounds(370, 60, 150, 25);
        panel.add(txtGiaMon);

        // Nút bấm
        JButton btnThemMon = new JButton("Thêm");
        btnThemMon.setBounds(552, 20, 90, 30);
        panel.add(btnThemMon);
        btnThemMon.addActionListener(e -> {
            String ten = txtTenMon.getText();
            String giaStr = txtGiaMon.getText();
            if(!ten.isEmpty() && !giaStr.isEmpty()){
                executeSQL("INSERT INTO drinks(drink_name, price) VALUES(?, ?)", ten, Double.parseDouble(giaStr));
                loadDataMon();
                clearForm();
            }
        });

        JButton btnXoaMon = new JButton("Xóa");
        btnXoaMon.setBounds(552, 60, 90, 30);
        panel.add(btnXoaMon);
        btnXoaMon.addActionListener(e -> {
            int row = tableMon.getSelectedRow();
            if(row != -1){
                int id = (int) modelMon.getValueAt(row, 0);
                executeSQL("DELETE FROM drinks WHERE drink_id = ?", id);
                loadDataMon();
                clearForm();
            }
        });

        JButton btnSuaMon = new JButton("Sửa");
        btnSuaMon.setBounds(552, 100, 90, 30);
        panel.add(btnSuaMon);
        btnSuaMon.addActionListener(e -> {
            int row = tableMon.getSelectedRow();
            if(row != -1){
                int id = (int) modelMon.getValueAt(row, 0);
                String ten = txtTenMon.getText();
                double gia = Double.parseDouble(txtGiaMon.getText());
                executeSQL("UPDATE drinks SET drink_name = ?, price = ? WHERE drink_id = ?", ten, gia, id);
                loadDataMon();
                clearForm();
            }
        });

        // JTable hiển thị món
        modelMon = new DefaultTableModel(new Object[]{"ID", "Tên món", "Giá"}, 0);
        tableMon = new JTable(modelMon);
        JScrollPane scrollMon = new JScrollPane(tableMon);
        scrollMon.setBounds(20, 150, 830, 300);
        panel.add(scrollMon);
   
        
        //Click vào dòng trong bảng hiện lên Textfield
        tableMon.getSelectionModel().addListSelectionListener(e -> {
            int row = tableMon.getSelectedRow();
            if (row != -1) {
                txtTenMon.setText(modelMon.getValueAt(row, 1).toString());
                txtGiaMon.setText(modelMon.getValueAt(row, 2).toString());
            }
        });
        
        return panel;
    }

    // ================= TAB DOANH THU =================
    private JPanel TabDoanhThu() {
        JPanel panel = new JPanel(null);
        
        dateNgayBD = new JDateChooser();
        dateNgayBD.setBounds(120, 20, 150, 25);
        dateNgayBD.setDateFormatString("yyyy-MM-dd");
        panel.add(dateNgayBD);

        dateNgayKT = new JDateChooser();
        dateNgayKT.setBounds(120, 60, 150, 25);
        dateNgayKT.setDateFormatString("yyyy-MM-dd");
        panel.add(dateNgayKT);

        cbBan = new JComboBox<>();
        cbBan.setBounds(341, 22, 124, 21);
        panel.add(cbBan);
        cbBan.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                loadComboBoxDT(); // Cứ mỗi lần click mở ComboBox là nó load lại từ DB
            }
            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });

        cbMon = new JComboBox<>();
        cbMon.setBounds(341, 62, 124, 21);
        panel.add(cbMon);
        cbMon.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                loadComboBoxDT(); // Cứ mỗi lần click mở ComboBox là nó load lại từ DB
            }
            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });

        // --- Các Label tiêu đề ---
        JLabel lblNgayBD = new JLabel("Từ ngày:");
        lblNgayBD.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblNgayBD.setBounds(20, 20, 90, 25);
        panel.add(lblNgayBD);

        JLabel lblNgayKT = new JLabel("Đến ngày:");
        lblNgayKT.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblNgayKT.setBounds(20, 60, 90, 25);
        panel.add(lblNgayKT);

        JLabel lblBan = new JLabel("Bàn:");
        lblBan.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblBan.setBounds(290, 20, 41, 25);
        panel.add(lblBan);

        JLabel lblMon = new JLabel("Món:");
        lblMon.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblMon.setBounds(290, 60, 41, 25);
        panel.add(lblMon);
        
     // --- Thiết lập Bảng hiển thị ---
        modelDoanhThu = new DefaultTableModel(
            new Object[]{""}, 0
        );
        tableDoanhThu = new JTable(modelDoanhThu);
        JScrollPane scrollDT = new JScrollPane(tableDoanhThu);
        scrollDT.setBounds(20, 100, 830, 323);
        panel.add(scrollDT);

        // --- Thiết lập Tổng tiền ---
        JLabel lblDoanhThu = new JLabel("Tổng doanh thu:");
        lblDoanhThu.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblDoanhThu.setBounds(537, 433, 160, 25);
        panel.add(lblDoanhThu);

        txtTongDoanhThu = new JTextField();
        txtTongDoanhThu.setBounds(673, 433, 177, 25);
        txtTongDoanhThu.setEditable(false);
        txtTongDoanhThu.setFont(new Font("Tahoma", Font.BOLD, 14));
        txtTongDoanhThu.setForeground(Color.RED);
        panel.add(txtTongDoanhThu);

        // --- Nút bấm Thống kê ---
     // 1. Nút Thống kê
        JButton btnThongKe = new JButton("Thống kê");
        btnThongKe.setFont(new Font("Tahoma", Font.BOLD, 13));
        btnThongKe.setBounds(480, 20, 110, 65);
        panel.add(btnThongKe);

        // Sự kiện nút Thống kê: Gọi hàm tính doanh thu rồi tính thuế ngay lập tức
        btnThongKe.addActionListener(e -> {
            thongKeDoanhThu(); // Hàm xử lý SQL và hiển thị txtTongDoanhThu
            tinhTienThueTuDong(); 
        });

        // 2. Nhãn % thuế
        JLabel lblPhanTramThue = new JLabel("Phần trăm thuế:");
        lblPhanTramThue.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblPhanTramThue.setBounds(617, 20, 124, 25);
        panel.add(lblPhanTramThue);

        // 3. JSpinner chọn % thuế
        spPhanTramThue = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1));
        spPhanTramThue.setBounds(753, 20, 53, 25);
        panel.add(spPhanTramThue);

        // Sự kiện thay đổi giá trị Spinner: Tự động cập nhật tiền thuế khi bấm tăng/giảm
        spPhanTramThue.addChangeListener(e -> {
            if (txtTongDoanhThu != null && !txtTongDoanhThu.getText().isEmpty()) {
                tinhTienThueTuDong();
            }
        });

        // 4. Nhãn Tiền thuế
        JLabel lblTienThue = new JLabel("Tiền thuế:");
        lblTienThue.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblTienThue.setBounds(617, 60, 80, 25);
        panel.add(lblTienThue);

        // 5. Ô hiển thị Tiền thuế
        txtTienThue = new JTextField();
        txtTienThue.setFont(new Font("Tahoma", Font.BOLD, 12));
        txtTienThue.setBounds(699, 60, 107, 25);
        txtTienThue.setEditable(false); // Không cho phép sửa thủ công
        txtTienThue.setColumns(10);
        panel.add(txtTienThue);
        

        return panel;
    }
    
    private void themTaiKhoan() {
        // Lấy dữ liệu từ các JTextField
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText().trim();
        String role = txtRole.getText().trim();

        if (user.isEmpty() || pass.isEmpty() || role.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, user);
            ps.setString(2, pass);
            ps.setString(3, role); // Lưu ý: Phải nhập đúng 'ADMIN' hoặc 'NHANVIEN'
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
                
                loadDataToTableTK(); // Load lại bảng
                clearForm();
                
            }
        } catch (SQLException e) {
            // Lỗi thường gặp: Data truncated (do nhập role sai chữ ADMIN/NHANVIEN)
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: Kiểm tra lại quyền (ADMIN/NHANVIEN)!");
        }
    }
    
    private void suaTaiKhoan() {
        if (selectedUserId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản từ bảng để sửa!");
            return;
        }

        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText().trim();
        String role = txtRole.getText().trim().toUpperCase(); // Đảm bảo luôn viết hoa

        if (user.isEmpty() || pass.isEmpty() || role.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Thông tin không được để trống!");
            return;
        }

        String sql = "UPDATE users SET username = ?, password = ?, role = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, user);
            ps.setString(2, pass);
            ps.setString(3, role);
            ps.setInt(4, selectedUserId); // Dùng ID đã lưu khi click chuột
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Cập nhật tài khoản thành công!");
                
                loadDataToTableTK();
                clearForm();
                
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi sửa: " + e.getMessage());
        }
    }
    
    private void xoaTaiKhoan() {
        if (selectedUserId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản cần xóa!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa tài khoản này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM users WHERE user_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, selectedUserId);
                if (ps.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                    
                    loadDataToTableTK();
                    clearForm();
                    
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void loadDataToTableTK() {
        // 1. Tạo danh sách tiêu đề cột
        String[] columnNames = {"ID", "Username", "Password", "Quyền"};
        
        // 2. Khởi tạo Model và KHÓA chức năng sửa trực tiếp
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Trả về false để tất cả các ô trên bảng không thể sửa đổi bằng cách gõ trực tiếp
                return false;
            }
        };

        // 3. Truy vấn dữ liệu từ MySQL
        String sql = "SELECT user_id, username, password, role FROM users";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role")
                });
            }
            
            // 4. Gán model vào bảng tableUsers
            tableUsers.setModel(model);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void themBan() {
        String tenBan = txtTenBan.getText().trim();
        if (tenBan.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên bàn!");
            return;
        }
        
        String sql = "INSERT INTO billiard_tables (table_name) VALUES (?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, tenBan);
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Thêm bàn thành công!");
                
                clearForm();
                loadDataToTableBan(); 
                
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }
    
    private void suaBan() {
        if (selectedBanId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bàn từ bảng để sửa!");
            return;
        }

        String tenMoi = txtTenBan.getText().trim();
        if (tenMoi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên bàn không được để trống!");
            return;
        }

        // Câu lệnh SQL cập nhật table_name dựa trên table_id
        String sql = "UPDATE billiard_tables SET table_name = ? WHERE table_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, tenMoi);
            ps.setInt(2, selectedBanId); // Biến này được gán khi bạn click vào bảng
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Cập nhật tên bàn thành công!");
                
                loadDataToTableBan(); // Tải lại bảng để thấy thay đổi
                clearForm();
                
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi sửa: " + e.getMessage());
        }
    }
    private void xoaBan() {
        if (selectedBanId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn cần xóa từ bảng!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa bàn này không?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM billiard_tables WHERE table_id = ?";
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setInt(1, selectedBanId);
                
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Đã xóa bàn thành công!");
                    
                    loadDataToTableBan();
                    clearForm();
                    
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Không thể xóa vì bàn đã có hóa đơn ");
            }
        }
    }
    
    private void loadDataToTableBan() {
        // 1. Định nghĩa tiêu đề cột hiển thị trên JTable
        String[] columnNames = {"ID", "Tên Bàn", "Trạng Thái"};
        
        // 2. Tạo Model và khóa chức năng sửa trực tiếp trên ô
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        // 3. Câu lệnh SQL chuẩn với tên bảng: billiard_tables
        String sql = "SELECT table_id, table_name, status FROM billiard_tables";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("table_id"),
                    rs.getString("table_name"),
                    rs.getString("status")
                });
            }
            
            // 4. Đổ dữ liệu vào tableBan
            tableBan.setModel(model);
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối CSDL: " + e.getMessage());
        }
    }
    
    private void capNhatGiaGioChoi() {
        String giaMoiStr = txtGiaGioChoi.getText().trim();

        if (giaMoiStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập giá tiền muốn thay đổi!");
            return;
        }

        try {
            double giaMoi = Double.parseDouble(giaMoiStr);

            int confirm = JOptionPane.showConfirmDialog(this, 
                "Xác nhận thay đổi giá : " + giaMoi + " VNĐ/giờ?", 
                "Cấu hình hệ thống", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // Câu lệnh SQL thay đổi giá trị mặc định của cột
                String sql = "ALTER TABLE billiard_sessions MODIFY COLUMN price_per_hour DECIMAL(10,2) DEFAULT " + giaMoi;

                try (Connection conn = DBConnection.getConnection();
                     Statement st = conn.createStatement()) {
                    
                    st.executeUpdate(sql);
                    
                    JOptionPane.showMessageDialog(this, "Đã cập nhật giá mới thành công!");
                    
                    clearForm();
                   
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá tiền phải là số!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi thay đổi cấu trúc bảng: " + e.getMessage());
        }
    }

    private void loadDataMon() {
        modelMon.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM drinks")) {
            while (rs.next()) {
                modelMon.addRow(new Object[]{rs.getInt("drink_id"), rs.getString("drink_name"), (long) rs.getDouble("price")});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Hàm thực thi SQL dùng chung (Thêm/Xóa/Sửa)
    private void executeSQL(String sql, Object... params) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Thao tác thành công!");
        } catch (SQLException e) { 
            // Kiểm tra mã lỗi khóa ngoại của MySQL (thường là 1451)
            if (e.getErrorCode() == 1451) {
                JOptionPane.showMessageDialog(null, "Không thể xóa vì nó đã tồn tại trong các hóa đơn cũ!");
            } else {
                JOptionPane.showMessageDialog(null, "Lỗi: " + e.getMessage());
            }
        }
    }
    
    private void loadComboBoxDT() {
        cbBan.removeAllItems();
        cbMon.removeAllItems();
        cbBan.addItem("Tất cả bàn");
        cbMon.addItem("Tất cả món");
        
        try (Connection conn = DBConnection.getConnection()) {
            // Lấy danh sách bàn
            ResultSet rsBan = conn.createStatement().executeQuery("SELECT table_name FROM billiard_tables");
            while (rsBan.next()) cbBan.addItem(rsBan.getString("table_name"));
            
            // Lấy danh sách món
            ResultSet rsMon = conn.createStatement().executeQuery("SELECT drink_name FROM drinks");
            while (rsMon.next()) cbMon.addItem(rsMon.getString("drink_name"));
            
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void thongKeDoanhThu() {
        modelDoanhThu.setRowCount(0);
        double tongCong = 0;
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String ngayBD = (dateNgayBD.getDate() != null) ? sdf.format(dateNgayBD.getDate()) : "1970-01-01";
        String ngayKT = (dateNgayKT.getDate() != null) ? sdf.format(dateNgayKT.getDate()) : "2099-12-31";
        
        String selectedBan = cbBan.getSelectedItem().toString();
        String selectedMon = cbMon.getSelectedItem().toString();

        String sql = "";
        
        // TRƯỜNG HỢP 2: THỐNG KÊ THEO MÓN TRONG KHOẢNG THỜI GIAN
        if (!selectedMon.equals("Tất cả món")) {
            sql = "SELECT d.drink_name AS name_display, SUM(od.quantity) AS qty, SUM(od.quantity * od.price) AS subtotal " +
                  "FROM order_details od " +
                  "JOIN drinks d ON od.drink_id = d.drink_id " +
                  "JOIN orders o ON od.order_id = o.order_id " +
                  "WHERE DATE(o.order_time) BETWEEN ? AND ? AND o.status = 'PAID' " +
                  "AND d.drink_name = ? " +
                  "GROUP BY d.drink_name";
            // Cập nhật lại cột cho Table khi xem theo món
            modelDoanhThu.setColumnIdentifiers(new Object[]{"Tên món", "Số lượng bán", "Tổng doanh thu món"});
        } 
        // TRƯỜNG HỢP 3: THỐNG KÊ THEO BÀN TRONG KHOẢNG THỜI GIAN
        else if (!selectedBan.equals("Tất cả bàn")) {
            sql = "SELECT b.table_name AS name_display, COUNT(o.order_id) AS qty, SUM(o.total_amount) AS subtotal " +
                  "FROM orders o " +
                  "JOIN billiard_tables b ON o.table_id = b.table_id " +
                  "WHERE DATE(o.order_time) BETWEEN ? AND ? AND o.status = 'PAID' " +
                  "AND b.table_name = ? " +
                  "GROUP BY b.table_name";
            modelDoanhThu.setColumnIdentifiers(new Object[]{"Tên bàn", "Số lượt chơi", "Tổng doanh thu bàn"});
        }
        // TRƯỜNG HỢP 1: THỐNG KÊ DOANH THU TỔNG QUÁT THEO KHOẢNG THỜI GIAN
        else {
            sql = "SELECT o.order_time AS name_display, o.order_id AS qty, o.total_amount AS subtotal " +
                  "FROM orders o " +
                  "WHERE DATE(o.order_time) BETWEEN ? AND ? AND o.status = 'PAID' " +
                  "ORDER BY o.order_time DESC";
            modelDoanhThu.setColumnIdentifiers(new Object[]{"Thời gian", "Mã hóa đơn", "Doanh thu"});
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, ngayBD);
            ps.setString(2, ngayKT);
            
            if (!selectedMon.equals("Tất cả món")) ps.setString(3, selectedMon);
            else if (!selectedBan.equals("Tất cả bàn")) ps.setString(3, selectedBan);

            ResultSet rs = ps.executeQuery();
            DecimalFormat df = new DecimalFormat("#,###");

            while (rs.next()) {
                double tien = rs.getDouble("subtotal");
                tongCong += tien;
                modelDoanhThu.addRow(new Object[]{
                    rs.getString("name_display"),
                    rs.getString("qty"),
                    df.format(tien) + " VNĐ"
                });
            }
            txtTongDoanhThu.setText(df.format(tongCong) + " VNĐ");

        } catch (SQLException e) {
            e.printStackTrace();
        }   
        
    }
    
    private void tinhTienThueTuDong() {
        try {
            // Xóa sạch các ký tự không phải số để tránh lỗi parse
            String cleanDoanhThu = txtTongDoanhThu.getText().replaceAll("[^0-9]", "");
            
            if (cleanDoanhThu.isEmpty()) {
                txtTienThue.setText("0");
                return;
            }

            double tong = Double.parseDouble(cleanDoanhThu);
            int phanTram = (int) spPhanTramThue.getValue();
            double thue = tong * (phanTram / 100.0);

            // Hiển thị lên ô txtTienThue với định dạng có dấu phẩy
            txtTienThue.setText(df.format(thue)+ " VNĐ");
            
        } catch (Exception e) {
            txtTienThue.setText("0");
            System.out.println("Lỗi tính thuế: " + e.getMessage());
        }
    }

    private void clearForm() {  	
        txtUsername.setText("");
        txtPassword.setText("");
        txtRole.setText("");
        selectedUserId = -1;
        txtTenBan.setText("");
        txtGiaGioChoi.setText("");
        selectedBanId = -1;
        txtTenMon.setText("");
        txtGiaMon.setText(""); 
    }
}
